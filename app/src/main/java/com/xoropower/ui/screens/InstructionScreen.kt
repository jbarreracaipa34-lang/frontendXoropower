package com.xoropower.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.xoropower.R
import com.xoropower.data.EjerciciosData
import com.xoropower.navigation.Routes
import androidx.compose.runtime.collectAsState
import com.xoropower.ui.viewmodel.ModuleViewModel
import com.xoropower.ui.viewmodel.EjerciciosListViewModel
import com.xoropower.ui.viewmodel.EjerciciosListState
import com.xoropower.ui.theme.ElectricBlue
import com.xoropower.ui.theme.XoroBlack
import com.xoropower.ui.theme.BrightRed
import com.xoropower.ui.theme.XoroWhite

/**
 * Tipos de ejercicio soportados por la pantalla de instrucciones.
 */
object TipoEjercicio {
    const val MANO_DERECHA   = "derecha"
    const val MANO_IZQUIERDA  = "izquierda"
    const val MANOS_JUNTAS    = "juntas"
    
    // Nuevos niveles
    const val INTERMEDIO_1    = "intermedio_sincopa"
    const val INTERMEDIO_2    = "intermedio_semicorcheas"
    const val INTERMEDIO_3    = "intermedio_polirritmo_4vs3"
    const val AVANZADO_1      = "avanzado_joropo"
    const val AVANZADO_2      = "avanzado_irregular"
    const val AVANZADO_3      = "avanzado_polirritmo_ostinato"
}

data class InstructionPage(
    val image: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionScreen(
    navController: NavController,
    moduleViewModel: ModuleViewModel = viewModel(),
    ejViewModel: EjerciciosListViewModel = viewModel(),
    tipoEjercicio: String = TipoEjercicio.MANOS_JUNTAS
) {
    val hasSeenInstructions by moduleViewModel.hasSeenInstructions.collectAsState()
    var showTheoryDialog by remember { mutableStateOf(false) }

    val ejEstado by ejViewModel.estado.collectAsState()

    // Helper: para tipos intermedio/avanzado/basico, obtener el ejercicio desde la BD por índice dentro del nivel
    val (nivelBD, indiceBD) = remember(tipoEjercicio) {
        when (tipoEjercicio) {
            TipoEjercicio.MANO_DERECHA   -> "basico" to 0
            TipoEjercicio.MANO_IZQUIERDA -> "basico" to 1
            TipoEjercicio.MANOS_JUNTAS   -> "basico" to 2
            TipoEjercicio.INTERMEDIO_1 -> "intermedio" to 0
            TipoEjercicio.INTERMEDIO_2 -> "intermedio" to 1
            TipoEjercicio.INTERMEDIO_3 -> "intermedio" to 2
            TipoEjercicio.AVANZADO_1   -> "avanzado" to 0
            TipoEjercicio.AVANZADO_2   -> "avanzado" to 1
            TipoEjercicio.AVANZADO_3   -> "avanzado" to 2
            else -> null to -1
        }
    }
    val ejercicioBD = remember(ejEstado, nivelBD, indiceBD) {
        if (nivelBD != null) ejViewModel.ejercicioPorNivelEIndice(nivelBD, indiceBD) else null
    }

    // ── PÁGINAS DE INSTRUCCIONES SEGÚN EL TIPO DE EJERCICIO ──
    val pages = remember(tipoEjercicio) {
        when (tipoEjercicio) {
            TipoEjercicio.MANO_DERECHA -> listOf(
                InstructionPage(
                    image = R.drawable.sujecion_derecha_vista_frontal,
                    title = "Sujeción Mano Derecha",
                    description = "La mano derecha sujeta la maraca de forma natural. Mantén la muñeca suelta para lograr un movimiento fluido al agitar."
                ),
                InstructionPage(
                    image = R.drawable.movimiento_derecha_descendente,
                    title = "Lectura del Pentagrama",
                    description = "En este ejercicio verás notas rojas en el pentagrama superior marcado con la letra \"D\". Cada nota roja representa un golpe con tu mano derecha. El palito de la nota (plica) indica hacia dónde va el movimiento de tu mano por cada pulso."
                )
            )

            TipoEjercicio.MANO_IZQUIERDA -> listOf(
                InstructionPage(
                    image = R.drawable.sujecion_izquierda_vista_frontal,
                    title = "Sujeción Mano Izquierda",
                    description = "La mano izquierda sujeta la maraca de forma natural. Mantén la muñeca suelta para lograr un movimiento fluido al agitar."
                ),
                InstructionPage(
                    image = R.drawable.movimiento_izquierda_ascendente,
                    title = "Lectura del Pentagrama",
                    description = "En este ejercicio verás notas azules en el pentagrama inferior marcado con la letra \"I\". Cada nota azul representa un golpe con tu mano izquierda. El palito de la nota (plica) indica hacia dónde va el movimiento de tu mano por cada pulso."
                )
            )

            TipoEjercicio.INTERMEDIO_1 -> listOf(
                InstructionPage(
                    image = R.drawable.intermedio,
                    title = "Nivel Intermedio",
                    description = "Aumentamos el tempo a 120 BPM. Aprenderás síncopas (golpes a contratiempo) y semicorcheas (patrones rápidos y alternados)."
                ),
                InstructionPage(
                    image = R.drawable.interm2,
                    title = "Síncopas y Contratiempos",
                    description = "Deberás golpear entre los pulsos principales. Mantén la precisión y escucha el metrónomo para no perder el compás."
                )
            )

            TipoEjercicio.INTERMEDIO_2 -> listOf(
                InstructionPage(
                    image = R.drawable.intermedio,
                    title = "Nivel 2 Intermedio",
                    description = "Aumentamos el tempo y la dificultad. Aprenderás a ejecutar semicorcheas (patrones rápidos y alternados) manteniendo la precisión."
                ),
                InstructionPage(
                    image = R.drawable.veloypre,
                    title = "Velocidad y Precisión",
                    description = "Las semicorcheas requieren movimientos cortos y ágiles. Mantén las muñecas relajadas para no cansarte y seguir el pulso a 120 BPM."
                )
            )

            TipoEjercicio.INTERMEDIO_3 -> listOf(
                InstructionPage(
                    image = R.drawable.intermedio,
                    title = "Nivel 3 Intermedio",
                    description = "Llegaste al reto de polirritmia 4 contra 3. Aquí pondrás a prueba la independencia total de tus manos."
                ),
                InstructionPage(
                    image = R.drawable.poliritmo,
                    title = "Desafío Polirrítmico",
                    description = "Coordina 4 golpes con una mano mientras la otra hace 3. Siente el cruce de los pulsos en lugar de tratar de contarlos mentalmente."
                )
            )

            TipoEjercicio.AVANZADO_1 -> listOf(
                InstructionPage(
                    image = R.drawable.avanzado,
                    title = "Ejercicio Avanzado",
                    description = "Subimos el tempo a 140 BPM. Practicaremos patrones tradicionales de Joropo Llanero."
                ),
                InstructionPage(
                    image = R.drawable.tres_tres_dos,
                    title = "Ritmo Joropo 3+3+2",
                    description = "El Joropo se caracteriza por agrupaciones irregulares de golpes. Coordinarás ambas manos en patrones rápidos."
                )
            )

            TipoEjercicio.AVANZADO_2 -> listOf(
                InstructionPage(
                    image = R.drawable.avanzado,
                    title = "Ejercicio 2 Avanzado",
                    description = "Continuamos con ritmos complejos a 140 BPM. Agregaremos silencios y variaciones rítmicas."
                ),
                InstructionPage(
                    image = R.drawable.agilidadysile,
                    title = "Agilidad y Silencios",
                    description = "La clave del nivel avanzado es saber cuándo no tocar. Mantén la fluidez de las manos en los descansos."
                )
            )

            TipoEjercicio.AVANZADO_3 -> listOf(
                InstructionPage(
                    image = R.drawable.avanzado,
                    title = "Ejercicio 3 Avanzado",
                    description = "Practicaremos independencia de manos a nivel experto. Ostinatos y polirritmias avanzadas."
                ),
                InstructionPage(
                    image = R.drawable.ritmoindependiente,
                    title = "Independencia Rítmica",
                    description = "No intentes sincronizar todo mentalmente. Deja que cada mano mantenga su propio pulso y siente el patrón resultante."
                )
            )

            else -> listOf( // MANOS_JUNTAS
                InstructionPage(
                    image = R.drawable.sujecion_escobillao,
                    title = "Las Dos Manos",
                    description = "El joropo se toca coordinando ambas manos. En la tablatura, las notas de color Rojo (arriba, marcadas \"D\") representan tu Mano Derecha, y las notas Azules (abajo, marcadas \"I\") representan tu Mano Izquierda."
                ),
                InstructionPage(
                    image = R.drawable.lectura_de_notas,
                    title = "Lectura del Pentagrama",
                    description = "Verás dos pentagramas: el superior con notas rojas (Derecha) y el inferior con notas azules (Izquierda). El palito de la nota (plica) indica hacia dónde va el movimiento de cada mano por cada pulso. Ambas manos deben coordinarse siguiendo el ritmo."
                ),
                InstructionPage(
                    image = R.drawable.maracas_instruccion,
                    title = "Clave de Percusión",
                    description = "La clave de percusión al inicio del pentagrama indica qué instrumento se toca. Los puntos después de la clave indican que el ejercicio se repite (puntos de repetición)."
                )
            )
        }
    }

    // Título y descripción: se usan directamente de la BD cuando están disponibles.
    // De lo contrario, se muestran las locales/fallback de inmediato para evitar estados de carga visuales.
    val (tituloEjercicio, descripcionEjercicio) = remember(tipoEjercicio, ejercicioBD) {
        if (ejercicioBD != null) {
            ejercicioBD.titulo to ejercicioBD.descripcion
        } else {
            when (tipoEjercicio) {
                TipoEjercicio.MANO_DERECHA    -> "Ejercicio: Mano Derecha" to
                    "Practica el ritmo solo con la mano derecha. Sigue las notas rojas del pentagrama superior."
                TipoEjercicio.MANO_IZQUIERDA  -> "Ejercicio: Mano Izquierda" to
                    "Practica el ritmo solo con la mano izquierda. Sigue las notas azules del pentagrama inferior."
                TipoEjercicio.INTERMEDIO_1    -> "Ejercicio: Síncopa Intermedia" to
                    "Aprende a golpear a contratiempo alternando derecha e izquierda a 120 BPM."
                TipoEjercicio.INTERMEDIO_2    -> "Ejercicio: Semicorcheas Rápidas" to
                    "Ejercita la velocidad alternando golpes rápidos en semicorcheas a 120 BPM."
                TipoEjercicio.INTERMEDIO_3    -> "Polirritmo: 4 vs 3" to
                    "Toca 4 negras con la mano derecha y 3 negras con la izquierda al mismo tiempo."
                TipoEjercicio.AVANZADO_1      -> "Ejercicio: Joropo Llanero (3+3+2)" to
                    "Domina el patrón base del joropo con la acentuación clásica llanera a 140 BPM."
                TipoEjercicio.AVANZADO_2      -> "Ejercicio: Ritmo Irregular" to
                    "Pon a prueba tu agilidad con contratiempos, dobles golpes y silencios a 140 BPM."
                TipoEjercicio.AVANZADO_3      -> "Polirritmo: Ostinato y Variación" to
                    "Mantén un pulso constante con la mano derecha mientras la izquierda hace variaciones."
                else -> "Ejercicio: Manos Juntas" to
                    "Pon a prueba tu coordinación. Sigue el pulso visual y presiona los botones en el momento exacto para dominar la base de la maraca."
            }
        }
    }

    // ID del ejercicio a iniciar
    // Para intermedio/avanzado se usa el ID real de la BD cuando está disponible;
    // de lo contrario cae al ID local (UUID de fallback) para que siga funcionando offline.
    val idEjercicio = remember(tipoEjercicio, ejercicioBD) {
        ejercicioBD?.id ?: when (tipoEjercicio) {
            TipoEjercicio.MANO_DERECHA   -> EjerciciosData.ID_EJERCICIO_DERECHA
            TipoEjercicio.MANO_IZQUIERDA -> EjerciciosData.ID_EJERCICIO_IZQUIERDA
            TipoEjercicio.INTERMEDIO_1   -> EjerciciosData.ID_INTERMEDIO_1
            TipoEjercicio.INTERMEDIO_2   -> EjerciciosData.ID_INTERMEDIO_2
            TipoEjercicio.INTERMEDIO_3   -> EjerciciosData.ID_INTERMEDIO_3
            TipoEjercicio.AVANZADO_1     -> EjerciciosData.ID_AVANZADO_1
            TipoEjercicio.AVANZADO_2     -> EjerciciosData.ID_AVANZADO_2
            TipoEjercicio.AVANZADO_3     -> EjerciciosData.ID_AVANZADO_3
            else -> EjerciciosData.ID_EJERCICIO_JUNTAS
        }
    }

    // Si ya vio instrucciones de este tipo, ir directo al resumen
    var currentPage by remember { mutableStateOf(if (hasSeenInstructions) pages.size else 0) }
    
    LaunchedEffect(hasSeenInstructions) {
        if (hasSeenInstructions && currentPage < pages.size) {
            currentPage = pages.size
        }
    }

    LaunchedEffect(currentPage) {
        if (currentPage >= pages.size) {
            moduleViewModel.setInstructionsSeen(true)
        }
    }

    val showSummary = currentPage >= pages.size
    val progress = if (showSummary) 1f else (currentPage + 1).toFloat() / (pages.size + 1)

    // Color temático según tipo de ejercicio
    val accentColor = when (tipoEjercicio) {
        TipoEjercicio.MANO_DERECHA -> Color(0xFFCF1020)   // Rojo
        TipoEjercicio.MANO_IZQUIERDA -> ElectricBlue       // Azul
        TipoEjercicio.INTERMEDIO_1, TipoEjercicio.INTERMEDIO_2, TipoEjercicio.INTERMEDIO_3 -> Color(0xFFFFD700) // Dorado
        TipoEjercicio.AVANZADO_1, TipoEjercicio.AVANZADO_2, TipoEjercicio.AVANZADO_3 -> BrightRed // Rojo brillante
        else -> ElectricBlue
    }

    Scaffold(
        containerColor = XoroBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    androidx.compose.material3.Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append(androidx.compose.ui.text.AnnotatedString("XORO", spanStyle = androidx.compose.ui.text.SpanStyle(color = ElectricBlue)))
                            append(androidx.compose.ui.text.AnnotatedString("PO", spanStyle = androidx.compose.ui.text.SpanStyle(color = Color.White)))
                            append(androidx.compose.ui.text.AnnotatedString("WER", spanStyle = androidx.compose.ui.text.SpanStyle(color = BrightRed)))
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(3f, 3f),
                                blurRadius = 8f
                            )
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = XoroWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (!showSummary) {
            // ── MODO CARRUSEL EXPLICATIVO ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Box(modifier = Modifier.weight(1.0f)) {
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "carousel"
                    ) { index ->
                        val page = pages[index]
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = page.image),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, XoroBlack),
                                            startY = 400f
                                        )
                                    )
                             )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.6f)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val page = pages[currentPage]
                    Text("PASO #${currentPage + 1}", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(page.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(page.description, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { currentPage++ },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("SIGUIENTE", fontWeight = FontWeight.Black, fontSize = 15.sp, color = XoroBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = XoroBlack, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        } else {
            // ── MODO RESUMEN FINAL (PROCESO DE NIVEL) ──
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Text("PROCESO DE NIVEL", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(tituloEjercicio, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val videoRes = when (tipoEjercicio) {
                        TipoEjercicio.MANO_DERECHA   -> R.raw.derechaa
                        TipoEjercicio.MANO_IZQUIERDA -> R.raw.izquierdaa
                        TipoEjercicio.INTERMEDIO_1   -> R.raw.ejerciciointermedio1
                        TipoEjercicio.INTERMEDIO_2   -> R.raw.ejercicio2int
                        TipoEjercicio.INTERMEDIO_3   -> R.raw.ejercicio3int
                        TipoEjercicio.AVANZADO_1     -> R.raw.ejercicio1avan
                        TipoEjercicio.AVANZADO_2     -> R.raw.ejercicio2avan
                        TipoEjercicio.AVANZADO_3     -> R.raw.ejercicio3avan
                        else                          -> R.raw.joropo
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    val uri = android.net.Uri.parse("android.resource://${ctx.packageName}/$videoRes")
                                    setVideoURI(uri)
                                    val mediaController = android.widget.MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(mediaController)
                                    // Mostrar el primer frame
                                    seekTo(1)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón de Información de Notas
                    OutlinedButton(
                        onClick = { showTheoryDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = accentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TEORÍA: INFORMACIÓN DE NOTAS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("PRÁCTICA DE RITMO", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column {
                            Text(tituloEjercicio, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                descripcionEjercicio,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    Text("PASOS A SEGUIR", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                val steps = run {
                    // Para intermedio/avanzado: usar pasoAPaso de la BD (formato "1. paso\n2. paso\n3. paso")
                    val pasoBD = ejercicioBD?.pasoAPaso
                    if (pasoBD != null && nivelBD != null) {
                        pasoBD.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            // Quitar el prefijo "N. " si existe (ej. "1. " → "")
                            .map { linea -> linea.replaceFirst(Regex("""^\d+\.\s*"""), "") }
                    } else {
                        // Fallback hardcodeado para básicos o si la BD no cargó aún
                        when (tipoEjercicio) {
                            TipoEjercicio.MANO_DERECHA -> listOf(
                                "Sujeta la maraca con la mano derecha de forma firme y relajada.",
                                "Observa las notas rojas en el pentagrama superior (D).",
                                "Presiona el botón DERECHO cuando la nota cruce la línea de tiempo."
                            )
                            TipoEjercicio.MANO_IZQUIERDA -> listOf(
                                "Sujeta la maraca con la mano izquierda de forma natural.",
                                "Observa las notas azules en el pentagrama inferior (I).",
                                "Presiona el botón IZQUIERDO cuando la nota cruce la línea de tiempo."
                            )
                            TipoEjercicio.INTERMEDIO_1, TipoEjercicio.INTERMEDIO_2, TipoEjercicio.INTERMEDIO_3 -> listOf(
                                "Asegúrate de tocar a contratiempo cuando veas notas desplazadas.",
                                "Alterna golpes rápidos según aparezcan las semicorcheas.",
                                "Usa los botones IZQ y DER coordinando ambas manos a 120 BPM."
                            )
                            TipoEjercicio.AVANZADO_1, TipoEjercicio.AVANZADO_2 -> listOf(
                                "Sigue el patrón de acentuación clásico del Joropo Llanero (3+3+2).",
                                "Mantén la concentración en los silencios e irregularidades.",
                                "Ejecuta con precisión y velocidad a 140 BPM."
                            )
                            TipoEjercicio.AVANZADO_3 -> listOf(
                                "Mantén un pulso constante con la mano derecha.",
                                "Deja que la mano izquierda fluya en su propia métrica o variación.",
                                "Escucha la superposición de ambos ritmos."
                            )
                            else -> listOf(
                                "Mantén la sujeción correcta en ambas manos.",
                                "Observa la línea de tiempo (el pulso musical).",
                                "Sincroniza tu movimiento con la llegada de las notas."
                            )
                        }
                    }
                }

                itemsIndexed(steps) { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.1f))
                                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((index + 1).toString(), color = accentColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(step, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            navController.navigate(Routes.RITMO + "/$idEjercicio") 
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("INICIAR EJERCICIO", color = XoroBlack, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    // ── DIÁLOGO DE INFORMACIÓN DE NOTAS (TEORÍA MUSICAL) ──
    if (showTheoryDialog) {
        Dialog(onDismissRequest = { showTheoryDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1220))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Cabecera del diálogo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEORÍA RÍTMICA 🎼",
                            color = ElectricBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { showTheoryDialog = false },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Text("✕", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Aprende los elementos básicos del compás y las figuras rítmicas para dominar las maracas.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Contenido scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. El Compás de 4/4
                        TheoryItem(
                            emoji = "📊",
                            title = "Métrica y Compás 4/4",
                            description = "Es la estructura de tiempo. Nos indica que hay exactamente cuatro tiempos o latidos por compás. Se cuenta constantemente: 1, 2, 3, 4."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 2. La Negra
                        TheoryItem(
                            emoji = "𝅘𝅥",
                            title = "La Negra (1 Tiempo)",
                            description = "Es la unidad de ritmo básica. Cada nota negra dura un pulso completo. En un compás de 4/4, caben exactamente 4 negras (un golpe constante en cada número del conteo: 1 - 2 - 3 - 4)."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 3. La Blanca
                        TheoryItem(
                            emoji = "𝅗𝅥",
                            title = "La Blanca (2 Tiempos)",
                            description = "Una figura rítmica larga. Dura exactamente el doble que una negra (dos pulsos completos). Das un golpe en el primer tiempo y esperas a que termine el segundo tiempo antes de volver a golpear."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 4. El Compás
                        TheoryItem(
                            emoji = "🎼",
                            title = "El Compás Rítmico",
                            description = "Es como una caja organizada que separa la música. En la tablatura verás líneas verticales llamadas barras divisoras. Su fin es agrupar el tiempo y ayudarte a no perder la cuenta."
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón de Entendido
                    Button(
                        onClick = { showTheoryDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "ENTENDIDO",
                            color = XoroBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TheoryItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}