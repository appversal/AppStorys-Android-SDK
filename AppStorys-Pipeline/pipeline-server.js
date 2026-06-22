#!/usr/bin/env node
'use strict';

/**
 * AppStorys Test Pipeline Server
 * ─────────────────────────────────────────────────────────────────────────────
 * All settings live in pipeline-config.json (same folder as this file).
 * Add new SDK platforms there — no code changes needed.
 *
 * Layer 1  DB vs Published JSON   (diff tool, no device needed)
 * Layer 2  Published vs SDK Parse (auto-launches app, pulls dump via adb/idb)
 * Layer 3  Visual Snapshot        (Paparazzi / platform snapshot tool)
 * Layer 4  Maestro Flows          (individual flows, not suite file)
 */

var http   = require('http');
var url    = require('url');
var fs     = require('fs');
var path   = require('path');
var exec   = require('child_process').exec;

var BASE   = __dirname;
var CFG    = JSON.parse(fs.readFileSync(path.join(BASE, 'pipeline-config.json'), 'utf8'));
var HTML   = fs.readFileSync(path.join(BASE, 'dashboard.html'), 'utf8');
var PORT   = 4321;

// ── HELPERS ───────────────────────────────────────────────────────────────────
function shell(cmd, opts) {
  return new Promise(function(resolve) {
    exec(cmd, { timeout: 180000, cwd: (opts && opts.cwd) || BASE }, function(err, stdout, stderr) {
      resolve({ ok: !err, stdout: stdout || '', stderr: stderr || '' });
    });
  });
}

function sleep(ms) {
  return new Promise(function(resolve) { setTimeout(resolve, ms); });
}

function readFirstImage(dir) {
  try {
    var files = fs.readdirSync(dir).filter(function(f) { return f.endsWith('.png'); });
    if (!files.length) return null;
    return { name: files[0], b64: fs.readFileSync(path.join(dir, files[0])).toString('base64') };
  } catch(e) { return null; }
}

function parseDiff(raw) {
  var matched = 0;
  var m = raw.match(/(\d+) values match/);
  if (m) matched = parseInt(m[1]);
  var mismatches = [], transforms = [];
  var zone = '';
  raw.split('\n').forEach(function(line) {
    if (line.indexOf('REAL mismatch') >= 0) { zone = 'mis'; return; }
    if (line.indexOf('known transforms') >= 0 || line.indexOf('ignored') >= 0) { zone = 'tr'; return; }
    if (line.indexOf('VERDICT') >= 0) { zone = ''; return; }
    var b = line.match(/^\s+[·•]\s+(.+)/);
    if (!b) return;
    if (zone === 'mis') mismatches.push(b[1].trim());
    else if (zone === 'tr') transforms.push(b[1].trim());
  });
  var pass = mismatches.length === 0 && (raw.indexOf('VERDICT') < 0 || raw.indexOf('clean') >= 0 || raw.indexOf('PASS') >= 0);
  return { matched: matched, mismatches: mismatches, transforms: transforms, pass: pass, raw: raw };
}

function sse(res, obj) {
  res.write('data: ' + JSON.stringify(obj) + '\n\n');
}

// ── DEVICE DETECTION & APP LAUNCH ─────────────────────────────────────────────
function getAndroidDevices() {
  return shell('adb devices').then(function(r) {
    var lines = r.stdout.split('\n').slice(1).filter(function(l) {
      return l.trim() && l.indexOf('device') >= 0 && l.indexOf('offline') < 0;
    });
    return lines.map(function(l) { return l.split('\t')[0].trim(); });
  });
}

function launchAndroid(androidCfg) {
  var cmd = 'adb shell am start -n ' + androidCfg.package + '/' + androidCfg.activity;
  return shell(cmd);
}

function pullAndroidDump(androidCfg) {
  var dumpPath = path.join(BASE, 'dumps', androidCfg.dumpFile);
  var cmd = 'adb exec-out run-as ' + androidCfg.package + ' cat files/' + androidCfg.dumpFile;
  return shell(cmd).then(function(r) {
    if (r.ok && r.stdout.trim().length > 20) {
      if (!fs.existsSync(path.join(BASE, 'dumps'))) fs.mkdirSync(path.join(BASE, 'dumps'));
      fs.writeFileSync(dumpPath, r.stdout);
      return { ok: true, dumpFile: dumpPath };
    }
    return { ok: false, msg: 'Dump empty or pull failed: ' + (r.stderr || 'no output') };
  });
}

function getIosSimulators() {
  return shell('xcrun simctl list devices booted --json').then(function(r) {
    try {
      var data = JSON.parse(r.stdout);
      var booted = [];
      Object.keys(data.devices || {}).forEach(function(rt) {
        (data.devices[rt] || []).forEach(function(d) {
          if (d.state === 'Booted') booted.push(d.udid);
        });
      });
      return booted;
    } catch(e) { return []; }
  });
}

function launchIos(iosCfg) {
  return shell('xcrun simctl launch booted ' + iosCfg.bundleId);
}

function pullIosDump(iosCfg) {
  var dumpPath = path.join(BASE, 'dumps', iosCfg.dumpFile);
  // Try idb first, then xcrun simctl (for simulator)
  var cmd = 'xcrun simctl get_app_container booted ' + iosCfg.bundleId + ' data';
  return shell(cmd).then(function(r) {
    if (!r.ok) return { ok: false, msg: 'Could not get iOS app container. Is idb or simctl available?' };
    var containerPath = r.stdout.trim() + '/Documents/' + iosCfg.dumpFile;
    return shell('cp "' + containerPath + '" "' + dumpPath + '"').then(function(r2) {
      return r2.ok ? { ok: true, dumpFile: dumpPath } : { ok: false, msg: 'Could not copy iOS dump: ' + r2.stderr };
    });
  });
}

// ── LAYER 1: DB vs Published JSON ─────────────────────────────────────────────
function layer1(res, campaignId, token) {
  sse(res, { layer: 1, status: 'running', msg: 'Comparing DB vs published JSON...' });
  return shell('node "' + CFG.diffTool + '" ' + campaignId + ' --token "' + token + '"')
    .then(function(r) {
      var d = parseDiff(r.stdout + r.stderr);
      sse(res, {
        layer: 1, status: d.pass ? 'pass' : 'fail',
        msg: d.pass ? d.matched + ' fields match — DB to Published is clean' : d.mismatches.length + ' mismatch(es) found',
        matched: d.matched, mismatches: d.mismatches, transforms: d.transforms, raw: d.raw
      });
      return d.pass;
    });
}

// ── LAYER 2: Published vs SDK Parse (auto-launch) ─────────────────────────────
function layer2(res, campaignId, platform, platformCfg) {
  sse(res, { layer: 2, status: 'running', msg: 'Auto-launching app to trigger campaign fetch...' });

  var isIos = platform === 'ios_swift';
  var devCfg = isIos ? platformCfg.ios : platformCfg.android;

  if (!devCfg) {
    sse(res, { layer: 2, status: 'skip', msg: (platformCfg.note || 'Platform not configured. Add config in pipeline-config.json.') });
    return Promise.resolve(null);
  }

  // Step 1: Check device
  var deviceCheck = isIos ? getIosSimulators() : getAndroidDevices();
  return deviceCheck.then(function(devices) {
    if (!devices.length) {
      sse(res, { layer: 2, status: 'skip', msg: 'No connected ' + (isIos ? 'iOS simulator' : 'Android device/emulator') + ' found. Connect one and re-run.' });
      return null;
    }

    sse(res, { layer: 2, status: 'running', msg: 'Device found (' + devices[0] + ') — launching app...' });

    // Step 2: Launch app
    var launchFn = isIos ? launchIos(devCfg) : launchAndroid(devCfg);
    return launchFn.then(function(launchResult) {
      if (!launchResult.ok && launchResult.stderr) {
        // Non-fatal — app might already be running
        console.log('[Layer 2] Launch warning:', launchResult.stderr.trim());
      }

      var delay = (devCfg.launchDelay || 4) * 1000;
      sse(res, { layer: 2, status: 'running', msg: 'App launched — waiting ' + (delay/1000) + 's for SDK to fetch campaigns...' });

      // Step 3: Wait for SDK
      return sleep(delay).then(function() {
        sse(res, { layer: 2, status: 'running', msg: 'Pulling SDK dump from device...' });

        // Step 4: Pull dump
        var pullFn = isIos ? pullIosDump(devCfg) : pullAndroidDump(devCfg);
        return pullFn.then(function(pullResult) {
          if (!pullResult.ok) {
            sse(res, { layer: 2, status: 'fail', msg: 'Pull failed: ' + pullResult.msg });
            return false;
          }

          // Step 5: Compare
          return shell('node "' + CFG.diffTool + '" ' + campaignId + ' --parse "' + pullResult.dumpFile + '"')
            .then(function(r) {
              var d = parseDiff(r.stdout + r.stderr);
              sse(res, {
                layer: 2, status: d.pass ? 'pass' : 'fail',
                msg: d.pass ? 'SDK parsed ' + d.matched + ' fields correctly' : d.mismatches.length + ' parse gap(s) found',
                matched: d.matched, mismatches: d.mismatches, transforms: d.transforms, raw: r.stdout + r.stderr
              });
              return d.pass;
            });
        });
      });
    });
  });
}

// ── LAYER 3: Visual Snapshot (Paparazzi / platform) ───────────────────────────
function layer3(res, platform, platformCfg) {
  sse(res, { layer: 3, status: 'running', msg: 'Running snapshot verification...' });

  if (platform !== 'android_kotlin') {
    sse(res, { layer: 3, status: 'skip', msg: 'Snapshot tests only configured for Android (Kotlin) so far. Wire your platform\'s snapshot tool in pipeline-config.json.' });
    return Promise.resolve(null);
  }

  var root = platformCfg.projectRoot;
  var gradlew = '"' + root + '\\gradlew.bat" ' + platformCfg.gradleModule + ':verifyPaparazziDebug --no-daemon';
  return shell(gradlew, { cwd: root }).then(function(r) {
    var imgDir = r.ok ? path.join(root, platformCfg.paparazziGoldenDir) : path.join(root, platformCfg.paparazziFailDir);
    var img = readFirstImage(imgDir);
    sse(res, {
      layer: 3, status: r.ok ? 'pass' : 'fail',
      msg: r.ok ? 'Snapshot matches baseline — no visual regression' : 'Visual difference detected',
      images: img ? [{ name: img.name, b64: img.b64 }] : [],
      raw: r.stdout + r.stderr
    });
    return r.ok;
  });
}

// ── LAYER 4: Maestro Flows ────────────────────────────────────────────────────
function layer4(res, platform, platformCfg) {
  sse(res, { layer: 4, status: 'running', msg: 'Starting Maestro flows...' });

  var maestroDir   = platformCfg.maestroDir;
  var maestroFlows = platformCfg.maestroFlows;

  if (!maestroDir || !maestroFlows || !maestroFlows.length) {
    sse(res, { layer: 4, status: 'skip', msg: 'No Maestro flows configured for ' + platform + '. Add maestroDir and maestroFlows in pipeline-config.json.' });
    return Promise.resolve(null);
  }

  var allSteps = [];
  var allRaw = '';
  var overallOk = true;
  var flowIndex = 0;

  function runNextFlow() {
    if (flowIndex >= maestroFlows.length) {
      var failCount = allSteps.filter(function(s) { return !s.pass; }).length;
      sse(res, {
        layer: 4,
        status: overallOk ? 'pass' : 'fail',
        msg: overallOk ? 'All ' + maestroFlows.length + ' flows passed' : failCount + ' flow(s) failed',
        steps: allSteps,
        raw: allRaw
      });
      return Promise.resolve(overallOk);
    }

    var flowFile = maestroFlows[flowIndex];
    var flowPath = path.join(maestroDir, flowFile);
    flowIndex++;

    sse(res, { layer: 4, status: 'running', msg: 'Flow ' + flowIndex + '/' + maestroFlows.length + ': ' + flowFile });

    if (!fs.existsSync(flowPath)) {
      allSteps.push({ line: '[MISSING] ' + flowFile, pass: false });
      allRaw += '\n[SKIP] ' + flowFile + ' not found at ' + flowPath + '\n';
      overallOk = false;
      return runNextFlow();
    }

    return shell('maestro test "' + flowPath + '"', { cwd: maestroDir })
      .then(function(r) {
        var raw = r.stdout + r.stderr;
        allRaw += '\n=== ' + flowFile + ' ===\n' + raw;

        // Parse individual step results
        raw.split('\n').forEach(function(line) {
          if (line.trim().length < 3) return;
          var pass = /PASSED|✓|✔/.test(line);
          var fail = /FAILED|✗|✘|Error/.test(line);
          if (pass || fail) allSteps.push({ line: '[' + flowFile + '] ' + line.trim(), pass: pass });
        });

        // Summary row per flow
        allSteps.push({ line: flowFile + (r.ok ? ' — PASSED ✓' : ' — FAILED ✗'), pass: r.ok });
        if (!r.ok) overallOk = false;

        return runNextFlow();
      });
  }

  return runNextFlow();
}

// ── PIPELINE RUNNER ───────────────────────────────────────────────────────────
function runPipeline(res, campaignId, token, platform) {
  var platformCfg = CFG.platforms[platform];
  if (!platformCfg) {
    res.writeHead(400); res.end('Unknown platform: ' + platform); return;
  }
  if (!platformCfg.enabled) {
    res.writeHead(400); res.end('Platform ' + platform + ' is not enabled in pipeline-config.json. Set enabled:true and fill in config first.'); return;
  }

  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    'Connection': 'keep-alive',
    'Access-Control-Allow-Origin': '*'
  });

  sse(res, { type: 'start', platform: platform, platformLabel: platformCfg.label });

  layer1(res, campaignId, token)
    .then(function(l1) { return layer2(res, campaignId, platform, platformCfg)
    .then(function(l2) { return layer3(res, platform, platformCfg)
    .then(function(l3) { return layer4(res, platform, platformCfg)
    .then(function(l4) {
      var results = [l1, l2, l3, l4];
      var failed  = results.filter(function(v) { return v === false; }).length;
      var skipped = results.filter(function(v) { return v === null; }).length;
      sse(res, { type: 'complete', verdict: failed === 0 ? 'PASS' : 'FAIL', failed: failed, skipped: skipped });
      res.end();
    }); }); }); })
    .catch(function(err) {
      sse(res, { type: 'error', msg: err.message });
      res.end();
    });
}

// ── HTTP SERVER ───────────────────────────────────────────────────────────────
http.createServer(function(req, res) {
  var p = url.parse(req.url, true);

  if (p.pathname === '/') {
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(HTML);
    return;
  }

  // Return enabled platforms list for the UI dropdown
  if (p.pathname === '/platforms') {
    var platforms = [];
    Object.keys(CFG.platforms).forEach(function(key) {
      var pc = CFG.platforms[key];
      platforms.push({ key: key, label: pc.label, enabled: pc.enabled });
    });
    res.writeHead(200, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
    res.end(JSON.stringify(platforms));
    return;
  }

  if (p.pathname === '/ping') {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('OK');
    return;
  }

  if (p.pathname === '/run') {
    var cid      = p.query.cid;
    var tok      = p.query.tok;
    var platform = p.query.platform || 'android_kotlin';
    if (!cid || !tok) { res.writeHead(400); res.end('missing cid or tok'); return; }
    runPipeline(res, cid, tok, platform);
    return;
  }

  res.writeHead(404); res.end('not found');

}).listen(PORT, function() {
  console.log('');
  console.log('  AppStorys Test Pipeline');
  console.log('  ─────────────────────────────────────────');
  console.log('  Open  ->  http://localhost:' + PORT);
  console.log('');
  console.log('  Enabled platforms:');
  Object.keys(CFG.platforms).forEach(function(k) {
    var pc = CFG.platforms[k];
    console.log('    ' + (pc.enabled ? '[ON] ' : '[off]') + ' ' + pc.label);
  });
  console.log('');
  console.log('  diff tool: ' + CFG.diffTool);
  console.log('');
});