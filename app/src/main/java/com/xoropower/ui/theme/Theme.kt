package com.xoropower.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val xoropowerColors = darkColorScheme(
    primary         = NeonOrange,
    secondary       = NeonPurple,
    background      = DarkGrey,
    surface         = SurfaceWarm,       // Tarjetas con tono cálido
    onPrimary       = WhitePure,
    onSurface       = WhitePure,
    onBackground    = WhitePure,
)

//  TEMA GLOBAL 
@Composable
fun xoropowerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = xoropowerColors,
        content = content
    )
}
