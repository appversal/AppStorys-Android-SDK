package com.appversal.appstorys.utils

import androidx.compose.ui.geometry.Rect

data class AppStorysCoordinates(
    val x: Float,
    val y: Float,
    val width: Int,
    val height: Int,
    val boundsInParent: () -> Rect,
    val boundsInRoot: () -> Rect,
    val boundsInWindow: () -> Rect
)
