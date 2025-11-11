package com.appversal.appstorys.ui

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScratch(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onConfettiTrigger: () -> Unit,
    wasFullyScratched: Boolean,
    onWasFullyScratched: (Boolean) -> Unit,
    gpayImageUrl: String = "", // Network URL for overlay
    bannerImageUrl: String = "" // Network URL for banner
) {
    var points by remember { mutableStateOf(listOf<Offset>()) }
    var touchedCells by remember { mutableStateOf(setOf<Int>()) }
    var isRevealed by remember { mutableStateOf(wasFullyScratched) }
    var showTerms by remember { mutableStateOf(false) }

    // Tuning parameters
    val gridCols = 20
    val gridRows = 20
    val revealThreshold = 0.1f

    // Card size (adaptive, capped)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardSize = min(screenWidth.value * 0.9f, 260f).dp

    LaunchedEffect(wasFullyScratched) {
        if (wasFullyScratched) {
            isRevealed = true
        }
    }

    if (isPresented) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    this@Column.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scratch card
                Box(
                    modifier = Modifier
                        .size(cardSize)
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    ScratchableCard(
                        cardSize = cardSize,
                        points = points,
                        isRevealed = isRevealed,
                        gpayImageUrl = gpayImageUrl,
                        bannerImageUrl = bannerImageUrl,
                        onPointsChanged = { newPoints ->
                            if (!isRevealed) {
                                points = newPoints
                            }
                        },
                        onCellTouched = { cellIndex ->
                            if (!isRevealed) {
                                touchedCells = touchedCells + cellIndex
                                val total = gridCols * gridRows
                                if (touchedCells.size.toFloat() / total >= revealThreshold) {
                                    isRevealed = true
                                    onWasFullyScratched(true)
                                    points = emptyList()
                                    onConfettiTrigger()
                                }
                            }
                        },
                        gridCols = gridCols,
                        gridRows = gridRows
                    )
                }

                // Action buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 96.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    this@Column.AnimatedVisibility(
                        visible = isRevealed,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { /* Claim action */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                )
                            ) {
                                Text(
                                    text = "Claim offer now",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                                Text(
                                    text = "Terms & Conditions*",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable {
                                            showTerms = true
                                        }
                                )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Terms and conditions bottom sheet
        if (showTerms) {
            ModalBottomSheet(
                onDismissRequest = { showTerms = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                dragHandle = null,
            ) {
                TermsAndConditionsView(onDismiss = { showTerms = false })
            }
        }
    }
}

@Composable
fun ScratchableCard(
    cardSize: androidx.compose.ui.unit.Dp,
    points: List<Offset>,
    isRevealed: Boolean,
    gpayImageUrl: String,
    bannerImageUrl: String,
    onPointsChanged: (List<Offset>) -> Unit,
    onCellTouched: (Int) -> Unit,
    gridCols: Int,
    gridRows: Int
) {
    val cardSizePx = with(LocalDensity.current) { cardSize.toPx() }
    val context = LocalContext.current

    Box(modifier = Modifier.size(cardSize)) {
        // Background content (revealed)
        CashBackInfoView(
            modifier = Modifier.size(cardSize),
            bannerImageUrl = bannerImageUrl
        )

        // Scratch overlay
        if (!isRevealed) {
            Box(
                modifier = Modifier
                    .size(cardSize)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val clampedOffset = Offset(
                                    offset.x.coerceIn(0f, cardSizePx),
                                    offset.y.coerceIn(0f, cardSizePx)
                                )
                                onPointsChanged(points + clampedOffset)
                                val cellIndex = cellIndexFor(
                                    clampedOffset,
                                    cardSizePx,
                                    gridCols,
                                    gridRows
                                )
                                onCellTouched(cellIndex)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val clampedOffset = Offset(
                                    change.position.x.coerceIn(0f, cardSizePx),
                                    change.position.y.coerceIn(0f, cardSizePx)
                                )
                                onPointsChanged(points + clampedOffset)
                                val cellIndex = cellIndexFor(
                                    clampedOffset,
                                    cardSizePx,
                                    gridCols,
                                    gridRows
                                )
                                onCellTouched(cellIndex)
                            }
                        )
                    }
            ) {
                // Overlay image (gpay) with scratch effect
                if (gpayImageUrl.isNotEmpty()) {

                        SubcomposeAsyncImage(
                            model = gpayImageUrl,
                            contentDescription = "Scratch overlay",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(cardSize)
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                                .drawWithContent {
                                    drawContent()

                                    // Create scratch path
                                    if (points.isNotEmpty()) {
                                        val path = Path().apply {
                                            moveTo(points.first().x, points.first().y)
                                            points.forEach { point ->
                                                lineTo(point.x, point.y)
                                            }
                                        }

                                        drawPath(
                                            path = path,
                                            color = Color.Black,
                                            style = Stroke(
                                                width = 50f,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            ),
                                            blendMode = BlendMode.Clear
                                        )
                                    }
                                }

                        )
                } else {
                    // Fallback colored overlay if no image provided
                    Canvas(
                        modifier = Modifier
                            .size(cardSize)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                    ) {
                        // Draw the scratch overlay background
                        drawRect(Color(0xFF282828))

                        // Create scratch path
                        if (points.isNotEmpty()) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.forEach { point ->
                                    lineTo(point.x, point.y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(
                                    width = 50f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                ),
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashBackInfoView(
    modifier: Modifier = Modifier,
    bannerImageUrl: String
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .background(Color(0xFF141414))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Network image for banner - Using same pattern as PinnedBanner
            if (bannerImageUrl.isNotEmpty()) {
                if (isGifUrl(bannerImageUrl)) {
                    val imageLoader = ImageLoader.Builder(context)
                        .components {
                            if (SDK_INT >= 28) {
                                add(ImageDecoderDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }
                        .build()

                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(bannerImageUrl)
                            .memoryCacheKey(bannerImageUrl)
                            .diskCacheKey(bannerImageUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .apply { size(coil.size.Size.ORIGINAL) }
                            .build(),
                        imageLoader = imageLoader
                    )

                    Image(
                        painter = painter,
                        contentDescription = "Banner",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .padding(24.dp)
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = bannerImageUrl,
                        contentDescription = "Banner",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(shape = CircleShape)
                            .padding(18.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Offer from AppStorys",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Cashback on mobile and recharge",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TermsAndConditionsView(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TermSection(
                title = "Eligibility:",
                content = "Only genuine users who meet the campaign criteria (as defined by the brand/platform) are eligible to participate in the Scratch CRC program."
            )

            TermSection(
                title = "Non-Transferable & One-Time Use:",
                content = "Each scratch code/reward is unique, valid for a single use, and cannot be transferred, exchanged, or redeemed for cash unless explicitly stated."
            )

            TermSection(
                title = "Fraud Prevention:",
                content = "Any misuse, duplication, unauthorized distribution, or suspicious activity related to the scratch code will result in immediate disqualification and potential blocking of the user/account."
            )

            TermSection(
                title = "Validity & Expiry:",
                content = "All scratch cards/codes must be redeemed within the specified validity period. Expired or tampered codes will not be accepted under any circumstances."
            )

            TermSection(
                title = "Brand's Final Authority:",
                content = "The company reserves the right to modify, suspend, or terminate the scratch campaign at any time. All decisions made by the company regarding rewards, eligibility, and disputes will be final and binding."
            )
        }

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-60).dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TermSection(title: String, content: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// Helper function to map point to grid cell index
private fun cellIndexFor(
    point: Offset,
    size: Float,
    gridCols: Int,
    gridRows: Int
): Int {
    val x = point.x.coerceIn(0f, size)
    val y = point.y.coerceIn(0f, size)

    val col = ((x / size) * gridCols).toInt().coerceIn(0, gridCols - 1)
    val row = ((y / size) * gridRows).toInt().coerceIn(0, gridRows - 1)

    return row * gridCols + col
}

// Helper function to check if URL is a GIF
private fun isGifUrl(url: String): Boolean {
    return url.lowercase().endsWith(".gif")
}