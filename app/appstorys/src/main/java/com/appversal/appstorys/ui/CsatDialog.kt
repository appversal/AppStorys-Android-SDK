package com.appversal.appstorys.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appversal.appstorys.api.CSATDetails
import com.appversal.appstorys.api.CSATStyling
import com.appversal.appstorys.utils.toColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CsatFeedback(
    val rating: Int,
    val feedbackOption: String? = null,
    val additionalComments: String = ""
)

@Composable
internal fun CsatDialog(
    onDismiss: () -> Unit,
    onSubmitFeedback: (CsatFeedback) -> Unit,
    csatDetails: CSATDetails
) {
    val localContent: Map<String, String> = remember {
        mapOf(
            "title" to (csatDetails.title?.takeIf { it.isNotEmpty() } ?: "We'd love your feedback!"),
            "description" to (csatDetails.descriptionText?.takeIf { it.isNotEmpty() } ?: "This will help us improve your experience"),
            "thankyouText" to (csatDetails.thankyouText?.takeIf { it.isNotEmpty() } ?: "Thank you for your feedback!"),
            "thankyouDescription" to (csatDetails.thankyouDescription?.takeIf { it.isNotEmpty() } ?: "We appreciate you taking the time to share your thoughts."),
            "feedbackPrompt" to "Please tell us what went wrong."
        )
    }

    val styling = remember {
        mapOf(
            "csatBackgroundColor" to csatDetails.styling?.csatBackgroundColor.toColor(Color.White),
            "csatTitleColor" to csatDetails.styling?.csatTitleColor.toColor(Color.Black),
            "csatDescriptionTextColor" to csatDetails.styling?.csatDescriptionTextColor.toColor(Color(0xFF504F58)),
            "csatCtaBackgroundColor" to csatDetails.styling?.csatCtaBackgroundColor.toColor(Color(0xFF007AFF)),
            "csatCtaTextColor" to csatDetails.styling?.csatCtaTextColor.toColor(Color.White),
            "csatSelectedOptionBackgroundColor" to csatDetails.styling?.csatSelectedOptionBackgroundColor.toColor(Color(0xFFE3F2FD)),
            "csatOptionStrokeColor" to csatDetails.styling?.csatOptionStrokeColor.toColor(Color(0xFFCCCCCC)),
            "csatSelectedOptionTextColor" to csatDetails.styling?.csatSelectedOptionTextColor.toColor(Color(0xFF007AFF)),
            "csatOptionTextColor" to csatDetails.styling?.csatOptionTextColour.toColor(Color.Black),
            "csatLowStarColor" to csatDetails.styling?.csatLowStarColor.toColor(Color.Black),
            "csatHighStarColor" to csatDetails.styling?.csatHighStarColor.toColor(Color.Black),
            "csatUnselectedStarColor" to csatDetails.styling?.csatUnselectedStarColor.toColor(Color.Black),
            "csatAdditionalTextColor" to csatDetails.styling?.csatAdditionalTextColor.toColor(Color.Black)
        )
    }

    val feedbackOptions = remember {
        if (csatDetails.feedbackOption?.toList()?.isNotEmpty() == true){
            csatDetails.feedbackOption.toList()
        }else{
            listOf(
                "Poor UI/UX",
                "App Performance",
                "Missing Features",
                "Other Issues"
            )
        }
    }

    var selectedStars by remember { mutableStateOf(0) }
    var showThanks by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var additionalComments by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = styling["csatBackgroundColor"]!!,
        shadowElevation = 8.dp
    ) {
        Box {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = styling["csatTitleColor"]!!
                )
            }

            AnimatedVisibility(
                visible = !showThanks,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MainContent(
                    localContent = localContent,
                    styling = styling,
                    selectedStars = selectedStars,
                    showFeedback = showFeedback,
                    feedbackOptions = feedbackOptions,
                    selectedOption = selectedOption,
                    additionalComments = additionalComments,
                    onStarSelected = { stars ->
                        selectedStars = stars
                        when {
                            stars >= 4 -> {
                                scope.launch {
                                    delay(1000)
                                    onSubmitFeedback(CsatFeedback(rating = stars))
                                    showThanks = true
                                }
                            }
                            else -> showFeedback = true
                        }
                    },
                    onOptionSelected = { selectedOption = it },
                    onCommentsChanged = { additionalComments = it },
                    onSubmit = {
                        onSubmitFeedback(
                            CsatFeedback(
                                rating = selectedStars,
                                feedbackOption = selectedOption,
                                additionalComments = additionalComments
                            )
                        )
                        showThanks = true
                    }
                )
            }

            if (csatDetails.thankyouImage != null){
                AnimatedVisibility(
                    visible = showThanks,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ThankYouContent(
                        localContent = localContent,
                        styling = styling,
                        onDone = onDismiss,
                        image = csatDetails.thankyouImage,
                        csatDetails = csatDetails
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    localContent: Map<String, String>,
    styling: Map<String, Color>,
    selectedStars: Int,
    showFeedback: Boolean,
    feedbackOptions: List<String>,
    selectedOption: String?,
    additionalComments: String,
    onStarSelected: (Int) -> Unit,
    onOptionSelected: (String) -> Unit,
    onCommentsChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
    ) {
        Text(
            text = localContent["title"]!!,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = styling["csatTitleColor"]!!
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = localContent["description"]!!,
            color = styling["csatDescriptionTextColor"]!!
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            repeat(5) { index ->
                val starColor = when {
                    index >= selectedStars -> styling["csatUnselectedStarColor"]!!
                    selectedStars >= 4 -> styling["csatHighStarColor"]!!
                    else -> styling["csatLowStarColor"]!!
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star ${index + 1}",
                    tint = starColor,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onStarSelected(index + 1) }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        AnimatedVisibility(visible = showFeedback) {
            FeedbackContent(
                localContent = localContent,
                styling = styling,
                feedbackOptions = feedbackOptions,
                selectedOption = selectedOption,
                additionalComments = additionalComments,
                onOptionSelected = onOptionSelected,
                onCommentsChanged = onCommentsChanged,
                onSubmit = onSubmit
            )
        }
    }
}

@Composable
private fun FeedbackContent(
    localContent: Map<String, String>,
    styling: Map<String, Color>,
    feedbackOptions: List<String>,
    selectedOption: String?,
    additionalComments: String,
    onOptionSelected: (String) -> Unit,
    onCommentsChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = localContent["feedbackPrompt"]!!,
            color = styling["csatTitleColor"]!!
        )

        Spacer(modifier = Modifier.height(12.dp))

        feedbackOptions.forEach { option ->
            val isSelected = option == selectedOption
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = 1.dp,
                        color = styling["csatOptionStrokeColor"]!!,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { onOptionSelected(option) },
                color = if (isSelected) styling["csatSelectedOptionBackgroundColor"]!!
                else styling["csatBackgroundColor"]!!,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(12.dp),
                    color = if (isSelected) styling["csatSelectedOptionTextColor"]!!
                    else styling["csatOptionTextColor"]!!
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = additionalComments,
            onValueChange = onCommentsChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Additional comments", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = styling["csatAdditionalTextColor"]!!,
                unfocusedTextColor = Color.Black,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = styling["csatCtaBackgroundColor"]!!
            )
        ) {
            Text(
                text = "Submit",
                color = styling["csatCtaTextColor"]!!
            )
        }
    }
}

@Composable
private fun ThankYouContent(
    localContent: Map<String, String>,
    styling: Map<String, Color>,
    image: String,
    csatDetails: CSATDetails,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = image.ifEmpty { "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTwlQ-xYqAIcjylz3NUGJ_jcdRmdzk_vMae0w&s"  },
            contentDescription = "Thank you",
            modifier = Modifier.size(66.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = localContent["thankyouText"]!!,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = styling["csatTitleColor"]!!
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = localContent["thankyouDescription"]!!,
            color = styling["csatDescriptionTextColor"]!!,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if(csatDetails.link.isNullOrEmpty()){
                    onDone()
                } else {
                    try {
                        val uri = Uri.parse(csatDetails.link)
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
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = styling["csatCtaBackgroundColor"]!!
            )
        ) {
            Text(
                text = "Done",
                color = styling["csatCtaTextColor"]!!
            )
        }
    }
}