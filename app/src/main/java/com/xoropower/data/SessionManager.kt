package com.xoropower.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("xoropower_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_AVATAR = "user_avatar"
        const val USER_ROLE = "user_role"
    }

    // Guarda los datos de la sesión
    fun saveAuthToken(token: String, name: String, email: String, avatar: String, rol: String = "estudiante") {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_AVATAR, avatar)
        editor.putString(USER_ROLE, rol)
        editor.apply()
    }

    // Recupera el token guardado
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Recupera el nombre del usuario
    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, "Estudiante")
    }

    // Recupera el email del usuario
    fun fetchUserEmail(): String? {
        return prefs.getString(USER_EMAIL, "")
    }

    // Recupera el avatar del usuario
    fun fetchUserAvatar(): String {
        return prefs.getString(USER_AVATAR, "🤠") ?: "🤠"
    }

    // Recupera el rol del usuario
    fun fetchUserRole(): String {
        return prefs.getString(USER_ROLE, "estudiante") ?: "estudiante"
    }

    // Verifica si el usuario actual es administrador
    fun isAdmin(): Boolean {
        return fetchUserRole() == "admin"
    }

    // Guarda el progreso de un ejercicio rítmico local
    fun saveExerciseProgress(idEjercicio: String, score: Int) {
        val keyCompleted = "progreso_${idEjercicio}_completed"
        val keyScore = "progreso_${idEjercicio}_score"
        val keyAttempts = "progreso_${idEjercicio}_attempts"
        
        val currentMaxScore = prefs.getInt(keyScore, 0)
        val newMaxScore = maxOf(currentMaxScore, score)
        
        val attempts = prefs.getInt(keyAttempts, 0) + 1
        val completed = prefs.getBoolean(keyCompleted, false) || (score >= 70)
        
        val editor = prefs.edit()
        editor.putBoolean(keyCompleted, completed)
        editor.putInt(keyScore, newMaxScore)
        editor.putInt(keyAttempts, attempts)
        editor.apply()
    }

    // Sincroniza el progreso del servidor con la sesión local.
    // Siempre confía en el servidor: sobreescribe el valor local con lo que dice la BD.
    fun syncExerciseProgress(idEjercicio: String, score: Int, completed: Boolean) {
        val keyCompleted = "progreso_${idEjercicio}_completed"
        val keyScore = "progreso_${idEjercicio}_score"
        
        val editor = prefs.edit()
        editor.putBoolean(keyCompleted, completed)
        editor.putInt(keyScore, score)
        editor.apply()
    }

    // Borra el progreso local de todos los ejercicios conocidos.
    // Se llama antes de aplicar el progreso fresco del servidor para evitar datos fantasma.
    fun resetAllExerciseProgress(ids: List<String>) {
        val editor = prefs.edit()
        for (id in ids) {
            editor.remove("progreso_${id}_completed")
            editor.remove("progreso_${id}_score")
            editor.remove("progreso_${id}_attempts")
        }
        editor.apply()
    }

    // Obtiene el progreso guardado de un ejercicio rítmico local
    fun getExerciseProgress(idEjercicio: String): ProgresoUsuario {
        val keyCompleted = "progreso_${idEjercicio}_completed"
        val keyScore = "progreso_${idEjercicio}_score"
        val keyAttempts = "progreso_${idEjercicio}_attempts"
        
        val completed = prefs.getBoolean(keyCompleted, false)
        val score = prefs.getInt(keyScore, 0)
        val attempts = prefs.getInt(keyAttempts, 0)
        
        return ProgresoUsuario(
            idProgreso = null,
            idUsuario = "",
            idEjercicio = idEjercicio,
            idLeccion = null,
            idModulo = null,
            completado = completed,
            puntuacionMasAlta = score,
            porcentajeAvance = score.toFloat(),
            vecesIntentado = if (attempts > 0) attempts else null,
            timestampUltimoIntento = null,
            timestampCompletado = null
        )
    }

    // Borra la sesión (Logout)
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
