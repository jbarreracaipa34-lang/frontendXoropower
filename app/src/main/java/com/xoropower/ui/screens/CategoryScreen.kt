package com.xoropower.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.xoropower.navigation.Routes
import com.xoropower.data.LevelUnlockHelper
import com.xoropower.data.SessionManager
import com.xoropower.ui.theme.*
import com.xoropower.ui.theme.BrightRed
import com.xoropower.ui.theme.XoroWhite
import com.xoropower.ui.viewmodel.AuthViewModel
import com.xoropower.ui.viewmodel.ModuleViewModel
import com.xoropower.ui.viewmodel.ModuleState
import com.xoropower.ui.viewmodel.SectionState
import com.xoropower.ui.viewmodel.EjerciciosListViewModel
import com.xoropower.ui.viewmodel.EjerciciosListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    moduleViewModel: ModuleViewModel = viewModel(),
    ejViewModel: EjerciciosListViewModel = viewModel(),
    reset: Boolean = false
) {
    val backStackEntry = navController.currentBackStackEntry
    val currentRoute = navController.currentDestination?.route
    var selectedLevel by remember { mutableStateOf(moduleViewModel.selectedLevel.value) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val esAdmin = sessionManager.isAdmin()
    var progressRefreshKey by remember { mutableIntStateOf(0) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        progressRefreshKey++
    }

    val moduleState by moduleViewModel.moduleState.collectAsState()
    val sectionState by moduleViewModel.sectionState.collectAsState()

    // ── CAMBIO: ahora viene del ViewModel para persistir al volver ──
    val currentModuloId by moduleViewModel.currentModuloId.collectAsState()

    // Forzar recarga cuando currentModuloId cambia a null
    LaunchedEffect(currentModuloId) {
        if (currentModuloId == null) {
            moduleViewModel.fetchModulos()
        }
    }

    // Limpiar estado cuando se navega desde el drawer
    LaunchedEffect(reset) {
        if (reset) {
            moduleViewModel.setCurrentModuloId(null)
        }
    }

    // Cargamos módulos y ejercicios al inicio
    LaunchedEffect(Unit) {
        moduleViewModel.fetchModulos()
        ejViewModel.cargarEjercicios()
    }

    // Cuando cambia el nivel seleccionado, cargamos las actividades de ese nivel
    LaunchedEffect(selectedLevel) {
        currentModuloId?.let { id ->
            moduleViewModel.fetchSeccion(id, selectedLevel)
        }
    }

    // Estado de ejercicios observado a nivel @Composable (no puede estar dentro de LazyListScope)
    val ejEstado by ejViewModel.estado.collectAsState()
    val ejerciciosList = (ejEstado as? EjerciciosListState.Success)?.ejercicios ?: emptyList()

    // Animación de pulso para el brillo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

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
        Scaffold(
            containerColor = XoroBlack,
            topBar = {
                xoropowerTopBar(
                    onMenuClick = if (currentModuloId != null) {
                        { moduleViewModel.setCurrentModuloId(null) }
                    } else {
                        { scope.launch { drawerState.open() } }
                    },
                    navController = navController,
                    authViewModel = authViewModel,
                    showBackIcon = currentModuloId != null
                )
            },
            bottomBar = { BottomNavBar(navController, Routes.CATEGORIES) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val glowColor = when (selectedLevel) {
                    "basico" -> ElectricBlue
                    "intermedio" -> GoldenYellow
                    "avanzado" -> BrightRed
                    else -> Color(0xFF9C27B0)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(glowColor.copy(alpha = 0.12f), Color.Transparent),
                                    center = Offset(size.width / 2f, size.height * 0.15f),
                                    radius = size.width * 1.1f * pulseScale
                                )
                            )
                        }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // ── CABECERA ──────────────────────────────────
                    item {
                        Column(
                            modifier = Modifier
                                .padding(top = 48.dp, bottom = 24.dp)
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                "ENTRENAMIENTO",
                                color = XoroMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                if (currentModuloId == null) {
                                    "CATEGORÍAS"
                                } else {
                                    // Obtener el nombre del módulo actual
                                    when (val state = moduleState) {
                                        is ModuleState.Success -> {
                                            state.modules.find { it.id == currentModuloId }?.titulo?.uppercase() ?: "MÓDULO"
                                        }
                                        else -> "MÓDULO"
                                    }
                                },
                                color = XoroWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (currentModuloId == null) {
                        // ── LISTA DE CATEGORÍAS (MÓDULOS) ─────────────
                        when (val state = moduleState) {
                            is ModuleState.Loading -> {
                                item {
                                    Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                                        CircularProgressIndicator(color = ElectricBlue)
                                    }
                                }
                            }
                            is ModuleState.Success -> {
                                itemsIndexed(state.modules) { index, modulo ->
                                    CategoryModuleCard(
                                        title = modulo.titulo,
                                        desc = modulo.descripcion ?: "",
                                        index = index + 1
                                    ) {
                                        // ── CAMBIO: guardar en ViewModel en lugar de remember ──
                                        moduleViewModel.setCurrentModuloId(modulo.id)
                                        moduleViewModel.fetchSeccion(modulo.id, selectedLevel)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                            is ModuleState.Error -> {
                                item {
                                    Text(
                                        state.message,
                                        color = BrightRed,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            if (esAdmin) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFD700).copy(alpha = 0.1f))
                                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "Modo administrador: todos los niveles y ejercicios están desbloqueados.",
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            val nivelBasicoUnlocked = remember(progressRefreshKey, ejerciciosList) { LevelUnlockHelper.isLevelUnlocked("basico", sessionManager, ejerciciosList) }
                            val nivelIntermedioUnlocked = remember(progressRefreshKey, ejerciciosList) { LevelUnlockHelper.isLevelUnlocked("intermedio", sessionManager, ejerciciosList) }
                            val nivelAvanzadoUnlocked = remember(progressRefreshKey, ejerciciosList) { LevelUnlockHelper.isLevelUnlocked("avanzado", sessionManager, ejerciciosList) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CategoryLevelButton(
                                    label = "Básico",
                                    isSelected = selectedLevel == "basico",
                                    isLocked = !nivelBasicoUnlocked,
                                    color = ElectricBlue,
                                    modifier = Modifier.width(110.dp)
                                ) { 
                                    selectedLevel = "basico"
                                    moduleViewModel.setSelectedLevel("basico")
                                }

                                CategoryLevelButton(
                                    label = "Intermedio",
                                    isSelected = selectedLevel == "intermedio",
                                    isLocked = !nivelIntermedioUnlocked,
                                    color = GoldenYellow,
                                    modifier = Modifier.width(110.dp)
                                ) { 
                                    selectedLevel = "intermedio"
                                    moduleViewModel.setSelectedLevel("intermedio")
                                }

                                CategoryLevelButton(
                                    label = "Avanzado",
                                    isSelected = selectedLevel == "avanzado",
                                    isLocked = !nivelAvanzadoUnlocked,
                                    color = BrightRed,
                                    modifier = Modifier.width(110.dp)
                                ) { 
                                    selectedLevel = "avanzado"
                                    moduleViewModel.setSelectedLevel("avanzado")
                                }
                            }

                            if (!esAdmin) {
                                LevelUnlockHelper.unlockHintForLevel(selectedLevel)?.let { hint ->
                                    if (!LevelUnlockHelper.isLevelUnlocked(selectedLevel, sessionManager, ejerciciosList)) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            hint,
                                            color = XoroMuted,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                        }

                        val ejerciciosBase = when (selectedLevel) {
                            "basico", "intermedio", "avanzado" -> {
                                // Intentar obtener desde la API
                                val desdeBD = (ejEstado as? EjerciciosListState.Success)
                                    ?.ejercicios
                                    ?.filter { it.nivel == selectedLevel }
                                    ?: emptyList()

                                if (desdeBD.isNotEmpty()) {
                                    desdeBD.mapIndexed { i, ej ->
                                        Triple((i + 1).toString(), ej.titulo, ej.descripcion)
                                    }
                                } else {
                                    // Fallback hardcodeado mientras carga o si hay error
                                    if (selectedLevel == "intermedio") listOf(
                                        Triple("1", "Síncopa Intermedia", "Aprende a golpear a contratiempo alternando derecha e izquierda a 120 BPM."),
                                        Triple("2", "Semicorcheas Rápidas", "Ejercita la velocidad alternando golpes rápidos en semicorcheas a 120 BPM."),
                                        Triple("3", "Polirritmo 4 vs 3", "Toca 4 negras con la mano derecha y 3 con la izquierda al mismo tiempo.")
                                    ) else if (selectedLevel == "avanzado") listOf(
                                        Triple("1", "Joropo Llanero (3+3+2)", "Domina el patrón base del joropo con la acentuación clásica llanera a 140 BPM."),
                                        Triple("2", "Ritmo Irregular", "Pon a prueba tu agilidad con contratiempos, dobles golpes y silencios a 140 BPM."),
                                        Triple("3", "Ostinato y Variación", "Mantén un pulso constante con la mano derecha mientras la izquierda hace variaciones.")
                                    ) else listOf(
                                        Triple("1", "Ejercicio: Mano Derecha", "Practica el ritmo solo con la mano derecha. Notas rojas (D) a 80 BPM."),
                                        Triple("2", "Ejercicio: Mano Izquierda", "Practica el ritmo solo con la mano izquierda. Notas azules (I) a 80 BPM."),
                                        Triple("3", "Ejercicio: Manos Juntas", "Coordina ambas manos siguiendo el ritmo completo a 80 BPM.")
                                    )
                                }
                            }
                            else -> emptyList()
                        }

                        val ejercicios = ejerciciosBase.mapIndexed { index, (id, title, desc) ->
                            CategoryActivityItem(
                                id = id,
                                title = title,
                                desc = desc,
                                isUnlocked = LevelUnlockHelper.isExerciseUnlocked(selectedLevel, index, sessionManager, ejerciciosList)
                            )
                        }

                        if (!LevelUnlockHelper.isLevelUnlocked(selectedLevel, sessionManager, ejerciciosList)) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = XoroMuted,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "Nivel bloqueado",
                                            color = XoroWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            LevelUnlockHelper.unlockHintForLevel(selectedLevel) ?: "",
                                            color = XoroMuted,
                                            fontSize = 12.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(ejercicios) { index, ejercicio ->
                                CategoryActivityCard(ejercicio) {
                                    val tipo = when (selectedLevel) {
                                        "intermedio" -> when (index) {
                                            0 -> "intermedio_sincopa"
                                            1 -> "intermedio_semicorcheas"
                                            2 -> "intermedio_polirritmo_4vs3"
                                            else -> "intermedio_sincopa"
                                        }
                                        "avanzado" -> when (index) {
                                            0 -> "avanzado_joropo"
                                            1 -> "avanzado_irregular"
                                            2 -> "avanzado_polirritmo_ostinato"
                                            else -> "avanzado_joropo"
                                        }
                                        else -> when (index) {
                                            0 -> "derecha"
                                            1 -> "izquierda"
                                            else -> "juntas"
                                        }
                                    }
                                    navController.navigate(Routes.INSTRUCTION + "/$tipo")
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryLevelButton(
    label: String,
    isSelected: Boolean,
    isLocked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isLocked -> Color.White.copy(alpha = 0.02f)
        isSelected -> color.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.04f)
    }
    val borderColor = when {
        isLocked -> Color.White.copy(alpha = 0.05f)
        isSelected -> color.copy(alpha = 0.7f)
        else -> Color.White.copy(alpha = 0.1f)
    }
    val textColor = when {
        isLocked -> XoroMuted.copy(alpha = 0.5f)
        isSelected -> color
        else -> XoroMuted
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !isLocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLocked) {
                Icon(
                    Icons.Outlined.Lock,
                    null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp).padding(end = 4.dp)
                )
            }
            Text(
                label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
            )
        }
    }
}

data class CategoryActivityItem(val id: String, val title: String, val desc: String, val isUnlocked: Boolean)

@Composable
fun CategoryActivityCard(activity: CategoryActivityItem, onClick: () -> Unit) {
    val alpha = if (activity.isUnlocked) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(enabled = activity.isUnlocked) { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (activity.isUnlocked) ElectricBlue.copy(alpha = 0.1f) else Color.Transparent)
                .border(
                    1.dp,
                    if (activity.isUnlocked) ElectricBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (activity.isUnlocked) {
                Text(activity.id, color = ElectricBlue, fontWeight = FontWeight.Black)
            } else {
                Icon(Icons.Outlined.Lock, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
            }
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(
                activity.title,
                color = if (activity.isUnlocked) XoroWhite else XoroMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                activity.desc,
                color = XoroMuted,
                fontSize = 12.sp,
                maxLines = 2
            )
        }

        if (activity.isUnlocked) {
            Icon(Icons.Outlined.ChevronRight, null, tint = ElectricBlue.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun CategoryModuleCard(title: String, desc: String, index: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.1f))
                .border(1.dp, ElectricBlue.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(index.toString(), color = ElectricBlue, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
            Text(title, color = XoroWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            if (desc.isNotEmpty()) {
                Text(desc, color = XoroMuted, fontSize = 13.sp, maxLines = 1)
            }
        }

        Icon(Icons.Outlined.ChevronRight, null, tint = ElectricBlue.copy(alpha = 0.5f))
    }
}