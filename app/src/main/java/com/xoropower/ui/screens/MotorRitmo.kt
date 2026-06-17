package com.xoropower.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import com.xoropower.data.audio.MetronomoManager
import com.xoropower.data.EjerciciosData
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import com.xoropower.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Close


// Definición de colores oficiales para la paleta de xoropower (Rojo, Azul marino, Blanco, Dorado).
private val XRojo        = Color(0xFFCF1020)
private val XAzul        = Color(0xFF0D3B8E)
private val XAzulClaro   = Color(0xFF1A5CC8)
private val XBlanco      = Color(0xFFF5F5F5)
private val XOro         = Color(0xFFFFD700)
private val XFondo       = Color(0xFF121212) // Negro suave
private val XSuperficie  = Color(0xFF1C1C1E) // Negro más claro para tarjetas
private val XCursor      = Color(0xFF00FF99)
// Color cálido ámbar definido para emular la apariencia del papel de partitura tradicional en las líneas del pentagrama.
private val XStaffLine   = Color(0xFFD4A574)
// Definición de alias para los colores oficiales.
private val ColorAzul      = XAzulClaro
private val ColorRojo      = XRojo
private val ColorOro       = XOro
private val ColorFondo     = XFondo
private val ColorSuperficie = XSuperficie
private val ColorCursor    = XCursor

// Estructura de datos que representa una nota interna procesada por el motor de juego.
data class NotaRitmo(
    val id: Int,
    val ms: Long,
    val carril: String,
    val color: Color,
    val texto: String,
    var fueAcertada: Boolean = false,
    var fuePerdida: Boolean = false,
    val tipo: String = "negra"
)

// Interfaz composable para la pantalla que presenta el resumen de los resultados del ejercicio.
@Composable
fun PantallaResultado(
    puntuacion: Int,
    totalNotas: Int,
    onVolver: () -> Unit,
    onReintentar: () -> Unit
) {
    val aciertos = minOf(puntuacion / 10, totalNotas)
    val porcentaje = if (totalNotas > 0) (aciertos * 100) / totalNotas else 0
    
    val displayPrecision = porcentaje
    val displayAciertos = aciertos
    val displayFallos = (totalNotas - aciertos)
    val displayPerdidos = 0

    val estrellas = when {
        displayPrecision >= 90 -> 3
        displayPrecision >= 60 -> 2
        else             -> 1
    }
    val noCompletado = displayPrecision < 10 // Se considera no completado si el porcentaje es inferior al 10%.

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f, targetValue = 1.1f, label = "scale",
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(XFondo, Color(0xFF1A1A1C), XFondo)
                )
            )
    ) {
        // Franja superior decorativa.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.horizontalGradient(listOf(XRojo, Color(0xFFFF4D60), XRojo)))
        )

        // Elementos visuales decorativos a los lados (emojis semi-transparentes).
        Text(
            "🪘",
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-16).dp)
                .alpha(0.10f)
        )
        Text(
            "🪘",
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 16.dp)
                .alpha(0.10f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // Indicador o insignia (badge) en la parte superior.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(listOf(XRojo, Color(0xFFFF4D60))))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    "xoropower ACÚSTICO",
                    color = XBlanco,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icono principal con animación de escala.
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.radialGradient(listOf(XOro.copy(0.25f), Color.Transparent)),
                        CircleShape
                    )
                    .border(2.dp, XOro.copy(0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎵", fontSize = (38 * pulse).sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                if (noCompletado) "Ejercicio No Completado" else "¡Ejercicio Completado!",
                color = if (noCompletado) Color.Gray else XBlanco,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Indicadores de estrellas de aprobación
            if (!noCompletado) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (i < estrellas) XOro.copy(0.15f) else Color.Transparent,
                                    CircleShape
                                )
                                .border(1.5.dp, if (i < estrellas) XOro.copy(0.5f) else Color.Gray.copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (i < estrellas) XOro else Color.Gray.copy(0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta contenedora de estadísticas detalladas del rendimiento.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(XSuperficie)
                    .border(1.dp, XAzulClaro.copy(0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilaEstadistica("Puntuación", "$displayPrecision pts", XOro)
                    HorizontalDivider(color = XAzul.copy(0.2f))
                    FilaEstadistica("Aciertos / Fallos / Perdidos", "$displayAciertos / $displayFallos / $displayPerdidos", XAzulClaro)
                    HorizontalDivider(color = XAzul.copy(0.2f))
                    FilaEstadistica("Precisión Acústica", "$displayPrecision%", XCursor)
                }
            }

            // Contenedor de botones de interacción del usuario.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón destinado para reiniciar el ejercicio actual.
                Button(
                    onClick = onReintentar,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(XAzulClaro, XAzul)),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reintentar", color = XBlanco, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Botón destinado para regresar a la vista del módulo actual.
                Button(
                    onClick = onVolver,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(XRojo, Color(0xFFFF4D60))),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Volver al Módulo", color = XBlanco, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Franja inferior decorativa.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.horizontalGradient(listOf(XRojo, Color(0xFFFF4D60), XRojo)))
        )
    }
}

@Composable
private fun FilaEstadistica(label: String, valor: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 15.sp)
        Text(valor, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// Interfaz composable principal que implementa el motor interactivo de ritmo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorRitmo(
    titulo: String,
    listaNotas: List<RhythmNote>,
    tempoBpm: Int = 80,
    onVolverClick: () -> Unit,
    onFinalizarEjercicio: (Int) -> Unit,
    onReintentar: () -> Unit = {},
    idEjercicio: String = ""
) {
    var bpmAjustado    by remember(tempoBpm) { mutableIntStateOf(tempoBpm) }
    var enCountdown    by remember { mutableStateOf(false) }
    var countdownCount by remember { mutableIntStateOf(3) }
    var metronomoActivo by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val metronomoManager = remember { MetronomoManager() }

    DisposableEffect(Unit) {
        onDispose {
            metronomoManager.liberar()
        }
    }

    var estaJugando    by remember { mutableStateOf(false) }
    var tiempoActual   by remember { mutableLongStateOf(0L) }
    var puntuacion     by remember { mutableIntStateOf(0) }
    var juegoTerminado by remember { mutableStateOf(false) }
    
    var showVideoDialog by remember { mutableStateOf(false) }
    var wasPlayingBeforeDialog by remember { mutableStateOf(false) }

    val notasProcesadas = remember(listaNotas, bpmAjustado) {
        mutableStateListOf<NotaRitmo>().apply {
            val factor = tempoBpm.toFloat() / bpmAjustado
            val ordenadas = listaNotas.sortedBy { it.ms }
            ordenadas.forEachIndexed { index, current -> 
                val next = if (index + 1 < ordenadas.size) ordenadas[index + 1] else null
                val delta = if (next != null) next.ms - current.ms else 500L
                val tipo = when {
                    current.tipo.isNotBlank() && current.tipo != "negra" -> current.tipo
                    delta >= 900L -> "blanca"
                    delta <= 300L -> "corchea"
                    else -> "negra"
                }
                val msEscalado = (current.ms.toFloat() * factor).toLong()
                add(NotaRitmo(current.id, msEscalado, current.lane, current.color, current.text, tipo = tipo))
            }
        }
    }

    // Se verifica la presencia de notas para la mano derecha e izquierda en la secuencia.
    val hayDerecha = remember(notasProcesadas) { notasProcesadas.any { it.carril == "derecha" } }
    val hayIzquierda = remember(notasProcesadas) { notasProcesadas.any { it.carril == "izquierda" } }
    val esCombinado = remember(hayDerecha, hayIzquierda) { hayDerecha && hayIzquierda }

    val videoRes = remember(hayDerecha, hayIzquierda, esCombinado, idEjercicio) {
        when {
            idEjercicio == EjerciciosData.ID_INTERMEDIO_1 -> R.raw.ejerciciointermedio1
            idEjercicio == EjerciciosData.ID_INTERMEDIO_2 -> R.raw.ejercicio2int
            idEjercicio == EjerciciosData.ID_INTERMEDIO_3 -> R.raw.ejercicio3int
            idEjercicio == EjerciciosData.ID_AVANZADO_1 -> R.raw.ejercicio1avan
            idEjercicio == EjerciciosData.ID_AVANZADO_2 -> R.raw.ejercicio2avan
            idEjercicio == EjerciciosData.ID_AVANZADO_3 -> R.raw.ejercicio3avan
            esCombinado -> R.raw.joropo
            hayDerecha -> R.raw.derechaa
            hayIzquierda -> R.raw.izquierdaa
            else -> R.raw.joropo
        }
    }

    val msPerBeatAjustado = (60_000L / bpmAjustado.coerceAtLeast(40))
    val finRepeticionMs = remember(notasProcesadas, bpmAjustado) {
        val ultima = notasProcesadas.maxOfOrNull { it.ms } ?: 0L
        val beats = ((ultima + msPerBeatAjustado - 1) / msPerBeatAjustado).toInt().coerceAtLeast(4)
        val alineados = ((beats + 3) / 4) * 4
        alineados * msPerBeatAjustado
    }
    val notasJugables = remember(notasProcesadas) { notasProcesadas.filter { it.tipo != "silencio" } }
    val tiempoTotal = finRepeticionMs + 500L

    // Constantes de BPM (se calculan aquí para usarse en el Canvas)
    val msPerBeat = 60_000f / bpmAjustado

    // Se anima la opacidad de pulsación del cursor (efecto glow).
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.5f, targetValue = 1.0f, label = "glow",
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse)
    )

    // Se almacenan pinceles (Paints) nativos optimizados para evitar recolocaciones en el Canvas.
    val density = LocalDensity.current
    val paintDiv = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#C1B498") // Color líneas
            strokeWidth = with(density) { 2.dp.toPx() }
        }
    }

    val paintBadge = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 14.sp.toPx() }
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val paintBeat = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#D0C4A9") // Color números beat
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 24.sp.toPx() }
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }

    // Control de metrónomo reactivo
    LaunchedEffect(estaJugando, metronomoActivo) {
        if (estaJugando && metronomoActivo) {
            metronomoManager.iniciar(bpmAjustado)
        } else {
            metronomoManager.detener()
        }
    }

    // Corrutina del countdown
    LaunchedEffect(enCountdown) {
        if (enCountdown) {
            countdownCount = 3
            while (countdownCount > 0) {
                delay(1000L)
                countdownCount--
            }
            enCountdown = false
            estaJugando = true
        }
    }

    // Hilo de control de tiempo principal del temporizador del juego.
    LaunchedEffect(estaJugando) {
        if (estaJugando) {
            val inicio = System.currentTimeMillis() - tiempoActual
            while (estaJugando) {
                tiempoActual = System.currentTimeMillis() - inicio
                notasProcesadas.forEach { nota ->
                    if (nota.tipo == "silencio") return@forEach
                    if (!nota.fueAcertada && !nota.fuePerdida && tiempoActual > nota.ms + 250) {
                        nota.fuePerdida = true
                    }
                }
                if (tiempoActual > tiempoTotal) {
                    estaJugando = false
                    juegoTerminado = true
                    // Calcular porcentaje real 0–100 basado en notas acertadas
                    val maxPosible = notasJugables.size * 10
                    val porcentaje = if (maxPosible > 0) ((puntuacion.coerceAtMost(maxPosible) * 100) / maxPosible) else 0
                    onFinalizarEjercicio(porcentaje)
                }
                delay(16L)
            }
        }
    }

    // Visualización opcional de la pantalla de resultados.
    AnimatedVisibility(
        visible = juegoTerminado,
        enter = fadeIn() + scaleIn(),
        exit  = fadeOut() + scaleOut()
    ) {
        PantallaResultado(
            puntuacion   = puntuacion,
            totalNotas   = notasJugables.size,
            onVolver     = onVolverClick,
            onReintentar = {
                // Reiniciar el juego
                juegoTerminado = false
                estaJugando = false
                tiempoActual = 0L
                puntuacion = 0
                // Reiniciar el estado de las notas
                notasProcesadas.forEach { it.fueAcertada = false; it.fuePerdida = false }
                onReintentar()
            }
        )
    }

    // Interfaz visual principal durante la ejecución del juego.
    if (!juegoTerminado) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolverClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = {
                                if (estaJugando) {
                                    wasPlayingBeforeDialog = true
                                    estaJugando = false
                                } else {
                                    wasPlayingBeforeDialog = false
                                }
                                showVideoDialog = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = XOro.copy(alpha = 0.15f),
                                contentColor = XOro
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🎥", fontSize = 14.sp)
                                Text("VER GUÍA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor       = XAzul,
                        titleContentColor    = XBlanco,
                        navigationIconContentColor = XBlanco.copy(0.7f)
                    )
                )
            },
            containerColor = XFondo
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            listOf(XFondo, Color(0xFF18181A), XFondo)
                        )
                    )
            ) {
                // Franja superior decorativa.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Brush.horizontalGradient(listOf(XRojo, Color(0xFFFF4D60), XRojo)))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Bloque del marcador de puntos y notas acertadas.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(XSuperficie)
                            .border(1.dp, XAzulClaro.copy(0.25f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PUNTOS", color = XBlanco.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            Text(
                                "$puntuacion",
                                color = XOro,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text("🪘", fontSize = (24 * glowAlpha).sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("NOTAS", color = XBlanco.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            Text(
                                "${notasJugables.count { it.fueAcertada }} / ${notasJugables.size}",
                                color = XAzulClaro,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♩ = $bpmAjustado BPM",
                            color = XOro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Etiquetas de los carriles correspondientes a mano derecha y mano izquierda.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("▲ DERECHA", color = XRojo.copy(0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("▼ IZQUIERDA", color = XAzulClaro.copy(0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(6.dp))

                    // Canvas principal destinado para la renderización del área de juego.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFE4C9)) // Fondo Crema/Beige
                            .border(1.dp, Color(0xFFD4C8AD), RoundedCornerShape(8.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val ancho = size.width
                            val alto  = size.height

                            // Definición de colores locales del Canvas.
                            val cremaOscuro = Color(0xFFC4B89B)
                            val negroRepeat = Color(0xFF222222)
                            val rojoNota    = Color(0xFFE84242)
                            val azulNota    = Color(0xFF3388E8)

                            // Analizamos qué carriles hay en este ejercicio
                            val hayDerecha   = notasProcesadas.any { it.carril == "derecha" }
                            val hayIzquierda = notasProcesadas.any { it.carril == "izquierda" }
                            val esCombinado  = hayDerecha && hayIzquierda

                            // Posicionamiento de los carriles en el Canvas.
                            val yMid       = alto * 0.40f
                            val staffSpace = 36.dp.toPx()  // espacio entre líneas de pentagrama
                            
                            val yDerecha   = if (esCombinado) yMid - staffSpace else yMid
                            val yIzquierda = if (esCombinado) yMid + staffSpace else yMid

                            // Conversión de BPM a velocidad de desplazamiento horizontal en pixeles.
                            // Mantenido a 80 BPM para tempo correcto
                            // Ajustado beatWidthPx según tipo de ejercicio
                            val beatWidthPx = if (esCombinado) 180.dp.toPx() else 150.dp.toPx()
                            val pixelesPorMs = beatWidthPx / msPerBeat

                            val margenIzq  = 20.dp.toPx()
                            val cursorX    = margenIzq + 145.dp.toPx()

                            // ══════════════════════════════════════════
                            // Se dibujan de forma dinámica las líneas correspondientes según el ejercicio.
                            // ══════════════════════════════════════════
                            if (esCombinado) {
                                // Pentagrama completo de 5 líneas para manos juntas
                                for (i in -2..2) {
                                    val yLine = yMid + (i * staffSpace)
                                    drawLine(
                                        color = cremaOscuro,
                                        start = Offset(margenIzq, yLine),
                                        end   = Offset(ancho, yLine),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                // Resaltado sutil de las líneas guía
                                if (hayDerecha) {
                                    drawLine(
                                        color = rojoNota.copy(alpha = 0.35f),
                                        start = Offset(margenIzq, yDerecha),
                                        end   = Offset(ancho, yDerecha),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                if (hayIzquierda) {
                                    drawLine(
                                        color = azulNota.copy(alpha = 0.35f),
                                        start = Offset(margenIzq, yIzquierda),
                                        end   = Offset(ancho, yIzquierda),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            } else {
                            // Una sola mano: pentagrama completo de 5 líneas igual que manos juntas
                            for (i in -2..2) {
                                val yLine = yMid + (i * staffSpace)
                                drawLine(
                                    color = cremaOscuro,
                                    start = Offset(margenIzq, yLine),
                                    end   = Offset(ancho, yLine),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            // Resaltado de la línea central (línea 3)
                            val activeColor = if (hayDerecha) rojoNota else azulNota
                            drawLine(
                                color = activeColor.copy(alpha = 0.35f),
                                start = Offset(margenIzq, yMid),
                                end   = Offset(ancho, yMid),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                            // ══════════════════════════════════════════
                            // Renderizado de las insignias indicativas de cada mano (D y I) y compás (4/4).
                            // ══════════════════════════════════════════
                            val badgeRadius = 18.dp.toPx()
                            val badgeX_4_4 = margenIzq + 20.dp.toPx()
                            val badgeX_DI = if (esCombinado) margenIzq + 60.dp.toPx() else margenIzq + 20.dp.toPx()

                            if (esCombinado) {
                                // Dibujar compás 4/4 apilado (firma de tiempo)
                                drawIntoCanvas { canvas ->
                                    val paintTimeSignature = android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#222222")
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        textSize = 48.sp.toPx()
                                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    }
                                    canvas.nativeCanvas.drawText("4", badgeX_4_4, yMid - 4.dp.toPx(), paintTimeSignature)
                                    canvas.nativeCanvas.drawText("4", badgeX_4_4, yMid + staffSpace * 1.3f, paintTimeSignature)
                                }
                                // Línea horizontal divisoria del compás (fracción 4/4)
                                drawLine(
                                    color = Color(0xFF222222),
                                    start = Offset(badgeX_4_4 - 18.dp.toPx(), yMid),
                                    end = Offset(badgeX_4_4 + 18.dp.toPx(), yMid),
                                    strokeWidth = 3.5.dp.toPx()
                                )
                            }

                            if (hayDerecha) {
                                // Sombra
                                drawCircle(color = Color.Black.copy(alpha = 0.15f), radius = badgeRadius, center = Offset(badgeX_DI, yDerecha + 4.dp.toPx()))
                                // Círculo principal
                                drawCircle(color = rojoNota, radius = badgeRadius, center = Offset(badgeX_DI, yDerecha))
                                // Borde
                                drawCircle(color = rojoNota.copy(alpha = 0.6f), radius = badgeRadius + 3.dp.toPx(), center = Offset(badgeX_DI, yDerecha), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                                drawIntoCanvas { canvas -> canvas.nativeCanvas.drawText("D", badgeX_DI, yDerecha + 5.dp.toPx(), paintBadge) }
                            }
                            if (hayIzquierda) {
                                drawCircle(color = Color.Black.copy(alpha = 0.15f), radius = badgeRadius, center = Offset(badgeX_DI, yIzquierda + 4.dp.toPx()))
                                drawCircle(color = azulNota, radius = badgeRadius, center = Offset(badgeX_DI, yIzquierda))
                                drawCircle(color = azulNota.copy(alpha = 0.6f), radius = badgeRadius + 3.dp.toPx(), center = Offset(badgeX_DI, yIzquierda), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                                drawIntoCanvas { canvas -> canvas.nativeCanvas.drawText("I", badgeX_DI, yIzquierda + 5.dp.toPx(), paintBadge) }
                            }

                            // ══════════════════════════════════════════
                            // Dibujo del signo de repetición musical (inicio) y doble barra.
                            // ══════════════════════════════════════════
                            // 1. Doble barra inicial — continua de arriba a abajo
                            val doubleBarX1 = margenIzq + 90.dp.toPx()
                            val doubleBarX2 = doubleBarX1 + 13.dp.toPx()
                            val fullBarTop = if (esCombinado) yMid - 2 * staffSpace else yMid - staffSpace
                            val fullBarBottom = if (esCombinado) yMid + 2 * staffSpace else yMid + staffSpace

                            drawLine(color = negroRepeat, start = Offset(doubleBarX1, fullBarTop), end = Offset(doubleBarX1, fullBarBottom), strokeWidth = 8.dp.toPx())
                            drawLine(color = negroRepeat, start = Offset(doubleBarX2, fullBarTop), end = Offset(doubleBarX2, fullBarBottom), strokeWidth = 3.dp.toPx())

                            // 2. Puntos a la derecha de las barras
                            val dotX = doubleBarX2 + 12.dp.toPx()
                            if (esCombinado) {
                                // Barras cortadas internas (arriba y abajo, separadas en el centro)
                                val repX1 = doubleBarX2 + 18.dp.toPx()
                                val repX2 = repX1 + 12.dp.toPx()
                                drawLine(color = negroRepeat, start = Offset(repX1, yMid - 2 * staffSpace + 8.dp.toPx()), end = Offset(repX1, yMid - 20.dp.toPx()), strokeWidth = 5.dp.toPx())
                                drawLine(color = negroRepeat, start = Offset(repX2, yMid - 2 * staffSpace + 8.dp.toPx()), end = Offset(repX2, yMid - 20.dp.toPx()), strokeWidth = 5.dp.toPx())
                                drawLine(color = negroRepeat, start = Offset(repX1, yMid + 20.dp.toPx()), end = Offset(repX1, yMid + 2 * staffSpace - 8.dp.toPx()), strokeWidth = 5.dp.toPx())
                                drawLine(color = negroRepeat, start = Offset(repX2, yMid + 20.dp.toPx()), end = Offset(repX2, yMid + 2 * staffSpace - 8.dp.toPx()), strokeWidth = 5.dp.toPx())    
                            // Puntos a la derecha de las barras cortadas - separados del centro
                                val dotX = repX2 + 10.dp.toPx()
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(dotX, yMid - 1.4f * staffSpace))
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(dotX, yMid - 0.9f * staffSpace))
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(dotX, yMid + 0.9f * staffSpace))
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(dotX, yMid + 1.4f * staffSpace))
                            } else {
                            drawLine(color = negroRepeat, start = Offset(doubleBarX1, yMid - 2 * staffSpace), end = Offset(doubleBarX1, yMid + 2 * staffSpace), strokeWidth = 8.dp.toPx())
                            drawLine(color = negroRepeat, start = Offset(doubleBarX2, yMid - 2 * staffSpace), end = Offset(doubleBarX2, yMid + 2 * staffSpace), strokeWidth = 3.dp.toPx())
                            val repX1 = doubleBarX2 + 14.dp.toPx()
                            val repX2 = repX1 + 8.dp.toPx()
                            val startY = yMid - 0.8f * staffSpace
                            val endY = yMid + 0.8f * staffSpace
                            drawLine(color = negroRepeat, start = Offset(repX1, startY), end = Offset(repX1, endY), strokeWidth = 4.dp.toPx())
                            drawLine(color = negroRepeat, start = Offset(repX2, startY), end = Offset(repX2, endY), strokeWidth = 4.dp.toPx())
                            val dotRepX = repX2 + 10.dp.toPx()
                            drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(dotRepX, yMid - 0.4f * staffSpace))
                            drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(dotRepX, yMid + 0.4f * staffSpace))
                            }

                            // ══════════════════════════════════════════
                            // Renderizado de los números de pulso (beats) a lo largo del pentagrama.
                            // ══════════════════════════════════════════
                            if (notasProcesadas.isNotEmpty()) {
                                val primerNota = notasProcesadas.minOf { it.ms }
                                val ultimaNota = notasProcesadas.maxOf { it.ms }

                                var beatMs  = primerNota
                                var beatNum = 1
                                while (beatMs <= ultimaNota + msPerBeat) {
                                    val xBeat = cursorX + (beatMs - tiempoActual) * pixelesPorMs
                                    if (xBeat > margenIzq + 20.dp.toPx() && xBeat < ancho + 50.dp.toPx()) {
                                        drawIntoCanvas { canvas ->
                                            canvas.nativeCanvas.drawText(
                                                beatNum.toString(),
                                                xBeat,
                                                yMid + (4.0f * staffSpace), // Abajo de todo
                                                paintBeat
                                            )
                                        }
                                    }
                                    beatMs += msPerBeat.toLong()
                                    beatNum = if (beatNum >= 4) 1 else beatNum + 1
                                }
                            }

                            // ══════════════════════════════════════════
                            // Renderizado individual y dibujo de la secuencia de notas.
                            // ══════════════════════════════════════════
                            val primerNotaDerecha = notasProcesadas.firstOrNull { it.tipo != "silencio" && it.carril == "derecha" }
                            val primerNotaIzquierda = notasProcesadas.firstOrNull { it.tipo != "silencio" && it.carril == "izquierda" }
                            val ultimaNotaDerecha = notasProcesadas.lastOrNull { it.tipo != "silencio" && it.carril == "derecha" }
                            val ultimaNotaIzquierda = notasProcesadas.lastOrNull { it.tipo != "silencio" && it.carril == "izquierda" }

                            notasProcesadas.forEach { nota ->
                                val xPos = cursorX + (nota.ms - tiempoActual) * pixelesPorMs
                                
                                // El texto indica la dirección del golpe ("arriba" o "abajo")
                                // La cabeza de la nota se queda SIEMPRE en su carril correspondiente
                                val yPos = if (nota.carril == "derecha") yDerecha else yIzquierda
                                
                                val esPrimerNota = (nota.carril == "derecha" && nota.id == primerNotaDerecha?.id) ||
                                                   (nota.carril == "izquierda" && nota.id == primerNotaIzquierda?.id)
                                val esUltimaNota = (nota.carril == "derecha" && nota.id == ultimaNotaDerecha?.id) ||
                                                   (nota.carril == "izquierda" && nota.id == ultimaNotaIzquierda?.id)

                                // Fuerzo nota inicial siempre "hacia arriba" (stemUp = true)
                                // La última nota respeta la configuración de EjerciciosData.kt
                                val esArriba = if (esPrimerNota) true else (nota.texto != "abajo")

                                // No dibujar si salió por la izquierda o está detrás del margen
                                if (xPos < margenIzq - 20.dp.toPx()) return@forEach

                                val colorBase = if (nota.carril == "derecha") rojoNota else azulNota

                                val alpha = when {
                                    nota.fueAcertada -> 0.15f
                                    nota.fuePerdida  -> 0.08f
                                    else             -> 1.0f
                                }

                                if (nota.tipo == "silencio") {
                                    val w = 18.dp.toPx()
                                    val h = 6.dp.toPx()
                                    drawRect(
                                        colorBase.copy(alpha = alpha),
                                        topLeft = Offset(xPos - w / 2, yPos - h / 2),
                                        size = androidx.compose.ui.geometry.Size(w, h)
                                    )
                                    return@forEach
                                }

                                withTransform({
                                    translate(left = xPos, top = yPos)
                                    rotate(degrees = -20f, pivot = Offset.Zero)
                                }) {
                                    if (nota.tipo == "blanca") {
                                        drawOval(
                                            color = colorBase.copy(alpha = alpha),
                                            topLeft = Offset(-12.dp.toPx(), -8.5.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 17.dp.toPx()),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
                                        )
                                    } else {
                                        // Borde muy sutil para que contraste con la línea de fondo
                                        drawOval(
                                            color = cremaOscuro.copy(alpha = alpha * 0.5f),
                                            topLeft = Offset(-13.dp.toPx(), -9.5.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(26.dp.toPx(), 19.dp.toPx())
                                        )
                                        // Nota sólida
                                        drawOval(
                                            color = colorBase.copy(alpha = alpha),
                                            topLeft = Offset(-12.dp.toPx(), -8.5.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(24.dp.toPx(), 17.dp.toPx())
                                        )
                                    }
                                }

                                // Plica (stem): Según la dirección del golpe
                                val stemUp = esArriba
                                val stemX  = if (stemUp) xPos + 10.dp.toPx() else xPos - 10.dp.toPx()
                                val stemEndY = if (stemUp) yPos - 36.dp.toPx() else yPos + 36.dp.toPx()
                                drawLine(
                                    color = colorBase.copy(alpha = alpha * 0.9f),
                                    start = Offset(stemX, yPos),
                                    end   = Offset(stemX, stemEndY),
                                    strokeWidth = 3.dp.toPx()
                                )

                                // Corchete (flag) para corcheas
                                if (nota.tipo == "corchea") {
                                    val flagEndX = stemX + 14.dp.toPx()
                                    val flagEndY = if (stemUp) stemEndY + 14.dp.toPx() else stemEndY - 14.dp.toPx()
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(stemX, stemEndY)
                                        quadraticBezierTo(stemX + 10.dp.toPx(), stemEndY, flagEndX, flagEndY)
                                        quadraticBezierTo(
                                            stemX + 10.dp.toPx(),
                                            stemEndY + (if (stemUp) 5.dp.toPx() else -5.dp.toPx()),
                                            stemX,
                                            stemEndY + (if (stemUp) 7.dp.toPx() else -7.dp.toPx())
                                        )
                                        close()
                                    }
                                    drawPath(path = path, color = colorBase.copy(alpha = alpha * 0.9f))
                                }
                            }

                            // Final repetición :|| al cierre del compás
                            val xRepeatEnd = cursorX + (finRepeticionMs + (msPerBeat / 2).toLong() - tiempoActual) * pixelesPorMs

                            if (xRepeatEnd > margenIzq && xRepeatEnd < ancho + 100.dp.toPx()) {
                            val fullBarTop = yMid - 2 * staffSpace
                            val fullBarBottom = yMid + 2 * staffSpace
                                if (esCombinado) {
                                val endDotX = xRepeatEnd - 42.dp.toPx()
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(endDotX, yMid - 1.4f * staffSpace))
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(endDotX, yMid - 0.9f * staffSpace))
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(endDotX, yMid + 0.9f * staffSpace))
                                drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(endDotX, yMid + 1.4f * staffSpace))
                                val endFineX = endDotX + 14.dp.toPx()
                                val endThickX = endFineX + 10.dp.toPx()
                            drawLine(color = negroRepeat, start = Offset(endFineX, fullBarTop), end = Offset(endFineX, fullBarBottom), strokeWidth = 1.5.dp.toPx())
                                drawLine(color = negroRepeat, start = Offset(endThickX, fullBarTop), end = Offset(endThickX, fullBarBottom), strokeWidth = 8.dp.toPx())
                            } else {
                            // Una sola mano: mismo cierre de repetición que manos juntas
                            val endDotX = xRepeatEnd - 42.dp.toPx()
                            drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(endDotX, yMid - 0.5f * staffSpace))
                            drawCircle(color = negroRepeat, radius = 3.5.dp.toPx(), center = Offset(endDotX, yMid + 0.5f * staffSpace))
                            val endFineX  = endDotX + 14.dp.toPx()
                            val endThickX = endFineX + 10.dp.toPx()
                            drawLine(color = negroRepeat, start = Offset(endFineX,  fullBarTop), end = Offset(endFineX,  fullBarBottom), strokeWidth = 1.5.dp.toPx())
                            drawLine(color = negroRepeat, start = Offset(endThickX, fullBarTop), end = Offset(endThickX, fullBarBottom), strokeWidth = 8.dp.toPx())
                            }
                            }

                            // ══════════════════════════════════════════
                            // Renderizado de la línea indicativa del pulso del cursor de tiempo.
                            // ══════════════════════════════════════════
                            // Línea principal
                            drawLine(
                                color       = Color.White.copy(alpha = glowAlpha * 0.7f),
                                start       = Offset(cursorX, yMid - 2.5f * staffSpace),
                                end         = Offset(cursorX, yMid + 3.5f * staffSpace),
                                strokeWidth = 4.dp.toPx()
                            )

                            // Hit markers
                            if (hayDerecha) drawCircle(color = rojoNota.copy(alpha = 0.5f), radius = 10.dp.toPx(), center = Offset(cursorX, yDerecha))
                            if (hayIzquierda) drawCircle(color = azulNota.copy(alpha = 0.5f), radius = 10.dp.toPx(), center = Offset(cursorX, yIzquierda))
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // ── CONTROLES ───────────────────────────────────────────
                    if (!estaJugando && tiempoActual == 0L && !enCountdown) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(XSuperficie)
                                .border(1.dp, XAzulClaro.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "AJUSTAR VELOCIDAD",
                                color = XBlanco.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "$bpmAjustado BPM",
                                color = XOro,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(2.dp))
                            Slider(
                                value = bpmAjustado.toFloat(),
                                onValueChange = { bpmAjustado = (it / 5).toInt() * 5 },
                                valueRange = 40f..200f,
                                colors = SliderDefaults.colors(
                                    thumbColor = XOro,
                                    activeTrackColor = XOro,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).height(32.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            // Botón de inicio — rojo xoropower
                            Button(
                                onClick = { enCountdown = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape  = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(listOf(XRojo, Color(0xFFFF4D60))),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🪘", fontSize = 22.sp)
                                        Text(
                                            "¡COMENZAR!",
                                            color      = XBlanco,
                                            fontSize   = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    } else if (enCountdown) {
                        // Countdown visual
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(XSuperficie)
                                .border(1.dp, XOro.copy(0.3f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "countdown_pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )
                            Text(
                                text = "$countdownCount",
                                color = XOro,
                                fontSize = 80.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.scale(pulseScale)
                            )
                        }
                    } else if (!juegoTerminado && !enCountdown) {
                        // Botones de interacción para presionar durante el ejercicio
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón izquierdo (azul)
                            Button(
                                onClick = {
                                    // Lógica para presionar izquierda
                                    notasProcesadas.forEach { nota ->
                                        if (!nota.fueAcertada && !nota.fuePerdida && 
                                            nota.carril == "izquierda" && 
                                            tiempoActual >= nota.ms - 150 && 
                                            tiempoActual <= nota.ms + 150) {
                                            nota.fueAcertada = true
                                            puntuacion += 10
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = XAzulClaro),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🔵", fontSize = 32.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "IZQUIERDA",
                                            color = XBlanco,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // Botón derecho (rojo)
                            Button(
                                onClick = {
                                    // Lógica para presionar derecha
                                    notasProcesadas.forEach { nota ->
                                        if (!nota.fueAcertada && !nota.fuePerdida && 
                                            nota.carril == "derecha" && 
                                            tiempoActual >= nota.ms - 150 && 
                                            tiempoActual <= nota.ms + 150) {
                                            nota.fueAcertada = true
                                            puntuacion += 10
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = XRojo),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🔴", fontSize = 32.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "DERECHA",
                                            color = XBlanco,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // ── OVERLAY DE COUNTDOWN ─────────────────────────────────
                if (enCountdown) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        var scale by remember { mutableStateOf(1f) }
                        var alpha by remember { mutableStateOf(1f) }
                        
                        LaunchedEffect(countdownCount) {
                            scale = 0.5f
                            alpha = 0f
                            animate(
                                initialValue = 0.5f,
                                targetValue = 2.5f,
                                animationSpec = tween(900, easing = FastOutSlowInEasing)
                            ) { value, _ ->
                                scale = value
                                alpha = (2.5f - value).coerceIn(0f, 1f)
                            }
                        }
                        
                        Text(
                            text = if (countdownCount > 0) "$countdownCount" else "¡YA!",
                            color = XOro,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .scale(scale)
                                .alpha(alpha)
                        )
                    }
                }
            }
        }
    }

    if (showVideoDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showVideoDialog = false
            if (wasPlayingBeforeDialog) {
                estaJugando = true
            }
        }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = XSuperficie,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(1.dp, XAzulClaro.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Guía del Ejercicio",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        IconButton(
                            onClick = {
                                showVideoDialog = false
                                if (wasPlayingBeforeDialog) {
                                    estaJugando = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.Black)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    val uri = android.net.Uri.parse("android.resource://${ctx.packageName}/$videoRes")
                                    setVideoURI(uri)
                                    val mediaController = android.widget.MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showVideoDialog = false
                            if (wasPlayingBeforeDialog) {
                                estaJugando = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = XRojo),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("ENTENDIDO", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BotonAccion(
    modifier: Modifier,
    gradiente: Brush,
    etiqueta: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape  = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradiente, RoundedCornerShape(20.dp))
                .border(1.dp, XBlanco.copy(0.15f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(etiqueta, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ── LÓGICA DE DETECCIÓN ────────────────────────────────────────────────────────
fun verificarAcierto(
    notas: MutableList<NotaRitmo>,
    tiempoActual: Long,
    carril: String,
    alAcertar: () -> Unit
) {
    val margen = 250L
    val nota = notas.find {
        it.carril == carril &&
        it.tipo != "silencio" &&
        !it.fueAcertada &&
        !it.fuePerdida &&
        tiempoActual >= it.ms - margen &&
        tiempoActual <= it.ms + margen
    }
    nota?.let {
        it.fueAcertada = true
        alAcertar()
    }
}
