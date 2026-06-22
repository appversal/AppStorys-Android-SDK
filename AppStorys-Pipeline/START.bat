@echo off
title AppStorys Test Pipeline
cd /d "%~dp0"
echo.
echo  AppStorys Test Pipeline
echo  ─────────────────────────────────────
echo  Open http://localhost:4321
echo  ─────────────────────────────────────
echo.
node pipeline-server.js
pause