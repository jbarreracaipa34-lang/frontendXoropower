package com.xoropower.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xoropower.data.network.EjercicioRitmoDto
import com.xoropower.data.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EjerciciosListState {
    object Idle : EjerciciosListState()
    object Loading : EjerciciosListState()
    data class Success(val ejercicios: List<EjercicioRitmoDto>) : EjerciciosListState()
    data class Error(val mensaje: String) : EjerciciosListState()
}

/**
 * ViewModel que carga la lista completa de ejercicios de ritmo desde la API.
 * CategoryScreen e InstructionScreen lo usan para obtener descripción,
 * pasos y videoUrl en lugar de los datos hardcodeados.
 */
class EjerciciosListViewModel : ViewModel() {

    // Inicia directamente en Success vacío para que la UI cargue inmediatamente los datos locales
    // sin mostrar estados de carga (shimmers, spinners o vacíos).
    private val _estado = MutableStateFlow<EjerciciosListState>(EjerciciosListState.Success(emptyList()))
    val estado: StateFlow<EjerciciosListState> = _estado

    init {
        // Primera carga al crear el ViewModel (de forma silenciosa)
        viewModelScope.launch {
            fetchEjerciciosSilently()
        }
        // Refresco silencioso cada 10 minutos en background (igual que ModuleViewModel)
        startSyncLoop()
    }

    private fun startSyncLoop() {
        viewModelScope.launch {
            while (true) {
                delay(10 * 60 * 1000L)
                fetchEjerciciosSilently()
            }
        }
    }

    /** Refresca sin poner el estado en Loading — la UI nunca se queda en blanco. */
    private suspend fun fetchEjerciciosSilently() {
        try {
            val respuesta = RetrofitClient.apiService.getEjerciciosRitmo()
            if (respuesta.isSuccessful) {
                val lista = respuesta.body()?.data ?: emptyList()
                _estado.value = EjerciciosListState.Success(lista)
            }
        } catch (_: Exception) { /* fallo silencioso, conserva datos anteriores */ }
    }

    /** Llamada pública desde la UI. Solo actúa si no hay ya datos cargados. */
    fun cargarEjercicios() {
        val current = _estado.value
        if (current is EjerciciosListState.Success && current.ejercicios.isNotEmpty()) return
        viewModelScope.launch {
            fetchEjerciciosSilently()
        }
    }

    /** Devuelve el ejercicio que corresponde al nivel + índice (0-based) dentro de ese nivel. */
    fun ejercicioPorNivelEIndice(nivel: String, indice: Int): EjercicioRitmoDto? {
        val lista = (estado.value as? EjerciciosListState.Success)?.ejercicios ?: return null
        return lista.filter { it.nivel == nivel }.getOrNull(indice)
    }

    /** Devuelve el ejercicio por su ID exacto. */
    fun ejercicioPorId(id: String): EjercicioRitmoDto? {
        val lista = (estado.value as? EjerciciosListState.Success)?.ejercicios ?: return null
        return lista.firstOrNull { it.id == id }
    }
}
