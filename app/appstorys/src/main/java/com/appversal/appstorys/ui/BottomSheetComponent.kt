package com.appversal.appstorys.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.appversal.appstorys.R
import com.appversal.appstorys.api.BottomSheetDetails
import com.appversal.appstorys.api.BottomSheetElement
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.util.Log


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BottomSheetComponent(
    onClick: (String?) -> Unit = { _ -> },
    onDismissRequest: () -> Unit,
    bottomSheetDetails: BottomSheetDetails,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            sheetState.expand()
        }
    }

    val backgroundColor = Color.Transparent

    val cornerRadius = bottomSheetDetails.cornerRadius?.toFloatOrNull()?.dp ?: 16.dp

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
        containerColor = backgroundColor,
        dragHandle = {},
        sheetState = sheetState
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val sortedElements = bottomSheetDetails.elements?.sortedBy { it.order } ?: emptyList()

            val imageElement = sortedElements.firstOrNull { it.type == "image" }
            val bodyElements = sortedElements.filter { it.type == "body" }
            val ctaElements = sortedElements.filter { it.type == "cta" }

            if (imageElement?.overlayButton == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ImageElement(imageElement, onClick = onClick)

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        bodyElements.forEach { BodyElement(it) }

                        val leftCTA = ctaElements.firstOrNull { it.position == "left" }
                        val rightCTA = ctaElements.firstOrNull { it.position == "right" }
                        val centerCTAs =
                            ctaElements.filter { it.position == "center" || it.position.isNullOrEmpty() }

                        if (leftCTA != null || rightCTA != null) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (leftCTA != null) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CTAElement(leftCTA) { onClick(leftCTA.ctaLink) }
                                    }
                                } else Spacer(modifier = Modifier.weight(1f))

                                if (rightCTA != null) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        CTAElement(rightCTA) { onClick(rightCTA.ctaLink) }
                                    }
                                } else Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        centerCTAs.forEach { cta ->
                            CTAElement(cta) { onClick(cta.ctaLink) }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    imageElement?.let {
                        ImageElement(it, onClick = onClick)
                    }

                    bodyElements.forEach { BodyElement(it) }

                    val leftCTA = ctaElements.firstOrNull { it.position == "left" }
                    val rightCTA = ctaElements.firstOrNull { it.position == "right" }
                    val centerCTAs =
                        ctaElements.filter { it.position == "center" || it.position.isNullOrEmpty() }

                    if (leftCTA != null || rightCTA != null) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (leftCTA != null) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CTAElement(leftCTA) { onClick(leftCTA.ctaLink) }
                                }
                            } else Spacer(modifier = Modifier.weight(1f))

                            if (rightCTA != null) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CTAElement(rightCTA) { onClick(rightCTA.ctaLink) }
                                }
                            } else Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    centerCTAs.forEach { cta ->
                        CTAElement(cta) { onClick(cta.ctaLink) }
                    }
                }
            }

            if (bottomSheetDetails.enableCrossButton == "true") {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0x4D000000), shape = CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                    )
                }
            }

        }
    }
}

@Composable
private fun ImageElement(element: BottomSheetElement, onClick: (String?) -> Unit = { _ -> }) {
    val paddingLeft = element.paddingLeft?.dp ?: 0.dp
    val paddingRight = element.paddingRight?.dp ?: 0.dp
    val paddingTop = element.paddingTop?.dp ?: 0.dp
    val paddingBottom = element.paddingBottom?.dp ?: 0.dp

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = paddingLeft,
                end = paddingRight,
                top = paddingTop,
                bottom = paddingBottom
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick(element.imageLink)
            },
        contentAlignment = when (element.alignment) {
            "left" -> Alignment.CenterStart
            "right" -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Image(
            painter = rememberAsyncImagePainter(element.url),
            contentDescription = "Image",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
private fun BodyElement(element: BottomSheetElement) {
    val paddingLeft = element.paddingLeft?.dp ?: 0.dp
    val paddingRight = element.paddingRight?.dp ?: 0.dp
    val paddingTop = element.paddingTop?.dp ?: 0.dp
    val paddingBottom = element.paddingBottom?.dp ?: 0.dp

    val alignment = when (element.alignment) {
        "left" -> Alignment.Start
        "right" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val textAlign = when (element.alignment) {
        "left" -> TextAlign.Start
        "right" -> TextAlign.End
        else -> TextAlign.Center
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(
                    android.graphics.Color.parseColor(
                        element.bodyBackgroundColor ?: "#FFFFFF"
                    )
                )
            )
            .padding(
                start = paddingLeft,
                end = paddingRight,
                top = paddingTop,
                bottom = paddingBottom
            ),
        horizontalAlignment = alignment
    ) {
        element.titleText?.let { title ->

            if (element.titleText.isNotBlank()) {
                val titleColor = try {
                    Color(
                        android.graphics.Color.parseColor(
                            element.titleFontStyle?.colour ?: "#000000"
                        )
                    )
                } catch (e: Exception) {
                    Color.Black
                }

                val decoration = element.titleFontStyle?.decoration.orEmpty()

                val titleFontWeight =
                    if (decoration.contains("bold")) FontWeight.Bold else FontWeight.Normal
                val titleFontStyle =
                    if (decoration.contains("italic")) FontStyle.Italic else FontStyle.Normal
                val titleTextDecoration =
                    if (decoration.contains("underline")) TextDecoration.Underline else null

                Text(
                    text = title,
                    color = titleColor,
                    fontSize = (element.titleFontSize ?: 16).sp,
                    fontWeight = titleFontWeight,
                    fontStyle = titleFontStyle,
                    textDecoration = titleTextDecoration,
                    textAlign = textAlign,
                    lineHeight = ((element.titleLineHeight ?: 1f) * (element.titleFontSize
                        ?: 16)).sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height((element.spacingBetweenTitleDesc?.toInt() ?: 0).dp))

        element.descriptionText?.let { description ->
            val descriptionColor = try {
                Color(
                    android.graphics.Color.parseColor(
                        element.descriptionFontStyle?.colour ?: "#000000"
                    )
                )
            } catch (e: Exception) {
                Color.Black
            }

            val decoration = element.descriptionFontStyle?.decoration.orEmpty()

            val descriptionFontWeight =
                if (decoration.contains("bold")) FontWeight.Bold else FontWeight.Normal
            val descriptionFontStyle =
                if (decoration.contains("italic")) FontStyle.Italic else FontStyle.Normal
            val descriptionTextDecoration =
                if (decoration.contains("underline")) TextDecoration.Underline else null

            Text(
                text = description,
                color = descriptionColor,
                fontSize = (element.descriptionFontSize ?: 14).sp,
                fontWeight = descriptionFontWeight,
                fontStyle = descriptionFontStyle,
                textDecoration = descriptionTextDecoration,
                textAlign = textAlign,
                lineHeight = ((element.descriptionLineHeight ?: 1f) * (element.descriptionFontSize
                    ?: 14)).sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CTAElement(element: BottomSheetElement, onClick: () -> Unit = {}) {
    val paddingLeft = element.paddingLeft?.dp ?: 0.dp
    val paddingRight = element.paddingRight?.dp ?: 0.dp
    val paddingTop = element.paddingTop?.dp ?: 0.dp
    val paddingBottom = element.paddingBottom?.dp ?: 0.dp

    val buttonColor = try {
        Color(android.graphics.Color.parseColor(element.ctaBoxColor ?: "#000000"))
    } catch (e: Exception) {
        Color.Black
    }

    val textColor = try {
        Color(android.graphics.Color.parseColor(element.ctaTextColour ?: "#FFFFFF"))
    } catch (e: Exception) {
        Color.White
    }

    val borderRadius = element.ctaBorderRadius?.dp ?: 5.dp
    val buttonHeight = element.ctaHeight?.dp ?: 50.dp
    val buttonWidth = element.ctaWidth?.dp ?: 100.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(
                    android.graphics.Color.parseColor(
                        element.ctaBackgroundColor ?: "#FFFFFF"
                    )
                )
            )
            .padding(
                start = paddingLeft,
                end = paddingRight,
                top = paddingTop,
                bottom = paddingBottom
            ),
        contentAlignment = when (element.alignment) {
            "left" -> Alignment.CenterStart
            "right" -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(borderRadius),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            modifier = Modifier
                .height(buttonHeight)
                .then(
                    if (element.ctaFullWidth == true) Modifier.fillMaxWidth()
                    else Modifier.width(buttonWidth)
                )
        ) {
            val decoration = element.ctaFontDecoration.orEmpty()

            val ctaFontWeight = if (decoration.contains("bold")) FontWeight.Bold else FontWeight.Normal
            val ctaFontStyle = if (decoration.contains("italic")) FontStyle.Italic else FontStyle.Normal
            val ctaTextDecoration =
                if (decoration.contains("underline")) TextDecoration.Underline else null

            val fontName = element.ctaFontFamily ?: "Poppins"

            Log.i("fontFamily", fontName)

            val fontFamily = try {
                val provider = GoogleFont.Provider(
                    providerAuthority = "com.google.android.gms.fonts",
                    providerPackage = "com.google.android.gms",
                    certificates = R.array.com_google_android_gms_fonts_certs
                )
                val googleFont = GoogleFont(fontName)
                FontFamily(
                    Font(
                        googleFont = googleFont,
                        fontProvider = provider,
                        weight = FontWeight.Normal,
                        style = FontStyle.Normal
                    )
                ).also {
                    Log.i("fontFamily", "FontFamily created successfully")
                }
            } catch (e: Exception) {
                Log.e("fontFamily", "Failed to load font: $fontName", e)
                FontFamily.Default
            }

            Text(
                text = element.ctaText ?: "Click",
                color = textColor,
                fontFamily = fontFamily,
                fontSize = (element.ctaFontSize?.toFloatOrNull() ?: 14f).sp,
                fontWeight = ctaFontWeight,
                fontStyle = ctaFontStyle,
                textDecoration = ctaTextDecoration
            )
        }
    }
}