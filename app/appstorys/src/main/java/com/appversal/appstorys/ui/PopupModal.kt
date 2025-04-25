package com.appversal.appstorys.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.appversal.appstorys.R

@Composable
fun PopupModal(
    imageLink: String,
    onCloseClick: () -> Unit,
) {

    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(imageLink)
    val imageLoaded = painter.state is coil.compose.AsyncImagePainter.State.Success

    Dialog(
        onDismissRequest = onCloseClick,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        // Outer Box with dimmed background and center alignment
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    onCloseClick()
                },
            contentAlignment = Alignment.Center
        ) {
            // Container box that sizes to image
            Box(
                modifier = Modifier
                    .wrapContentSize()
            ) {
                // Image with border radius and optional max size
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(8.dp)
                        .clickable {
                            try {
                                val uri = Uri.parse("https://khatabook.com/")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Could not open link",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.modal),
                        contentDescription = "Popup Image",
                        modifier = Modifier
                            .size(width = 320.dp, height = 553.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                // Close button
//                if (imageLoaded){
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { onCloseClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
//                }
            }
        }
    }
}
