package com.xoropower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xoropower.data.EjerciciosData
import com.xoropower.data.ProgresoUsuario
import com.xoropower.data.ResumenProgreso
import com.xoropower.data.RachaDto
import com.xoropower.data.SessionManager
import com.xoropower.data.PuntoHistorialDto
import com.xoropower.data.network.EjercicioRitmoDto
import com.xoropower.data.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProgresoState {
    object Loading : ProgresoState()
    data class Success(
        val resumen: ResumenProgreso,
        val lista: List<ProgresoUsuario>,
        val ejercicios: List<EjercicioRitmoDto>,
        val racha: RachaDto?,
        val historialSemanal: List<PuntoHistorialDto>
    ) : ProgresoState()
    data class Error(val message: String) : ProgresoState()
}

class ProgresoViewModel(application: Application) : AndroidViewModel(application) {
    private val _progresoState = MutableStateFlow<ProgresoState>(ProgresoState.Loading)
    val progresoState: StateFlow<ProgresoState> = _progresoState

    private val api = RetrofitClient.apiService
    private val sessionManager = SessionManager(application)

    init {
        startSyncLoop()
    }

    private fun startSyncLoop() {
        viewModelScope.launch {
            // Esperamos los primeros 5 minutos antes del primer refresco automático,
            // ya que al abrir la pantalla se ejecuta fetchProgreso() inmediatamente.
            delay(5 * 60 * 1000)
            while (true) {
                fetchProgresoSilently()
                delay(5 * 60 * 1000)
            }
        }
    }

    private suspend fun fetchProgresoSilently() {
        if (sessionManager.fetchAuthToken().isNullOrEmpty()) {
            return
        }
        try {
            val responseResumen = api.getResumenProgreso()
            val responseLista = api.getProgreso()
            val responseEjercicios = api.getEjerciciosRitmo()
            val responseRacha = api.getRacha()
            val responseHistorial = api.getHistorialSemanal()

            if (responseResumen.isSuccessful && responseLista.isSuccessful) {
                val resumen = responseResumen.body()?.data
                val lista = responseLista.body()?.data ?: emptyList()

                // Primero limpiamos todo el progreso local para evitar datos fantasma
                // cuando la BD no tiene registros o el usuario los eliminó.
                val todosLosIds = listOf(
                    EjerciciosData.ID_EJERCICIO_DERECHA,
                    EjerciciosData.ID_EJERCICIO_IZQUIERDA,
                    EjerciciosData.ID_EJERCICIO_JUNTAS,
                    EjerciciosData.ID_INTERMEDIO_1,
                    EjerciciosData.ID_INTERMEDIO_2,
                    EjerciciosData.ID_INTERMEDIO_3,
                    EjerciciosData.ID_AVANZADO_1,
                    EjerciciosData.ID_AVANZADO_2,
                    EjerciciosData.ID_AVANZADO_3
                ) + lista.mapNotNull { it.idEjercicio }
                sessionManager.resetAllExerciseProgress(todosLosIds)

                // Ahora sí aplicamos el progreso fresco del servidor
                lista.forEach { progreso ->
                    if (progreso.idEjercicio != null) {
                        sessionManager.syncExerciseProgress(
                            idEjercicio = progreso.idEjercicio,
                            score = progreso.puntuacionMasAlta ?: 0,
                            completed = progreso.completado
                        )
                    }
                }

                val ejercicios = if (responseEjercicios.isSuccessful) {
                    responseEjercicios.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }
                val racha = if (responseRacha.isSuccessful) {
                    responseRacha.body()?.data
                } else {
                    null
                }
                val historial = if (responseHistorial.isSuccessful) {
                    responseHistorial.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }

                if (resumen != null) {
                    _progresoState.value = ProgresoState.Success(
                        resumen = resumen,
                        lista = lista,
                        ejercicios = ejercicios,
                        racha = racha,
                        historialSemanal = historial
                    )
                }
            }
        } catch (e: Exception) {
            // Se omite de forma silenciosa en la sincronización en segundo plano
        }
    }

    fun fetchProgreso() {
        _progresoState.value = ProgresoState.Loading
        viewModelScope.launch {
            try {
                val responseResumen = api.getResumenProgreso()
                val responseLista = api.getProgreso()
                val responseEjercicios = api.getEjerciciosRitmo()
                val responseRacha = api.getRacha()
                val responseHistorial = api.getHistorialSemanal()

                if (responseResumen.isSuccessful && responseLista.isSuccessful) {
                    val resumen = responseResumen.body()?.data
                    val lista = responseLista.body()?.data ?: emptyList()

                    // Primero limpiamos todo el progreso local para evitar datos fantasma
                    val todosLosIds = listOf(
                        EjerciciosData.ID_EJERCICIO_DERECHA,
                        EjerciciosData.ID_EJERCICIO_IZQUIERDA,
                        EjerciciosData.ID_EJERCICIO_JUNTAS,
                        EjerciciosData.ID_INTERMEDIO_1,
                        EjerciciosData.ID_INTERMEDIO_2,
                        EjerciciosData.ID_INTERMEDIO_3,
                        EjerciciosData.ID_AVANZADO_1,
                        EjerciciosData.ID_AVANZADO_2,
                        EjerciciosData.ID_AVANZADO_3
                    ) + lista.mapNotNull { it.idEjercicio }
                    sessionManager.resetAllExerciseProgress(todosLosIds)

                    // Ahora sí aplicamos el progreso fresco del servidor
                    lista.forEach { progreso ->
                        if (progreso.idEjercicio != null) {
                            sessionManager.syncExerciseProgress(
                                idEjercicio = progreso.idEjercicio,
                                score = progreso.puntuacionMasAlta ?: 0,
                                completed = progreso.completado
                            )
                        }
                    }

                    val ejercicios = if (responseEjercicios.isSuccessful) {
                        responseEjercicios.body()?.data ?: emptyList()
                    } else {
                        emptyList()
                    }
                    val racha = if (responseRacha.isSuccessful) {
                        responseRacha.body()?.data
                    } else {
                        null
                    }
                    val historial = if (responseHistorial.isSuccessful) {
                        responseHistorial.body()?.data ?: emptyList()
                    } else {
                        emptyList()
                    }

                    if (resumen != null) {
                        _progresoState.value = ProgresoState.Success(
                            resumen = resumen,
                            lista = lista,
                            ejercicios = ejercicios,
                            racha = racha,
                            historialSemanal = historial
                        )
                    } else {
                        _progresoState.value = ProgresoState.Error("Resumen no disponible")
                    }
                } else {
                    val errorMsg = "Resumen error: ${responseResumen.code()}, Lista error: ${responseLista.code()}"
                    _progresoState.value = ProgresoState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _progresoState.value = ProgresoState.Error("Error de red: ${e.message}")
            }
        }
    }
}
