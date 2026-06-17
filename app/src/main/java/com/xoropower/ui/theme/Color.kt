package com.xoropower.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

//  xoropower — OFFICIAL DESIGN TOKENS (Imported from globals.css)
val XoroBlack       = Color(0xFF000000)
val XoroSurface     = Color(0xFF0D0D0D)
val XoroSurface2    = Color(0xFF141414)
val XoroSurface3    = Color(0xFF1C1C1C)
val XoroBlue        = Color(0xFF1E90FF)
val XoroBlueDark    = Color(0xFF1260CC)
val XoroWhite       = Color(0xFFFFFFFF)
val XoroRed         = Color(0xFFE53E3E)
val XoroRedDark     = Color(0xFFC53030)
val XoroMuted       = Color(0xFF666666)

// Colores específicos para niveles y efectos
val ElectricBlue    = Color(0xFF0055FF)
val GoldenYellow    = Color(0xFFFFD700)
val BrightRed       = Color(0xFFFF0033)

// Transparencias y Bordes
val XoroBorder      = Color(0xFFFFFFFF).copy(alpha = 0.08f)
val XoroBlueGlow    = Color(0xFF1E90FF).copy(alpha = 0.20f)

// Aliases para compatibilidad con pantallas existentes
val DarkGrey        = XoroBlack
val SurfaceWarm     = XoroSurface2
val DrawerBg        = XoroSurface
val NeonOrange      = XoroBlue   // Reasignado al azul original
val NeonPurple      = XoroRed    // Reasignado al rojo original
val CyanAccent      = XoroBlue
val WhitePure       = XoroWhite
val GreyText        = XoroMuted

//  GRADIENTES OFICIALES
val LlanuraGradient = Brush.verticalGradient(
    colors = listOf(XoroSurface, XoroBlack)
)
val BlueGradient = Brush.horizontalGradient(
    colors = listOf(XoroBlue, XoroBlueDark)
)
val RedGradient = Brush.horizontalGradient(
    colors = listOf(XoroRed, XoroRedDark)
)
