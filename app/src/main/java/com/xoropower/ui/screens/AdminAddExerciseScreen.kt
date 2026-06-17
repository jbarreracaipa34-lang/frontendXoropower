package com.xoropower.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xoropower.data.*
import com.xoropower.data.EjercicioEditorLogic
import com.xoropower.data.EditorSlot
import com.xoropower.data.network.RetrofitClient
import com.xoropower.data.audio.PreviewMetronomoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddExerciseScreen(onVolverClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var nivel by remember { mutableStateOf("basico") }
    var compas by remember { mutableStateOf("4/4") }
    var videoUrl by remember { mutableStateOf("") }
    var videoBase64 by remember { mutableStateOf<String?>(null) }
    var videoExtension by remember { mutableStateOf<String?>(null) }
    var videoFileName by remember { mutableStateOf<String?>(null) }
    var pasoAPaso by remember { mutableStateOf("") }

    // Launcher para seleccionar video del celular
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream = context.contentResolver.openInputStream(it) ?: return@let
                val bytes = inputStream.readBytes()
                videoBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                videoExtension = context.contentResolver.getType(it)?.split("/")?.lastOrNull() ?: "mp4"
                videoFileName = "video_local.${videoExtension}"
                videoUrl = "" // Limpiar URL externa si se selecciona archivo local
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val beatsPerMeasure = if (compas == "3/4") 3 else 4
    var numCompases by remember { mutableIntStateOf(1) }
    var bpm by remember { mutableIntStateOf(80) }
    var pulsoActual by remember { mutableIntStateOf(0) }
    var slots by remember { mutableStateOf(EjercicioEditorLogic.slotsVacios(4, 1)) }

    // Función para verificar si el compás actual está lleno y agregar uno nuevo
    fun verificarYAgregarCompas() {
        val compasActual = (pulsoActual / beatsPerMeasure) + 1
        if (compasActual == numCompases && numCompases < 4) {
            // Verificar si el último compás está completamente lleno (todas las posiciones tienen notas)
            val indiceInicioUltimoCompas = (numCompases - 1) * beatsPerMeasure
            val ultimoCompasLleno = slots.indices
                .filter { it >= indiceInicioUltimoCompas && it < indiceInicioUltimoCompas + beatsPerMeasure }
                .all { index ->
                    val slot = slots[index]
                    slot.derecha != null || slot.izquierda != null
                }
            
            if (ultimoCompasLleno) {
                numCompases++
                slots = slots + List(beatsPerMeasure) { EditorSlot() }
            }
        }
    }

    var previewActivo by remember { mutableStateOf(false) }
    var playheadMs by remember { mutableLongStateOf(0L) }
    var enConteo by remember { mutableStateOf(false) }
    var conteoBeatActual by remember { mutableIntStateOf(-1) }
    var notaSeleccionada by remember { mutableStateOf<NotaPartituraVisual?>(null) }

    LaunchedEffect(beatsPerMeasure, numCompases) {
        // Mantener las notas existentes cuando cambia el compás o número de compases
        val nuevoTamano = beatsPerMeasure * numCompases
        if (slots.size != nuevoTamano) {
            val nuevosSlots = if (nuevoTamano > slots.size) {
                // Agregar slots vacíos al final
                val slotsAAgregar = nuevoTamano - slots.size
                slots + List(slotsAAgregar) { EditorSlot() }
            } else {
                // Recortar slots del final
                slots.take(nuevoTamano)
            }
            slots = nuevosSlots
        }
        pulsoActual = 0
        notaSeleccionada = null
    }
    
    val metronomoPlayer = remember { PreviewMetronomoPlayer() }
    
    DisposableEffect(Unit) {
        onDispose {
            metronomoPlayer.liberar()
        }
    }

    var enviando by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mostrarMetadata by remember { mutableStateOf(false) }
    var nivelExpanded by remember { mutableStateOf(false) }
    var compasExpanded by remember { mutableStateOf(false) }

    val finRepeticionMs = EjercicioEditorLogic.duracionTotalMs(bpm, beatsPerMeasure, numCompases)
    val notasVisuales = EjercicioEditorLogic.aPartituraVisual(slots, bpm, beatsPerMeasure)
    val msPorPulso = EjercicioEditorLogic.msPorPulso(bpm)

    LaunchedEffect(previewActivo, bpm, beatsPerMeasure, finRepeticionMs) {
        if (!previewActivo) {
            enConteo = false
            conteoBeatActual = -1
            playheadMs = 0L
            return@LaunchedEffect
        }
        
        val msBeat = msPorPulso
        
        // 1. Fase de conteo (count-in) - 1 compás de parpadeo antes de play
        enConteo = true
        conteoBeatActual = 0
        
        val conteoTotalMs = beatsPerMeasure * msBeat
        var conteoMs = 0L
        var ultimoBeatSonado = -1
        
        val tickInterval = 16L
        
        // Suena el primer tick del conteo inmediatamente
        metronomoPlayer.playConteo()
        ultimoBeatSonado = 0
        conteoBeatActual = 0
        
        while (conteoMs < conteoTotalMs && previewActivo) {
            delay(tickInterval)
            conteoMs += tickInterval
            val currentBeat = (conteoMs / msBeat).toInt().coerceAtMost(beatsPerMeasure - 1)
            conteoBeatActual = currentBeat
            if (currentBeat > ultimoBeatSonado && currentBeat < beatsPerMeasure) {
                metronomoPlayer.playConteo()
                ultimoBeatSonado = currentBeat
            }
        }
        
        // Reset estados de conteo
        enConteo = false
        conteoBeatActual = -1
        
        // 2. Fase de reproducción con metrónomo acentuado
        playheadMs = 0L
        var playbackMs = 0L
        ultimoBeatSonado = -1
        
        // Suena el primer pulso (acento de inicio de compás)
        metronomoPlayer.playAccent()
        ultimoBeatSonado = 0
        
        while (playbackMs < finRepeticionMs && previewActivo) {
            delay(tickInterval)
            playbackMs += tickInterval
            playheadMs = playbackMs
            
            val currentBeat = (playbackMs / msBeat).toInt()
            if (currentBeat > ultimoBeatSonado && playbackMs < finRepeticionMs) {
                if (currentBeat % beatsPerMeasure == 0) {
                    metronomoPlayer.playAccent()
                } else {
                    metronomoPlayer.playNormal()
                }
                ultimoBeatSonado = currentBeat
            }
        }
        
        previewActivo = false
        playheadMs = 0L
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0A14), Color(0xFF101028))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("PANEL DE ADMINISTRADOR", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text("Crear Ejercicio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))

            PartituraRitmoCanvas(
                notas = notasVisuales,
                bpm = bpm,
                compas = compas,
                beatsPerMeasure = beatsPerMeasure,
                finRepeticionMs = finRepeticionMs,
                playheadMs = if (previewActivo) playheadMs else msPorPulso * (pulsoActual + 1),
                modoEditor = true,
                pulsoActual = pulsoActual,
                notaSeleccionada = notaSeleccionada,
                enConteo = enConteo,
                conteoBeatActual = conteoBeatActual,
                previewActivo = previewActivo,
                onPulsoTocado = {
                    pulsoActual = it
                    notaSeleccionada = null
                },
                onNotaTocada = {
                    notaSeleccionada = it
                    if (it != null) {
                        val index = (it.ms / msPorPulso - 1).toInt().coerceIn(slots.indices)
                        pulsoActual = index
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Pulso ${(pulsoActual % beatsPerMeasure) + 1} de $beatsPerMeasure · Compás ${(pulsoActual / beatsPerMeasure) + 1}/$numCompases",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ManoEditorPanel(
                    titulo = "Mano Derecha",
                    color = Color(0xFFE53935),
                    modifier = Modifier.weight(1f),
                    onNegraArriba = {
                        if (pulsoActual < slots.size) {
                            slots = EjercicioEditorLogic.aplicarFigura(slots, pulsoActual, "derecha", FiguraRitmica.NEGRA, "arriba")
                            notaSeleccionada = null
                            verificarYAgregarCompas()
                        }
                    },
                    onNegraAbajo = {
                        if (pulsoActual < slots.size) {
                            slots = EjercicioEditorLogic.aplicarFigura(slots, pulsoActual, "derecha", FiguraRitmica.NEGRA, "abajo")
                            notaSeleccionada = null
                            verificarYAgregarCompas()
                        }
                    },
                    onSilencio = {
                        if (pulsoActual < slots.size) {
                            slots = EjercicioEditorLogic.aplicarFigura(slots, pulsoActual, "derecha", FiguraRitmica.SILENCIO, "abajo")
                            notaSeleccionada = null
                            verificarYAgregarCompas()
                        }
                    }
                )
                
                ManoEditorPanel(
                    titulo = "Mano Izquierda",
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.weight(1f),
                    onNegraArriba = {
                        if (pulsoActual < slots.size) {
                            slots = EjercicioEditorLogic.aplicarFigura(slots, pulsoActual, "izquierda", FiguraRitmica.NEGRA, "arriba")
                            notaSeleccionada = null
                            verificarYAgregarCompas()
                        }
                    },
                    onNegraAbajo = {
                        if (pulsoActual < slots.size) {
                            slots = EjercicioEditorLogic.aplicarFigura(slots, pulsoActual, "izquierda", FiguraRitmica.NEGRA, "abajo")
                            notaSeleccionada = null
                            verificarYAgregarCompas()
                        }
                    },
                    onSilencio = {
                        if (pulsoActual < slots.size) {
                            slots = EjercicioEditorLogic.aplicarFigura(slots, pulsoActual, "izquierda", FiguraRitmica.SILENCIO, "abajo")
                            notaSeleccionada = null
                            verificarYAgregarCompas()
                        }
                    }
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Controles y Tempo",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    var showCompasMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BotonDoradoEstilo(
                            label = "Compás $compas",
                            onClick = { showCompasMenu = true }
                        )
                        DropdownMenu(
                            expanded = showCompasMenu,
                            onDismissRequest = { showCompasMenu = false },
                            modifier = Modifier.background(Color(0xFF1A1A1A))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Compás 4/4", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    compas = "4/4"
                                    showCompasMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Compás 3/4", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    compas = "3/4"
                                    showCompasMenu = false
                                }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { bpm = (it / 5).toInt() * 5 },
                        valueRange = 40f..200f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF1E88E5),
                            activeTrackColor = Color(0xFF1E88E5),
                            inactiveTrackColor = Color(0xFF333333)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        "$bpm BPM",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Button(
                        onClick = { previewActivo = !previewActivo },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (previewActivo) Color(0xFFFF5252) else Color(0xFFD4AF37)
                        )
                    ) {
                        if (previewActivo) {
                            Text("DETENER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        } else {
                            Text("▶ INICIAR CONTEO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    BotonDoradoEstilo(
                        label = "SIGUIENTE PULSO",
                        onClick = {
                            pulsoActual = EjercicioEditorLogic.indiceSiguientePulso(slots, pulsoActual)
                            notaSeleccionada = null
                            if (pulsoActual >= slots.lastIndex && numCompases < 4) {
                                numCompases++
                                slots = slots + List(beatsPerMeasure) { EditorSlot() }
                            }
                        }
                    )
                    
                    if (notaSeleccionada != null) {
                        Spacer(Modifier.height(8.dp))
                        BotonDoradoEstilo(
                            label = "BORRAR NOTA",
                            onClick = {
                                val notaSel = notaSeleccionada
                                if (notaSel != null) {
                                    val index = (notaSel.ms / msPorPulso - 1).toInt().coerceIn(slots.indices)
                                    slots = EjercicioEditorLogic.borrarNota(slots, index, notaSel.carril)
                                    notaSeleccionada = null
                                }
                            },
                            textColor = Color(0xFFFF5252),
                            borderColor = Color(0xFFFF5252)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    BotonDoradoEstilo(
                        label = "BORRAR TODO",
                        onClick = {
                            slots = EjercicioEditorLogic.slotsVacios(beatsPerMeasure, numCompases)
                            pulsoActual = 0
                            notaSeleccionada = null
                            previewActivo = false
                        },
                        textColor = Color(0xFFFF4444),
                        borderColor = Color(0xFFFF4444)
                    )
                }
            }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { mostrarMetadata = !mostrarMetadata }) {
                    Text(if (mostrarMetadata) "Ocultar info" else "Título, nivel, video...", fontSize = 12.sp)
                }
                OutlinedButton(onClick = {
                    if (numCompases < 4) {
                        numCompases++
                        slots = slots + EjercicioEditorLogic.slotsVacios(beatsPerMeasure, 1)
                    }
                }) {
                    Text("+ Compás", fontSize = 12.sp)
                }
            }

            if (mostrarMetadata) {
                Spacer(Modifier.height(12.dp))
                AdminMetaFields(
                    titulo = titulo, onTitulo = { titulo = it },
                    descripcion = descripcion, onDescripcion = { descripcion = it },
                    videoUrl = videoUrl, onVideoUrl = { 
                        videoUrl = it
                        videoBase64 = null // Limpiar video local si se ingresa URL
                        videoFileName = null
                    },
                    videoFileName = videoFileName,
                    onSeleccionarVideo = { videoPickerLauncher.launch("video/*") },
                    onLimpiarVideo = {
                        videoUrl = ""
                        videoBase64 = null
                        videoExtension = null
                        videoFileName = null
                    },
                    pasoAPaso = pasoAPaso, onPasoAPaso = { pasoAPaso = it },
                    nivel = nivel, onNivel = { nivel = it },
                    nivelExpanded = nivelExpanded, onNivelExpanded = { nivelExpanded = it },
                    compas = compas, onCompas = { compas = it },
                    compasExpanded = compasExpanded, onCompasExpanded = { compasExpanded = it }
                )
            }

            Spacer(Modifier.height(16.dp))
            mensajeExito?.let { Text(it, color = Color(0xFF81C784), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            mensajeError?.let { Text(it, color = Color(0xFFEF9A9A), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    mensajeExito = null
                    mensajeError = null
                    val notasDto = EjercicioEditorLogic.aNotasDto(slots, bpm, beatsPerMeasure)
                    if (titulo.isBlank()) {
                        mensajeError = "Indica un título en 'Título, nivel, video...'"
                        mostrarMetadata = true
                        return@Button
                    }
                    if (notasDto.isEmpty()) {
                        mensajeError = "Agrega al menos una nota o silencio en la partitura."
                        return@Button
                    }
                    enviando = true
                    scope.launch {
                        try {
                            val dto = CrearEjercicioDto(
                                titulo = titulo,
                                descripcion = descripcion.ifBlank { "Ejercicio de ritmo en compás $compas." },
                                nivel = nivel,
                                tempoBpm = bpm,
                                secuenciaNotas = notasDto,
                                videoUrl = videoUrl.ifBlank { null },
                                videoBase64 = videoBase64,
                                videoExtension = videoExtension,
                                pasoAPaso = pasoAPaso.ifBlank { null }
                            )
                            val response = RetrofitClient.apiService.crearEjercicio(dto)
                            if (response.isSuccessful && response.body()?.success == true) {
                                mensajeExito = "✅ Ejercicio '$titulo' publicado."
                                titulo = ""; descripcion = ""; videoUrl = ""; videoBase64 = null; videoExtension = null; videoFileName = null; pasoAPaso = ""
                                slots = EjercicioEditorLogic.slotsVacios(beatsPerMeasure, 1)
                                numCompases = 1; pulsoActual = 0; notaSeleccionada = null
                            } else {
                                mensajeError = "Error al publicar (${response.code()})"
                            }
                        } catch (e: Exception) {
                            mensajeError = "Error: ${e.message}"
                        } finally {
                            enviando = false
                        }
                    }
                },
                enabled = !enviando,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                if (enviando) CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                else Text("PUBLICAR EJERCICIO", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onVolverClick, modifier = Modifier.fillMaxWidth()) {
                Text("← Volver")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BotonDoradoEstilo(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFFD4AF37),
    borderColor: Color = Color(0xFFD4AF37)
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF252525),
            contentColor = textColor
        ),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun ManoEditorPanel(
    titulo: String,
    color: Color,
    modifier: Modifier = Modifier,
    onNegraArriba: () -> Unit,
    onNegraAbajo: () -> Unit,
    onSilencio: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            titulo,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        BotonDoradoEstilo("Negra ↑", onNegraArriba)
        Spacer(Modifier.height(8.dp))
        BotonDoradoEstilo("Negra ↓", onNegraAbajo)
        Spacer(Modifier.height(8.dp))
        BotonDoradoEstilo("Silencio", onSilencio)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminMetaFields(
    titulo: String, onTitulo: (String) -> Unit,
    descripcion: String, onDescripcion: (String) -> Unit,
    videoUrl: String, onVideoUrl: (String) -> Unit,
    videoFileName: String?,
    onSeleccionarVideo: () -> Unit,
    onLimpiarVideo: () -> Unit,
    pasoAPaso: String, onPasoAPaso: (String) -> Unit,
    nivel: String, onNivel: (String) -> Unit,
    nivelExpanded: Boolean, onNivelExpanded: (Boolean) -> Unit,
    compas: String, onCompas: (String) -> Unit,
    compasExpanded: Boolean, onCompasExpanded: (Boolean) -> Unit
) {
    val niveles = listOf("basico", "intermedio", "avanzado")
    val compases = listOf("4/4", "3/4")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF12122A))
            .padding(14.dp)
    ) {
        OutlinedTextField(titulo, onTitulo, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(descripcion, onDescripcion, label = { Text("Desarrollo del nivel") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = nivelExpanded, onExpandedChange = onNivelExpanded, modifier = Modifier.weight(1f)) {
                OutlinedTextField(nivel, {}, readOnly = true, label = { Text("Nivel") }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = nivelExpanded, onDismissRequest = { onNivelExpanded(false) }) {
                    niveles.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.replaceFirstChar { it.uppercase() }) },
                            onClick = { onNivel(item); onNivelExpanded(false) }
                        )
                    }
                }
            }
            ExposedDropdownMenuBox(expanded = compasExpanded, onExpandedChange = onCompasExpanded, modifier = Modifier.weight(1f)) {
                OutlinedTextField(compas, {}, readOnly = true, label = { Text("Compás") }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = compasExpanded, onDismissRequest = { onCompasExpanded(false) }) {
                    compases.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = { onCompas(item); onCompasExpanded(false) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        
        // Campo de video con opción de URL o archivo local
        Text("Video (URL o archivo local)", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        
        if (videoFileName != null) {
            // Mostrar archivo local seleccionado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("📁 $videoFileName", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Archivo local seleccionado", color = Color.Gray, fontSize = 11.sp)
                }
                Button(
                    onClick = onLimpiarVideo,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("✕", color = Color.White, fontSize = 12.sp)
                }
            }
        } else {
            // Mostrar opción de URL o seleccionar archivo
            Column {
                OutlinedTextField(
                    videoUrl,
                    onVideoUrl,
                    label = { Text("URL de video (YouTube, Vimeo, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onSeleccionarVideo,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar video del celular", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(pasoAPaso, onPasoAPaso, label = { Text("Pasos (uno por línea)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
    }
}
