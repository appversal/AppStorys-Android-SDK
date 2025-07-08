package com.appversal.appstorys.ui

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.appversal.appstorys.utils.isGifUrl

@Composable
internal fun OverlayFloater(
    modifier: Modifier,
    image: String,
    lottieUrl: String?,
    height: Dp,
    width: Dp,
    borderRadiusValues: RoundedCornerShape,
    onClick: () -> Unit
) {
    val url =
        image.ifEmpty { "https://gratisography.com/wp-content/uploads/2024/11/gratisography-augmented-reality-800x525.jpg" }
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(url)
        .memoryCacheKey(url)
        .diskCacheKey(url)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()

    Surface (
        modifier = modifier
            .padding(16.dp)
            .height(height)
            .width(width)
            .clip(borderRadiusValues)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        color = Color.Transparent,
        shape = borderRadiusValues
    ) {
        when {
            !lottieUrl.isNullOrEmpty() -> {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.Url(lottieUrl)
                )
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .height(height ?: Dp.Unspecified)
                        .width(width ?: Dp.Unspecified)
                )
            }

            !image.isNullOrEmpty() -> {
                if (isGifUrl(image)) {
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
                            .data(image)
                            .memoryCacheKey(image)
                            .diskCacheKey(image)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .apply { size(coil.size.Size.ORIGINAL) }
                            .build(),
                        imageLoader = imageLoader
                    )

                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(height ?: Dp.Unspecified)
                            .width(width ?: Dp.Unspecified)
                    )
                } else {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = modifier
                            .fillMaxSize()
                            .clip(borderRadiusValues)
                    )
                }
            }
        }
    }
}