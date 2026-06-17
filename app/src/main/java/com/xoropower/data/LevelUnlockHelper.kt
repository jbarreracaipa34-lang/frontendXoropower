package com.xoropower.data

import com.xoropower.data.network.EjercicioRitmoDto

/**
 * Controla qué niveles y ejercicios están desbloqueados según el progreso del estudiante.
 * Los administradores tienen acceso completo sin restricciones.
 */
object LevelUnlockHelper {

    fun isLevelUnlocked(nivel: String, sessionManager: SessionManager, ejercicios: List<EjercicioRitmoDto> = emptyList()): Boolean {
        if (sessionManager.isAdmin()) return true
        return when (nivel) {
            "basico" -> true
            "intermedio" -> isNivelBasicoCompleto(sessionManager, ejercicios)
            "avanzado" -> isNivelIntermedioCompleto(sessionManager, ejercicios)
            else -> false
        }
    }

    fun isExerciseUnlocked(nivel: String, exerciseIndex: Int, sessionManager: SessionManager, ejercicios: List<EjercicioRitmoDto> = emptyList()): Boolean {
        if (sessionManager.isAdmin()) return true
        if (!isLevelUnlocked(nivel, sessionManager, ejercicios)) return false
        if (exerciseIndex == 0) return true

        val nivelEjercicios = ejercicios.filter { it.nivel == nivel }
        val previousId = if (nivelEjercicios.isNotEmpty()) {
            nivelEjercicios.getOrNull(exerciseIndex - 1)?.id
        } else {
            exerciseIdsForLevel(nivel).getOrNull(exerciseIndex - 1)
        } ?: return false

        return sessionManager.getExerciseProgress(previousId).completado
    }

    fun exerciseIdsForLevel(nivel: String): List<String> = when (nivel) {
        "basico" -> listOf(
            EjerciciosData.ID_EJERCICIO_DERECHA,
            EjerciciosData.ID_EJERCICIO_IZQUIERDA,
            EjerciciosData.ID_EJERCICIO_JUNTAS
        )
        "intermedio" -> listOf(
            EjerciciosData.ID_INTERMEDIO_1,
            EjerciciosData.ID_INTERMEDIO_2,
            EjerciciosData.ID_INTERMEDIO_3
        )
        "avanzado" -> listOf(
            EjerciciosData.ID_AVANZADO_1,
            EjerciciosData.ID_AVANZADO_2,
            EjerciciosData.ID_AVANZADO_3
        )
        else -> emptyList()
    }

    fun unlockHintForLevel(nivel: String): String? = when (nivel) {
        "intermedio" -> "Completa los 3 ejercicios del nivel Básico para desbloquear"
        "avanzado" -> "Completa los ejercicios del nivel Intermedio para desbloquear"
        else -> null
    }

    private fun isNivelBasicoCompleto(sessionManager: SessionManager, ejercicios: List<EjercicioRitmoDto>): Boolean {
        val basicos = ejercicios.filter { it.nivel == "basico" }
        val ids = if (basicos.isNotEmpty()) basicos.map { it.id } else exerciseIdsForLevel("basico")
        return ids.all { id -> sessionManager.getExerciseProgress(id).completado }
    }

    private fun isNivelIntermedioCompleto(sessionManager: SessionManager, ejercicios: List<EjercicioRitmoDto>): Boolean {
        val intermedios = ejercicios.filter { it.nivel == "intermedio" }
        val ids = if (intermedios.isNotEmpty()) intermedios.map { it.id } else exerciseIdsForLevel("intermedio")
        return ids.all { id -> sessionManager.getExerciseProgress(id).completado }
    }

    private fun isNivelAvanzadoCompleto(sessionManager: SessionManager, ejercicios: List<EjercicioRitmoDto>): Boolean {
        val avanzados = ejercicios.filter { it.nivel == "avanzado" }
        val ids = if (avanzados.isNotEmpty()) avanzados.map { it.id } else exerciseIdsForLevel("avanzado")
        return ids.all { id -> sessionManager.getExerciseProgress(id).completado }
    }
}
