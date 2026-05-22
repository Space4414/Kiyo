package com.space4414.kiyo.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawBehindOutline(color: Color, cornerRadius: Dp = 16.dp, width: Dp = 1.dp): Modifier =
    this.border(width = width, color = color, shape = RoundedCornerShape(cornerRadius))
