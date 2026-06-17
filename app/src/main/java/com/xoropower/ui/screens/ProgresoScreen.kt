package com.xoropower.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.xoropower.navigation.Routes
import com.xoropower.ui.theme.*
import com.xoropower.ui.viewmodel.AuthViewModel
import com.xoropower.ui.viewmodel.ProgresoViewModel
import com.xoropower.ui.viewmodel.ProgresoState
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import com.xoropower.data.PuntoHistorialDto
import com.xoropower.data.RachaDto

@Composable
fun ProgresoScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    progresoViewModel: ProgresoViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val progresoState by progresoViewModel.progresoState.collectAsState()

    // Se realiza la carga del progreso musical del usuario al iniciar la pantalla.
    LaunchedEffect(Unit) {
        progresoViewModel.fetchProgreso()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { 
            DrawerContent(
                navController = navController, 
                onClose = { scope.launch { drawerState.close() } },
                authViewModel = authViewModel
            ) 
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(XoroBlack)) {
            // Fondo decorativo con iluminación premium (degradado sutil).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(XoroBlue.copy(alpha = 0.08f), Color.Transparent),
                            startY = 0f,
                            endY = 1200f
                        )
                    )
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    xoropowerTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        navController = navController,
                        authViewModel = authViewModel
                    )
                },
                bottomBar = {
                    BottomNavBar(navController, Routes.PROGRESO)
                }
            ) { padding ->
                when (val state = progresoState) {
                    is ProgresoState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFFD700), // Dorado Premium
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Cargando tu progreso musical...",
                                    color = XoroMuted,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    is ProgresoState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(XoroSurface)
                                    .border(1.dp, XoroRed.copy(0.2f), RoundedCornerShape(24.dp))
                                    .padding(24.dp)
                            ) {
                                Text(
                                    "Error al Cargar",
                                    color = XoroRed,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    state.message,
                                    color = XoroMuted,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { progresoViewModel.fetchProgreso() },
                                    colors = ButtonDefaults.buttonColors(containerColor = XoroBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reintentar", tint = XoroWhite)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reintentar", color = XoroWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    is ProgresoState.Success -> {
                        val resumen = state.resumen
                        val lista = state.lista
                        val ejercicios = state.ejercicios
                        val racha = state.racha
                        val historialSemanal = state.historialSemanal

                        val context = androidx.compose.ui.platform.LocalContext.current
                        val sessionManager = remember(context) { com.xoropower.data.SessionManager(context) }

                        // Sincronizar el progreso recibido del backend hacia las SharedPreferences locales del dispositivo
                        LaunchedEffect(lista) {
                            lista.forEach { item ->
                                if (item.idEjercicio != null) {
                                    val local = sessionManager.getExerciseProgress(item.idEjercicio)
                                    val dbScore = item.puntuacionMasAlta ?: 0
                                    val localScore = local.puntuacionMasAlta ?: 0
                                    if (dbScore > localScore || (item.completado && !local.completado)) {
                                        sessionManager.saveExerciseProgress(item.idEjercicio, dbScore)
                                    }
                                }
                            }
                        }

                        val derechaProgreso = remember(sessionManager, lista) {
                            val local = sessionManager.getExerciseProgress(com.xoropower.data.EjerciciosData.ID_EJERCICIO_DERECHA)
                            val backend = lista.find { it.idEjercicio == com.xoropower.data.EjerciciosData.ID_EJERCICIO_DERECHA }
                            if (backend != null) {
                                com.xoropower.data.ProgresoUsuario(
                                    idProgreso = backend.idProgreso,
                                    idUsuario = backend.idUsuario,
                                    idEjercicio = com.xoropower.data.EjerciciosData.ID_EJERCICIO_DERECHA,
                                    idLeccion = backend.idLeccion,
                                    idModulo = backend.idModulo,
                                    completado = backend.completado || local.completado,
                                    puntuacionMasAlta = maxOf(backend.puntuacionMasAlta ?: 0, local.puntuacionMasAlta ?: 0),
                                    porcentajeAvance = maxOf(backend.porcentajeAvance ?: 0f, local.porcentajeAvance ?: 0f),
                                    vecesIntentado = maxOf(backend.vecesIntentado ?: 0, local.vecesIntentado ?: 0),
                                    timestampUltimoIntento = backend.timestampUltimoIntento ?: local.timestampUltimoIntento,
                                    timestampCompletado = backend.timestampCompletado ?: local.timestampCompletado
                                )
                            } else {
                                local
                            }
                        }
                        val izquierdaProgreso = remember(sessionManager, lista) {
                            val local = sessionManager.getExerciseProgress(com.xoropower.data.EjerciciosData.ID_EJERCICIO_IZQUIERDA)
                            val backend = lista.find { it.idEjercicio == com.xoropower.data.EjerciciosData.ID_EJERCICIO_IZQUIERDA }
                            if (backend != null) {
                                com.xoropower.data.ProgresoUsuario(
                                    idProgreso = backend.idProgreso,
                                    idUsuario = backend.idUsuario,
                                    idEjercicio = com.xoropower.data.EjerciciosData.ID_EJERCICIO_IZQUIERDA,
                                    idLeccion = backend.idLeccion,
                                    idModulo = backend.idModulo,
                                    completado = backend.completado || local.completado,
                                    puntuacionMasAlta = maxOf(backend.puntuacionMasAlta ?: 0, local.puntuacionMasAlta ?: 0),
                                    porcentajeAvance = maxOf(backend.porcentajeAvance ?: 0f, local.porcentajeAvance ?: 0f),
                                    vecesIntentado = maxOf(backend.vecesIntentado ?: 0, local.vecesIntentado ?: 0),
                                    timestampUltimoIntento = backend.timestampUltimoIntento ?: local.timestampUltimoIntento,
                                    timestampCompletado = backend.timestampCompletado ?: local.timestampCompletado
                                )
                            } else {
                                local
                            }
                        }
                        val mergedJuntas = remember(sessionManager, lista) {
                            val local = sessionManager.getExerciseProgress(com.xoropower.data.EjerciciosData.ID_EJERCICIO_JUNTAS)
                            val backend = lista.find { it.idEjercicio == com.xoropower.data.EjerciciosData.ID_EJERCICIO_JUNTAS }
                            if (backend != null) {
                                com.xoropower.data.ProgresoUsuario(
                                    idProgreso = backend.idProgreso,
                                    idUsuario = backend.idUsuario,
                                    idEjercicio = com.xoropower.data.EjerciciosData.ID_EJERCICIO_JUNTAS,
                                    idLeccion = backend.idLeccion,
                                    idModulo = backend.idModulo,
                                    completado = backend.completado || local.completado,
                                    puntuacionMasAlta = maxOf(backend.puntuacionMasAlta ?: 0, local.puntuacionMasAlta ?: 0),
                                    porcentajeAvance = maxOf(backend.porcentajeAvance ?: 0f, local.porcentajeAvance ?: 0f),
                                    vecesIntentado = maxOf(backend.vecesIntentado ?: 0, local.vecesIntentado ?: 0),
                                    timestampUltimoIntento = backend.timestampUltimoIntento ?: local.timestampUltimoIntento,
                                    timestampCompletado = backend.timestampCompletado ?: local.timestampCompletado
                                )
                            } else {
                                local
                            }
                        }

                        // Se realiza el cálculo del progreso general consolidado sobre la base de 3 ejercicios.
                        val completedCount = remember(derechaProgreso, izquierdaProgreso, mergedJuntas) {
                            var count = 0
                            if (derechaProgreso.completado) count++
                            if (izquierdaProgreso.completado) count++
                            if (mergedJuntas.completado) count++
                            count
                        }

                        val totalProgress = completedCount / 3f

                        val mergedList = remember(lista, derechaProgreso, izquierdaProgreso, mergedJuntas) {
                            val temp = mutableListOf<com.xoropower.data.ProgresoUsuario>()
                            // En caso de registrarse intentos o finalizaciones, se incorporan al listado del historial.
                            if ((derechaProgreso.vecesIntentado ?: 0) > 0 || derechaProgreso.completado) {
                                temp.add(derechaProgreso)
                            }
                            if ((izquierdaProgreso.vecesIntentado ?: 0) > 0 || izquierdaProgreso.completado) {
                                temp.add(izquierdaProgreso)
                            }
                            if ((mergedJuntas.vecesIntentado ?: 0) > 0 || mergedJuntas.completado) {
                                temp.add(mergedJuntas)
                            }
                            // Se agregan ejercicios adicionales provistos por el backend si aún no están contenidos.
                            lista.forEach { item ->
                                if (item.idEjercicio != com.xoropower.data.EjerciciosData.ID_EJERCICIO_JUNTAS &&
                                    item.idEjercicio != com.xoropower.data.EjerciciosData.ID_EJERCICIO_DERECHA &&
                                    item.idEjercicio != com.xoropower.data.EjerciciosData.ID_EJERCICIO_IZQUIERDA) {
                                    temp.add(item)
                                }
                            }
                            temp
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(horizontal = 24.dp)
                        ) {
                            // Se renderiza el bloque de encabezado personalizado.
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFFCF1020).copy(0.3f), Color.Transparent)
                                                )
                                            )
                                            .border(1.5.dp, Color(0xFFFFD700), CircleShape), // Borde dorado
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            authViewModel.getUserAvatar() ?: "🤠",
                                            fontSize = 28.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "HOLA, ${authViewModel.getUserName()?.uppercase() ?: "JUGADOR"}",
                                            color = XoroMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Text(
                                            "Tu Progreso",
                                            color = XoroWhite,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Card de Racha 🔥
                            if (racha != null && racha.rachaActual > 0) {
                                item {
                                    val infiniteTransition = rememberInfiniteTransition(label = "flame")
                                    val flameScale by infiniteTransition.animateFloat(
                                        initialValue = 0.9f,
                                        targetValue = 1.1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "flameScale"
                                    )
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 24.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Brush.horizontalGradient(listOf(Color(0xFFFF4500), Color(0xFFFF8C00))))
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔥",
                                            fontSize = 40.sp,
                                            modifier = Modifier.graphicsLayer(scaleX = flameScale, scaleY = flameScale)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                "Racha de Práctica",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Text(
                                                "${racha.rachaActual} Días Consecutivos",
                                                color = XoroWhite,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                "¡Sigue así! Racha máxima de ${racha.rachaMaxima} días",
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // Se renderiza la tarjeta principal contenedora del progreso acumulado.
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(XoroSurface)
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
                                        .padding(24.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Nivel Actual", color = XoroMuted, fontSize = 13.sp)
                                            Text(
                                                if (completedCount >= 3) "Básico II" else "Básico I",
                                                color = XoroWhite,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                progress = { totalProgress },
                                                modifier = Modifier.size(72.dp),
                                                color = Color(0xFFFFD700), // Dorado
                                                trackColor = Color.White.copy(alpha = 0.05f),
                                                strokeWidth = 7.dp,
                                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                            )
                                            Text(
                                                "${(totalProgress * 100).toInt()}%",
                                                color = XoroWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(28.dp))

                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Actividades Realizadas", color = XoroMuted, fontSize = 14.sp)
                                            Text(
                                                "${completedCount} / 3",
                                                color = XoroWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        LinearProgressIndicator(
                                            progress = { totalProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(CircleShape),
                                            color = XoroBlue,
                                            trackColor = Color.White.copy(alpha = 0.05f)
                                        )
                                    }
                                }
                            }

                            // Gráfico de evolución semanal
                            item {
                                Spacer(modifier = Modifier.height(28.dp))
                                EvolutionChart(points = historialSemanal)
                            }

                            // Se renderiza la sección destinada al historial de actividades.
                            item {
                                Spacer(modifier = Modifier.height(36.dp))
                                Text(
                                    "Historial de Ejercicios",
                                    color = XoroWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }

                            if (mergedList.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(XoroSurface)
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "🪘",
                                            fontSize = 48.sp,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        Text(
                                            "¡Aún no tienes ejercicios registrados!",
                                            color = XoroWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Completa ejercicios en tus niveles para ver tu puntuación y avances aquí.",
                                            color = XoroMuted,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Button(
                                            onClick = { navController.navigate(Routes.CATEGORIES) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF1020)), // Rojo xoropower
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Empezar", tint = XoroWhite)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Empezar Ejercicio", color = XoroWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                items(mergedList) { item ->
                                    val nombreEjercicio = when (item.idEjercicio) {
                                        com.xoropower.data.EjerciciosData.ID_EJERCICIO_DERECHA -> "Mano Derecha"
                                        com.xoropower.data.EjerciciosData.ID_EJERCICIO_IZQUIERDA -> "Mano Izquierda"
                                        com.xoropower.data.EjerciciosData.ID_EJERCICIO_JUNTAS -> "Manos Juntas"
                                        com.xoropower.data.EjerciciosData.ID_INTERMEDIO_1 -> "Síncopa Intermedia"
                                        com.xoropower.data.EjerciciosData.ID_INTERMEDIO_2 -> "Semicorcheas Rápidas"
                                        com.xoropower.data.EjerciciosData.ID_INTERMEDIO_3 -> "Polirritmo 4 vs 3"
                                        com.xoropower.data.EjerciciosData.ID_AVANZADO_1 -> "Joropo Llanero (3+3+2)"
                                        com.xoropower.data.EjerciciosData.ID_AVANZADO_2 -> "Ritmo Irregular"
                                        com.xoropower.data.EjerciciosData.ID_AVANZADO_3 -> "Ostinato y Variación"
                                        else -> ejercicios.find { it.id == item.idEjercicio }?.titulo ?: "Ejercicio de Ritmo"
                                    }
                                    val nivelEjercicio = when (item.idEjercicio) {
                                        com.xoropower.data.EjerciciosData.ID_EJERCICIO_DERECHA,
                                        com.xoropower.data.EjerciciosData.ID_EJERCICIO_IZQUIERDA,
                                        com.xoropower.data.EjerciciosData.ID_EJERCICIO_JUNTAS -> "BÁSICO"
                                        com.xoropower.data.EjerciciosData.ID_INTERMEDIO_1,
                                        com.xoropower.data.EjerciciosData.ID_INTERMEDIO_2,
                                        com.xoropower.data.EjerciciosData.ID_INTERMEDIO_3 -> "INTERMEDIO"
                                        com.xoropower.data.EjerciciosData.ID_AVANZADO_1,
                                        com.xoropower.data.EjerciciosData.ID_AVANZADO_2,
                                        com.xoropower.data.EjerciciosData.ID_AVANZADO_3 -> "AVANZADO"
                                        else -> ejercicios.find { it.id == item.idEjercicio }?.nivel?.uppercase() ?: "BÁSICO"
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(XoroSurface)
                                            .border(
                                                1.dp, 
                                                if (item.completado) Color(0xFFFFD700).copy(0.15f) else Color.White.copy(0.04f), 
                                                RoundedCornerShape(20.dp)
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    nivelEjercicio,
                                                    color = if (nivelEjercicio.contains("BAS")) XoroBlue else Color(0xFFFFD700),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "Ritmo Veloz",
                                                    color = XoroWhite,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    nombreEjercicio,
                                                    color = XoroMuted,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Intentos: ${item.vecesIntentado ?: 1}",
                                                    color = XoroMuted,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    "Puntos",
                                                    color = XoroMuted,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    "${item.puntuacionMasAlta ?: 0}",
                                                    color = Color(0xFFFFD700), // Dorado
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (item.completado) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFFCF1020).copy(0.15f), // Rojo sutil
                                                        modifier = Modifier.border(1.dp, Color(0xFFCF1020).copy(0.3f), RoundedCornerShape(8.dp))
                                                    ) {
                                                        Text(
                                                            "COMPLETADO",
                                                            color = Color(0xFFFF5252),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvolutionChart(points: List<com.xoropower.data.PuntoHistorialDto>) {
    if (points.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(XoroSurface)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Practica para ver tu evolución de precisión semanal", 
                color = XoroMuted, 
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(XoroSurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            "Precisión Semanal Promedio", 
            color = XoroWhite, 
            fontSize = 16.sp, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingLeft = 60f
            val paddingRight = 40f
            val paddingTop = 20f
            val paddingBottom = 40f

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            val maxVal = 100f
            val minVal = 0f

            val xSteps = points.size
            val xIncrement = if (xSteps > 1) chartWidth / (xSteps - 1) else chartWidth

            val path = Path()
            val fillPath = Path()

            val coordinates = points.mapIndexed { index, punto ->
                val x = paddingLeft + index * xIncrement
                val yFraction = (punto.precisionPromedio - minVal) / (maxVal - minVal)
                val y = height - paddingBottom - (yFraction * chartHeight)
                Offset(x, y)
            }

            if (coordinates.isNotEmpty()) {
                // Dibujar líneas horizontales de cuadrícula (0%, 50%, 100%)
                val gridLines = listOf(0f, 0.5f, 1f)
                gridLines.forEach { fraction ->
                    val y = height - paddingBottom - (fraction * chartHeight)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(paddingLeft, y),
                        end = Offset(width - paddingRight, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                path.moveTo(coordinates[0].x, coordinates[0].y)
                fillPath.moveTo(coordinates[0].x, height - paddingBottom)
                fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                for (i in 1 until coordinates.size) {
                    val pPrev = coordinates[i - 1]
                    val pCurr = coordinates[i]
                    
                    // Curva de Bezier suave
                    val controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val controlY1 = pPrev.y
                    val controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                    val controlY2 = pCurr.y

                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y)
                }

                if (coordinates.size > 1) {
                    fillPath.lineTo(coordinates.last().x, height - paddingBottom)
                    fillPath.close()
                }

                // Dibujar relleno degradado debajo de la línea
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(XoroBlue.copy(alpha = 0.25f), Color.Transparent),
                        startY = coordinates.minOf { it.y },
                        endY = height - paddingBottom
                    )
                )

                // Dibujar línea principal
                drawPath(
                    path = path,
                    color = XoroBlue,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Dibujar puntos interactivos
                coordinates.forEachIndexed { index, offset ->
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 5.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = XoroBlue,
                        radius = 2.5.dp.toPx(),
                        center = offset
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Etiquetas de días
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { punto ->
                val dateLabel = try {
                    val parts = punto.fecha.split("T")[0].split("-")
                    if (parts.size >= 3) "${parts[2]}/${parts[1]}" else punto.fecha
                } catch (e: Exception) {
                    punto.fecha
                }
                Text(
                    text = dateLabel,
                    color = XoroMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

