package com.xoropower.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xoropower.data.ActualizarProgresoDto
import com.xoropower.data.EjerciciosData
import com.xoropower.data.SessionManager
import com.xoropower.data.network.RetrofitClient
import com.xoropower.ui.viewmodel.RitmoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun PantallaRitmo(
    idEjercicio: String,
    onVolverClick: () -> Unit
) {
    val contexto = LocalContext.current
    val sessionManager = remember { SessionManager(contexto) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // Gestión de la carga para ejercicios provistos por el backend.
    val viewModel: RitmoViewModel = viewModel()
    val detallesEjercicio by viewModel.exercise.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()

    // Se inicializa la petición para cargar el ejercicio seleccionado al presentarse la pantalla.
    LaunchedEffect(idEjercicio) {
        viewModel.loadExercise(idEjercicio, token)
    }

    when {
        cargando -> {
            // Renderizado de la interfaz visual de carga premium.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF0A0A14), Color(0xFF0D0D20)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cargando ejercicio...",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        }

        detallesEjercicio != null -> {
            // Renderizado e inicio del motor del juego utilizando los datos obtenidos en tiempo real del backend.
            MotorRitmo(
                titulo = detallesEjercicio!!.titulo,
                listaNotas = detallesEjercicio!!.notas,
                tempoBpm = detallesEjercicio!!.tempoBpm,
                onVolverClick = onVolverClick,
                onFinalizarEjercicio = { porcentaje ->
                    // Se guarda el progreso actual a nivel local para habilitar cálculos híbridos.
                    sessionManager.saveExerciseProgress(idEjercicio, porcentaje)
                    
                    // Guardar en la base de datos — GlobalScope para que no se cancele al desmontar el composable
                    @Suppress("OPT_IN_USAGE")
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            RetrofitClient.apiService.actualizarProgreso(
                                ActualizarProgresoDto(
                                    idEjercicio = idEjercicio,
                                    puntuacion = porcentaje
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                idEjercicio = idEjercicio
            )
        }

        else -> {
            // Interfaz de respaldo en caso de que ocurra un error durante el proceso de carga.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A14)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No se pudo cargar el ejercicio.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.loadExercise(idEjercicio, token) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("Reintentar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
