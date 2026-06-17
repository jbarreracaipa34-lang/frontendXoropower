package com.xoropower.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Estructura de la nota
data class RhythmNote(
    val id: Int,
    val ms: Long,
    val lane: String, // "izquierda" o "derecha"
    val color: Color,
    val text: String,
    var isHit: Boolean = false,
    var isMissed: Boolean = false,
    val tipo: String = "negra" // negra, blanca, silencio, corchea
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    onBackClick: () -> Unit
) {
    // Estado del juego
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var score by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // Notas de ejemplo (Esto vendrá del backend después)
    // Sin nombres de notas porque es instrumento de percusión
    val notes = remember {
        mutableStateListOf(
            // Compás 1: Negras (4 tiempos a 80 BPM = 750ms por beat)
            RhythmNote(1, 750, "derecha", Color(0xFFF44336), ""),
            RhythmNote(2, 1500, "derecha", Color(0xFFF44336), ""),
            RhythmNote(3, 2250, "derecha", Color(0xFFF44336), ""),
            RhythmNote(4, 3000, "derecha", Color(0xFFF44336), ""),
            
            // Compás 2: Negras alternando
            RhythmNote(5, 3750, "izquierda", Color(0xFF2196F3), ""),
            RhythmNote(6, 4500, "izquierda", Color(0xFF2196F3), ""),
            RhythmNote(7, 5250, "izquierda", Color(0xFF2196F3), ""),
            RhythmNote(8, 6000, "izquierda", Color(0xFF2196F3), "")
        )
    }

    // Configuración visual
    val laneHeight = 80.dp
    val density = LocalDensity.current
    val pxPerMs = 0.2f // Velocidad de la línea verde

    // Lógica del temporizador
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val startTime = System.currentTimeMillis()
            while (isPlaying) {
                currentTime = System.currentTimeMillis() - startTime
                
                // Marcar como perdidas las notas que ya pasaron
                notes.forEach { note ->
                    if (!note.isHit && !note.isMissed && currentTime > note.ms + 200) {
                        note.isMissed = true
                    }
                }
                
                if (currentTime > 9000) isPlaying = false // Fin del ejercicio
                delay(16) // ~60fps
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Escala de Iniciación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Panel de puntuación
            Text(
                text = "PUNTUACIÓN: $score",
                color = Color.Yellow,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ÁREA DE JUEGO (Songsterr Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(laneHeight * 2 + 40.dp)
                    .background(Color(0xFF222222))
                    .padding(vertical = 20.dp)
            ) {
                // Dibujo de carriles y notas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val midY = size.height / 2

                    // Líneas horizontales (Lanes)
                    drawLine(Color.Gray, Offset(0f, midY - 40.dp.toPx()), Offset(width, midY - 40.dp.toPx()), strokeWidth = 2f)
                    drawLine(Color.Gray, Offset(0f, midY + 40.dp.toPx()), Offset(width, midY + 40.dp.toPx()), strokeWidth = 2f)

                    // Dibujar Notas
                    notes.forEach { note ->
                        val xPos = note.ms * pxPerMs
                        val yPos = if (note.lane == "derecha") midY - 40.dp.toPx() else midY + 40.dp.toPx()
                        
                        val alpha = if (note.isHit) 0.3f else if (note.isMissed) 0.1f else 1.0f
                        
                        drawCircle(
                            color = note.color.copy(alpha = alpha),
                            radius = 20.dp.toPx(),
                            center = Offset(xPos - (currentTime * pxPerMs) + 50.dp.toPx(), yPos)
                        )

                    }

                    // LÍNEA VERDE (Songsterr Cursor)
                    drawLine(
                        color = Color.Green,
                        start = Offset(50.dp.toPx(), 0f),
                        end = Offset(50.dp.toPx(), size.height),
                        strokeWidth = 8f
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTONES DE INTERACCIÓN
            if (!isPlaying && currentTime == 0L) {
                Button(
                    onClick = { isPlaying = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("EMPEZAR EJERCICIO", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón Izquierdo (Azul)
                    GameButton(
                        color = Color(0xFF2196F3),
                        label = "IZQUIERDA",
                        onClick = {
                            checkHit(notes, currentTime, "izquierda") { score += 10 }
                        }
                    )

                    // Botón Derecho (Rojo)
                    GameButton(
                        color = Color(0xFFF44336),
                        label = "DERECHA",
                        onClick = {
                            checkHit(notes, currentTime, "derecha") { score += 10 }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameButton(color: Color, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(140.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// Lógica de detección de acierto
fun checkHit(notes: List<RhythmNote>, currentTime: Long, lane: String, onHit: () -> Unit) {
    val window = 200L // Margen de error de 200ms
    val note = notes.find { 
        it.lane == lane && 
        !it.isHit && 
        !it.isMissed && 
        currentTime >= it.ms - window && 
        currentTime <= it.ms + window 
    }
    
    note?.let {
        it.isHit = true
        onHit()
    }
}
