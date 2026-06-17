package com.xoropower.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xoropower.data.network.NotaRitmoDto
import com.xoropower.data.network.RetrofitClient
import com.xoropower.ui.screens.RhythmNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

data class DetallesEjercicio(
    val id: String,
    val titulo: String,
    val tempoBpm: Int,
    val notas: List<RhythmNote>
)

class RitmoViewModel : ViewModel() {
    private val _ejercicio = MutableStateFlow<DetallesEjercicio?>(null)
    val exercise: StateFlow<DetallesEjercicio?> = _ejercicio

    private val _cargando = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _cargando

    fun loadExercise(idEjercicio: String, token: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // El interceptor configurado en RetrofitClient añade el token de autenticación automáticamente a la cabecera.
                val respuesta = RetrofitClient.apiService.getEjercicioRitmo(idEjercicio)

                if (respuesta.isSuccessful) {
                    val cuerpo = respuesta.body()
                    println("RitmoViewModel: Cuerpo recibido: $cuerpo")
                    val datos = cuerpo?.data

                    if (datos != null) {
                        println("RitmoViewModel: Datos ejercicio: ${datos.titulo}")
                        val notasJuego = datos.secuenciaNotas.mapIndexed { index, nota: NotaRitmoDto ->
                            RhythmNote(
                                id = index,
                                ms = nota.ms,
                                lane = nota.mano,
                                color = if (nota.color == "azul") Color(0xFF2196F3) else Color(0xFFF44336),
                                text = nota.texto ?: "",
                                tipo = nota.tipo ?: "negra"
                            )
                        }

                        _ejercicio.value = DetallesEjercicio(
                            id = datos.id,
                            titulo = datos.titulo,
                            tempoBpm = datos.tempoBpm,
                            notas = notasJuego
                        )
                        println("RitmoViewModel: Ejercicio cargado en el StateFlow")
                    } else {
                        println("RitmoViewModel: Respuesta exitosa pero .data es null.")
                    }
                } else {
                    val errorBody = respuesta.errorBody()?.string()
                    println("RitmoViewModel: Error de servidor ${respuesta.code()} - $errorBody")
                }

            } catch (error: Exception) {
                println("RitmoViewModel: Excepción - ${error.message}")
                error.printStackTrace()
            } finally {
                _cargando.value = false
            }
        }
    }
}
