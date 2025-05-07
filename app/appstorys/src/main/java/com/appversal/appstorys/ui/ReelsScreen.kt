package com.appversal.appstorys.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.appversal.appstorys.api.Reel
import com.appversal.appstorys.api.ReelsDetails
import org.json.JSONArray
import java.io.File

@Composable
internal fun ReelsRow(
    modifier: Modifier,
    reels: List<Reel>,
    onReelClick: (Int) -> Unit,
    height: Dp,
    width: Dp,
    cornerRadius: Dp,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(reels.size) { index ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .padding(end = 10.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable { onReelClick(index) }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(reels[index].thumbnail),
                    contentDescription = "Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

}

@Composable
internal fun FullScreenVideoScreen(
    reelsDetails: ReelsDetails,
    reels: List<Reel>,
    likedReels: List<String>,
    startIndex: Int,
    onBack: () -> Unit,
    sendLikesStatus: (Pair<Reel, String>) -> Unit,
    sendEvents: (Pair<Reel, String>) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { reels.size })
    val context = LocalContext.current

    val likesState = remember {
        mutableStateMapOf<String, Int>().apply {
            reels.forEach { reel ->
                reel.id?.let { this[it] = reel.likes ?: 0 }
            }
        }
    }

    LaunchedEffect(Unit) {
        likedReels.forEach {
            likesState[it] = likesState[it]?.plus(1) ?: 1
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        sendEvents(Pair(reels[pagerState.currentPage], "IMP"))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            beyondViewportPageCount = 20
        ) { page ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                if (pagerState.currentPage == page) {
                    reels[page].video?.let {
                        VideoPlayer(
                            url = it,
                            isPlaying = pagerState.currentPage == page
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(20f))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    if (!likedReels.contains(reels[page].id)) {
                                        reels[page].id?.let{
                                            likesState[it] = likesState[reels[page].id]?.plus(1) ?: 1
                                        }

                                    } else {
                                        reels[page].id?.let{
                                            likesState[it] = likesState[reels[page].id]?.minus(1) ?: 1
                                        }
                                    }

                                    sendLikesStatus(
                                        Pair(
                                            reels[page],
                                            if (!likedReels.contains(reels[page].id)) "like" else "unlike"
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                                    contentDescription = "Like",
                                    tint = if (likedReels.contains(reels[page].id)) Color(android.graphics.Color.parseColor(reelsDetails.styling?.likeButtonColor)) else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = likesState[reels[page].id].toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (!reels[page].link.isNullOrEmpty()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        val sendIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(
                                                android.content.Intent.EXTRA_TEXT,
                                                reels[page].link
                                            )
                                            type = "text/plain"
                                        }
                                        val shareIntent =
                                            android.content.Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Share",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0f, 0f, 0f, 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            if (!reels[page].descriptionText.isNullOrEmpty()) {
                                Text(
                                    text = reels[page].descriptionText ?: "",
                                    color = Color(android.graphics.Color.parseColor(reelsDetails.styling?.descriptionTextColor)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!reels[page].link.isNullOrEmpty() && !reels[page].buttonText.isNullOrEmpty()) {
                                Button(
                                    onClick = {
                                        sendEvents(Pair(reels[page], "CLK"))
                                        try {
                                            val uri = Uri.parse(reels[page].link)
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                uri
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Could not open link",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(android.graphics.Color.parseColor(reelsDetails.styling?.ctaBoxColor))
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = reels[page].buttonText ?: "",
                                        color = Color(android.graphics.Color.parseColor(reelsDetails.styling?.ctaTextColor)),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.fillMaxHeight(0.02f))
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = { onBack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    url: String,
    isPlaying: Boolean
) {
    val context = LocalContext.current

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (!initialized) {
            val mediaItem = MediaItem.fromUri(url.toUri())
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            initialized = true
        }
        exoPlayer.playWhenReady = isPlaying
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }


    if (isPlaying) {

        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    }
}

internal fun saveLikedReels(idList: List<String>, sharedPreferences: SharedPreferences) {
    val jsonArray = JSONArray(idList)
    sharedPreferences.edit().putString("LIKED_REELS", jsonArray.toString()).apply()
}

internal fun getLikedReels(sharedPreferences: SharedPreferences): List<String> {
    val jsonString = sharedPreferences.getString("LIKED_REELS", "[]") ?: "[]"
    val jsonArray = JSONArray(jsonString)
    return List(jsonArray.length()) { jsonArray.getString(it) }
}

@Composable
fun getScreenHeight(): Int {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp
}
