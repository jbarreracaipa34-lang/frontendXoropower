package com.xoropower.data

import androidx.compose.ui.graphics.Color
import com.xoropower.ui.screens.RhythmNote

/**
 * Datos locales de ejercicios para cuando no hay conexión al backend
 * o para los ejercicios de mano individual que aún no están en la BD.
 *
 * Tempo: 80 BPM → 1 beat = 750 ms
 * Compás: 4/4 → 4 beats por compás
 * Duración por compás: 3000 ms
 */
object EjerciciosData {

    private const val BPM = 80
    private const val MS_PER_BEAT = 60_000L / BPM  // 750ms

    // ── COLORES ──
    private val ROJO = Color(0xFFF44336)   // Mano Derecha
    private val AZUL = Color(0xFF2196F3)   // Mano Izquierda

    // ── IDs locales (se usan como fallback si el backend no tiene estos ejercicios) ──
    const val ID_EJERCICIO_DERECHA   = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    const val ID_EJERCICIO_IZQUIERDA = "b2c3d4e5-f6a7-8901-bcde-f23456789012"
    const val ID_EJERCICIO_JUNTAS    = "7ab3c673-201a-45b6-85a3-e1c813b8fef4" // El que ya existe en el backend

    const val ID_INTERMEDIO_1        = "00000000-0000-0000-0000-000000000030"
    const val ID_INTERMEDIO_2        = "00000000-0000-0000-0000-000000000040"
    const val ID_INTERMEDIO_3        = "00000000-0000-0000-0000-000000000070"
    const val ID_AVANZADO_1          = "00000000-0000-0000-0000-000000000050"
    const val ID_AVANZADO_2          = "00000000-0000-0000-0000-000000000060"
    const val ID_AVANZADO_3          = "00000000-0000-0000-0000-000000000080"

}
