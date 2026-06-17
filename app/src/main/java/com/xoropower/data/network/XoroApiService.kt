package com.xoropower.data.network

import com.xoropower.data.*
import retrofit2.Response
import retrofit2.http.*

// ── RITMO DTOS ──────────────────────────────────────────────
data class NotaRitmoDto(
    val ms: Long,
    val mano: String,
    val color: String,
    val texto: String?,
    val tipo: String? = null
)

data class EjercicioRitmoDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val nivel: String,
    val tempoBpm: Int,
    val secuenciaNotas: List<NotaRitmoDto> = emptyList(),
    val videoUrl: String? = null,
    val pasoAPaso: String? = null
)

interface XoroApiService {

    // ── AUTENTICACIÓN ────────────────────────────────────────────
    
    @POST("autenticacion/login")
    suspend fun login(@Body request: LoginDto): Response<ApiResponse<LoginResponse>>

    @POST("autenticacion/registro")
    suspend fun register(@Body request: RegistroDto): Response<ApiResponse<Usuario>>

    // ── MÓDULOS ───────────────────────────────────────────────────

    @GET("modulos")
    suspend fun getModulos(): Response<ApiResponse<List<Modulo>>>

    @GET("modulos/{identificador}")
    suspend fun getModuloDetalle(
        @Path("identificador") identificador: String
    ): Response<ApiResponse<Modulo>>

    @GET("modulos/{identificador}/seleccionador/{nivel}")
    suspend fun getSeccionPorNivel(
        @Path("identificador") identificador: String,
        @Path("nivel") nivel: String
    ): Response<ApiResponse<Seccion>>

    // ── PROGRESO ──────────────────────────────────────────────────

    @GET("progreso")
    suspend fun getProgreso(): Response<ApiResponse<List<ProgresoUsuario>>>

    @GET("progreso/resumen")
    suspend fun getResumenProgreso(): Response<ApiResponse<ResumenProgreso>>

    @POST("progreso")
    suspend fun actualizarProgreso(
        @Body request: ActualizarProgresoDto
    ): Response<ApiResponse<ProgresoUsuario>>

    // ── GAMIFICACIÓN ──────────────────────────────────────────────

    @GET("racha")
    suspend fun getRacha(): Response<ApiResponse<RachaDto>>

    @GET("historial/semanal")
    suspend fun getHistorialSemanal(): Response<ApiResponse<List<PuntoHistorialDto>>>

    @GET("historial/{idEjercicio}")
    suspend fun getHistorialEjercicio(
        @Path("idEjercicio") idEjercicio: String
    ): Response<ApiResponse<List<IntentoDto>>>

    // ── RITMO ─────────────────────────────────────────────────────

    @GET("ejercicios-ritmo")
    suspend fun getEjerciciosRitmo(): Response<ApiResponse<List<EjercicioRitmoDto>>>

    @GET("ejercicios-ritmo/{id}")
    suspend fun getEjercicioRitmo(
        @Path("id") id: String
    ): Response<ApiResponse<EjercicioRitmoDto>>

    // ── ADMIN ────────────────────────────────────────────────────

    @POST("ejercicios-ritmo")
    suspend fun crearEjercicio(
        @Body request: CrearEjercicioDto
    ): Response<ApiResponse<EjercicioRitmoDto>>

    @POST("autenticacion/promover-admin")
    suspend fun promoverAdmin(
        @Body request: PromoverAdminDto
    ): Response<ApiResponse<Usuario>>
}
