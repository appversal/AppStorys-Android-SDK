package com.appversal.appstorys.ui

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.provider.FontRequest
import androidx.media3.common.*
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.appversal.appstorys.R
import com.appversal.appstorys.api.PipStyling
import com.appversal.appstorys.ui.xml.toDp
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
internal fun PipVideo(
    height: Dp,
    width: Dp,
    videoUri: String,
    fullScreenVideoUri: String?,
    button_text: String,
    position: String?,
    link: String,
    bottomPadding: Dp = 0.dp,
    topPadding: Dp = 0.dp,
    isMovable: Boolean = true,
    pipStyling: PipStyling?,
    onClose: () -> Unit,
    onButtonClick: () -> Unit,
    onExpandClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp * density
    val screenHeight = configuration.screenHeightDp * density

    val pipWidth = width
    val pipHeight = height

    val boundaryPadding = 12.dp
    val boundaryPaddingPx = with(LocalDensity.current) { boundaryPadding.toPx() }

    var pipSize by remember { mutableStateOf(IntSize(0, 0)) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isInitialized by remember { mutableStateOf(false) }

    var isFullScreen by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(true) }

    val pipPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5000)
            .setLoadControl(DefaultLoadControl())
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
                repeatMode = Player.REPEAT_MODE_ALL
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                prepare()
                play()
            }
    }

    val bottomPaddingPx = with(LocalDensity.current) { bottomPadding.toPx() }
    val topPaddingPx = with(LocalDensity.current) { topPadding.toPx() }

    DisposableEffect(Unit) {
        onDispose {
            pipPlayer.release()
        }
    }

    LaunchedEffect(isFullScreen) {
        if (isFullScreen) {
            pipPlayer.pause()
        } else {
            pipPlayer.play()
        }
    }

    LaunchedEffect(isMuted) {
        pipPlayer.volume = if (isMuted) 0f else 1.0f
    }

    if (isFullScreen && !fullScreenVideoUri.isNullOrEmpty()) {
        FullScreenVideoDialog(
            videoUri = fullScreenVideoUri,
            onDismiss = {
                isFullScreen = false
                pipPlayer.play()
            },
            onClose = onClose,
            button_text = button_text,
            link = link,
            pipStyling = pipStyling,
            onButtonClick = onButtonClick
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isInitialized) {
            Card(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(width = pipWidth, height = pipHeight)
                    .onGloballyPositioned { coordinates ->
                        pipSize = coordinates.size
                    }
                    .clickable {
                        if (!fullScreenVideoUri.isNullOrEmpty()) {
                            onExpandClick()
                            isFullScreen = true
                            pipPlayer.pause()
                        }
                    }
                    .then(
                        if (isMovable) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX = (offsetX + dragAmount.x).coerceIn(
                                        boundaryPaddingPx,
                                        screenWidth - pipSize.width - boundaryPaddingPx
                                    )
                                    offsetY = (offsetY + dragAmount.y).coerceIn(
                                        boundaryPaddingPx + topPaddingPx,
                                        screenHeight - pipSize.height - boundaryPaddingPx - bottomPaddingPx
                                    )
                                }
                            }
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box {
                    PipPlayerView(
                        exoPlayer = pipPlayer,
                        pipStyling = pipStyling,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(23.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { isMuted = !isMuted },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = if (isMuted) painterResource(R.drawable.mute) else painterResource(R.drawable.volume),
                            contentDescription = "Mute/Unmute",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if(!fullScreenVideoUri.isNullOrEmpty()){
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    if (!fullScreenVideoUri.isNullOrEmpty()) {
                                        onExpandClick()
                                        isFullScreen = true
                                        pipPlayer.pause()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.expand),
                                contentDescription = "Maximize",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(width = pipWidth, height = pipHeight)
                    .onGloballyPositioned { coordinates ->
                        pipSize = coordinates.size

                        if (position == "left") {
                            offsetX = boundaryPaddingPx
                        } else {
                            offsetX = screenWidth - pipSize.width - boundaryPaddingPx
                        }
                        offsetY = screenHeight - pipSize.height - boundaryPaddingPx - bottomPaddingPx
                        isInitialized = true
                    }
                    .alpha(0f)
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PipPlayerView(
    exoPlayer: ExoPlayer,
    pipStyling: PipStyling?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                useArtwork = false
                setKeepContentOnPlayerReset(true)
            }
        },
        modifier = modifier
    )
}

@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoDialog(
    videoUri: String,
    onDismiss: () -> Unit,
    button_text: String?,
    link: String?,
    pipStyling: PipStyling?,
    onClose: () -> Unit,
    onButtonClick: () -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val fullscreenPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(5000)
            .setLoadControl(DefaultLoadControl())
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
                repeatMode = Player.REPEAT_MODE_ALL
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                volume = if (isMuted) 0f else 1.0f
                prepare()
                play()
            }
    }

    LaunchedEffect(isMuted) {
        fullscreenPlayer.volume = if (isMuted) 0f else 1.0f
    }

    DisposableEffect(Unit) {
        onDispose {
            fullscreenPlayer.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
                ) {
                PipPlayerView(
                    exoPlayer = fullscreenPlayer,
                    pipStyling = pipStyling,
                    modifier = Modifier.fillMaxWidth(),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.minimize),
                        contentDescription = "Minimize",
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            painter = if (isMuted) painterResource(R.drawable.mute) else painterResource(R.drawable.volume),
                            contentDescription = "Mute/Unmute",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                if(!button_text.isNullOrEmpty() && !link.isNullOrEmpty()){

                    fun String?.toDp(): Dp = this?.toIntOrNull()?.dp ?: 0.dp

                    val paddingLeft = pipStyling?.marginLeft?.toDp()
                    val paddingRight = pipStyling?.marginRight?.toDp()
                    val paddingTop = pipStyling?.marginTop?.toDp()
                    val paddingBottom = pipStyling?.marginBottom?.toDp()

                    val buttonColor = try {
                        Color(android.graphics.Color.parseColor(pipStyling?.ctaButtonBackgroundColor ?: "#000000"))
                    } catch (e: Exception) {
                        Color.Black
                    }

                    val textColor = try {
                        Color(android.graphics.Color.parseColor(pipStyling?.ctaButtonTextColor ?: "#FFFFFF"))
                    } catch (e: Exception) {
                        Color.White
                    }

                    val context = LocalContext.current
                    val fontName = "Poppins"

                    Log.i("fontFamily", "$fontName")

                    val provider = GoogleFont.Provider(
                        providerAuthority = "com.google.android.gms.fonts",
                        providerPackage = "com.google.android.gms",
                        certificates = R.array.com_google_android_gms_fonts_certs
                    )

                    val googleFont = GoogleFont(fontName)

                    val fontFamily = FontFamily(
                        Font(googleFont, provider, FontWeight.Normal, FontStyle.Normal,)
                    )

                    Button(
                        onClick = {
                            uriHandler.openUri(link);
                            onButtonClick()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(
//                                top = paddingTop ?: 0.dp,
                                bottom = paddingBottom ?: 0.dp,
                                start = paddingLeft ?: 0.dp,
                                end = paddingRight ?: 0.dp
                            ).then(
                                if (pipStyling?.ctaFullWidth == true) {
                                    Modifier.fillMaxWidth()
                                } else {
                                    Modifier.width(pipStyling?.ctaWidth?.toDp() ?: 0.dp)
                                }
                            )
                            .height(pipStyling?.ctaHeight?.toDp() ?: 0.dp)
                        ,
                        shape = RoundedCornerShape(pipStyling?.cornerRadius?.toDp() ?: 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    ) {
                        Text(
                            fontFamily = fontFamily,
                            text = button_text.toString(),
                            color = textColor,
                            textAlign = TextAlign.Center
                            )
                    }
                }
            }
        }
    }
}