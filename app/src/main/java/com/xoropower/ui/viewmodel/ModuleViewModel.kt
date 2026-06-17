package com.xoropower.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xoropower.data.Actividad
import com.xoropower.data.Modulo
import com.xoropower.data.Seccion
import com.xoropower.data.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ModuleState {
    object Loading : ModuleState()
    data class Success(val modules: List<Modulo>) : ModuleState()
    data class Error(val message: String) : ModuleState()
}

sealed class SectionState {
    object Idle : SectionState()
    object Loading : SectionState()
    data class Success(val seccion: Seccion) : SectionState()
    data class Error(val message: String) : SectionState()
}

class ModuleViewModel : ViewModel() {
    private val _moduleState = MutableStateFlow<ModuleState>(ModuleState.Loading)
    val moduleState: StateFlow<ModuleState> = _moduleState

    private val _sectionState = MutableStateFlow<SectionState>(SectionState.Idle)
    val sectionState: StateFlow<SectionState> = _sectionState

    // Estado que almacena de manera persistente el identificador del módulo actualmente seleccionado.
    private val _currentModuloId = MutableStateFlow<String?>(null)
    val currentModuloId: StateFlow<String?> = _currentModuloId

    private val _hasSeenInstructions = MutableStateFlow(false)
    val hasSeenInstructions: StateFlow<Boolean> = _hasSeenInstructions

    fun setInstructionsSeen(seen: Boolean) {
        _hasSeenInstructions.value = seen
    }

    // Nivel seleccionado (básico / intermedio / avanzado) — persiste al volver desde un ejercicio
    private val _selectedLevel = MutableStateFlow("basico")
    val selectedLevel: StateFlow<String> = _selectedLevel

    fun setSelectedLevel(nivel: String) {
        _selectedLevel.value = nivel
    }

    private val api = RetrofitClient.apiService

    init {
        startSyncLoop()
    }

    // Asigna el identificador del módulo actualmente seleccionado.
    fun setCurrentModuloId(id: String?) {
        _currentModuloId.value = id
    }

    private fun startSyncLoop() {
        viewModelScope.launch {
            while (true) {
                fetchModulosSilently()
                delay(5 * 60 * 1000)
            }
        }
    }

    private suspend fun fetchModulosSilently() {
        try {
            val response = api.getModulos()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()?.data ?: emptyList()
                _moduleState.value = ModuleState.Success(list)
            }
        } catch (e: Exception) {
            // Se omite la visualización de errores de forma silenciosa en caso de que ya se disponga de datos en el estado.
        }
    }

    fun fetchModulos() {
        if (_moduleState.value !is ModuleState.Success) {
            _moduleState.value = ModuleState.Loading
        }

        viewModelScope.launch {
            try {
                val response = api.getModulos()
                if (response.isSuccessful && response.body()?.success == true) {
                    val list = response.body()?.data ?: emptyList()
                    _moduleState.value = ModuleState.Success(list)
                } else if (_moduleState.value !is ModuleState.Success) {
                    _moduleState.value = ModuleState.Error("No se pudieron cargar los módulos")
                }
            } catch (e: Exception) {
                if (_moduleState.value !is ModuleState.Success) {
                    _moduleState.value = ModuleState.Error("Error de red: ${e.message}")
                }
            }
        }
    }

    fun fetchSeccion(moduloId: String, nivel: String) {
        val currentState = _sectionState.value
        if (currentState !is SectionState.Success ||
            currentState.seccion.moduloId != moduloId ||
            currentState.seccion.nivel != nivel) {
            _sectionState.value = SectionState.Loading
        }

        viewModelScope.launch {
            try {
                val response = api.getSeccionPorNivel(moduloId, nivel)
                if (response.isSuccessful && response.body()?.success == true) {
                    val seccion = response.body()?.data
                    if (seccion != null) {
                        _sectionState.value = SectionState.Success(seccion)
                    } else if (_sectionState.value !is SectionState.Success) {
                        _sectionState.value = SectionState.Error("No hay contenido para este nivel")
                    }
                } else if (_sectionState.value !is SectionState.Success) {
                    _sectionState.value = SectionState.Error("Nivel no disponible")
                }
            } catch (e: Exception) {
                if (_sectionState.value !is SectionState.Success) {
                    _sectionState.value = SectionState.Error("Error de red: ${e.message}")
                }
            }
        }
    }

    fun getActividades(): List<Actividad> {
        val state = _sectionState.value
        return if (state is SectionState.Success) state.seccion.actividades else emptyList()
    }
}