package com.xoropower.data

import com.google.gson.annotations.SerializedName

// Modelos de datos para xoropower
// Coinciden con el esquema de base de datos en español.

data class Usuario(
    @SerializedName("id") val id: String,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = "🤠",
    @SerializedName("rol") val rol: String? = "estudiante"
)

data class Modulo(
    @SerializedName("id") val id: String,
    @SerializedName("identificador") val identificador: String? = null,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("url_portada") val urlPortada: String? = null,
    @SerializedName("orden") val orden: Int
)

data class Seccion(
    @SerializedName("id") val id: String,
    @SerializedName("modulo_id") val moduloId: String,
    @SerializedName("nivel") val nivel: String, // basico, intermedio, avanzado
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("orden") val orden: Int,
    @SerializedName("actividades") val actividades: List<Actividad> = emptyList()
)

data class Actividad(
    @SerializedName("id") val id: String,
    @SerializedName("seccion_id") val seccionId: String,
    @SerializedName("tipo") val tipo: String, // informacion, video, ejercicio
    @SerializedName("titulo") val titulo: String,
    @SerializedName("texto_cuerpo") val textoCuerpo: String?,
    @SerializedName("url_video") val urlVideo: String?, // Reservado para uso futuro (admin panel)
    @SerializedName("orden") val orden: Int
)

data class ProgresoUsuario(
    @SerializedName("idProgreso") val idProgreso: String?,
    @SerializedName("idUsuario") val idUsuario: String,
    @SerializedName("idEjercicio") val idEjercicio: String?,
    @SerializedName("idLeccion") val idLeccion: String?,
    @SerializedName("idModulo") val idModulo: String?,
    @SerializedName("completado") val completado: Boolean,
    @SerializedName("puntuacionMasAlta") val puntuacionMasAlta: Int?,
    @SerializedName("porcentajeAvance") val porcentajeAvance: Float?,
    @SerializedName("vecesIntentado") val vecesIntentado: Int?,
    @SerializedName("timestampUltimoIntento") val timestampUltimoIntento: String?,
    @SerializedName("timestampCompletado") val timestampCompletado: String?
)

data class ResumenProgreso(
    @SerializedName("completadas") val completadas: Int,
    @SerializedName("enProgreso") val enProgreso: Int,
    @SerializedName("total") val total: Int
)

// Envoltorio para respuestas de la API
data class ApiResponse<T>(
    @SerializedName("data") val data: T?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String? = null,
    @SerializedName("success") val success: Boolean
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("usuario") val usuario: Usuario
)

// ── DTOs para Peticiones (Request Bodies) ─────────────────────

data class LoginDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegistroDto(
    @SerializedName("nombre_usuario") val nombreCompleto: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class ActualizarProgresoDto(
    @SerializedName("id_ejercicio") val idEjercicio: String,
    @SerializedName("puntuacion") val puntuacion: Int
)

data class RachaDto(
    @SerializedName("rachaActual") val rachaActual: Int,
    @SerializedName("rachaMaxima") val rachaMaxima: Int,
    @SerializedName("ultimaFecha") val ultimaFecha: String?
)

data class PuntoHistorialDto(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("precisionPromedio") val precisionPromedio: Float,
    @SerializedName("totalEjercicios") val totalEjercicios: Int
)

data class IntentoDto(
    @SerializedName("id") val id: String?,
    @SerializedName("idEjercicio") val idEjercicio: String,
    @SerializedName("precisionPorcentaje") val precisionPorcentaje: Float,
    @SerializedName("aciertos") val aciertos: Int,
    @SerializedName("fallos") val fallos: Int,
    @SerializedName("perdidos") val perdidos: Int,
    @SerializedName("fecha") val fecha: String
)

data class CrearEjercicioDto(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("nivel") val nivel: String,
    @SerializedName("tempoBpm") val tempoBpm: Int,
    @SerializedName("secuenciaNotas") val secuenciaNotas: List<NotaEjercicioDto>,
    @SerializedName("videoUrl") val videoUrl: String? = null,
    @SerializedName("videoBase64") val videoBase64: String? = null,
    @SerializedName("videoExtension") val videoExtension: String? = null,
    @SerializedName("pasoAPaso") val pasoAPaso: String? = null
)

data class NotaEjercicioDto(
    @SerializedName("ms") val ms: Long,
    @SerializedName("mano") val mano: String,
    @SerializedName("color") val color: String? = null,
    @SerializedName("texto") val texto: String? = null,
    @SerializedName("tipo") val tipo: String? = null
)

data class PromoverAdminDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
