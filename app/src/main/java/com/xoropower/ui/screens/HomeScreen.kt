package com.xoropower.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.xoropower.R
import com.xoropower.navigation.Routes
import com.xoropower.ui.theme.*
import com.xoropower.ui.viewmodel.AuthViewModel
import com.xoropower.ui.viewmodel.ModuleState
import com.xoropower.ui.viewmodel.ModuleViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Colores locales del Home (Usando los globales de Color.kt) ──
private val DeepBlack    = Color(0xFF030305)
private val CardBg       = Color(0xFF0E0E16)
private val CardBorder   = Color(0xFF1E1E2E)

@Composable
fun HomeScreen(
    navController: NavController, 
    authViewModel: AuthViewModel = viewModel(),
    moduleViewModel: ModuleViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.xoropower.data.SessionManager(context) }
    val userName = remember { sessionManager.fetchUserName() ?: "Estudiante" }
    val esAdmin = remember { sessionManager.isAdmin() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val moduleState by moduleViewModel.moduleState.collectAsState()

    // Mensajes motivacionales rotativos
    val messages = listOf(
        "El ritmo es el alma del llano." to "Sigue practicando tu pulso.",
        "La maraca es tu voz." to "Domina el escobillao.",
        "Disciplina y pasión." to "Sigue adelante, músico.",
        "Cada práctica cuenta." to "La perfección llega con constancia."
    )
    var msgIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            msgIdx = (msgIdx + 1) % messages.size
        }
    }

    // Animación de pulso para los glows de fondo
    val infiniteTransition = rememberInfiniteTransition(label = "home_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
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
        Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {

            // ── Glows de fondo izquierda/derecha ──────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Glow azul izquierda
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricBlue.copy(alpha = pulseAlpha), Color.Transparent),
                                center = Offset(0f, size.height * 0.25f),
                                radius = size.width * 0.7f
                            ),
                            center = Offset(0f, size.height * 0.25f),
                            radius = size.width * 0.7f
                        )
                        // Glow rojo derecha
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(BrightRed.copy(alpha = pulseAlpha), Color.Transparent),
                                center = Offset(size.width, size.height * 0.25f),
                                radius = size.width * 0.7f
                            ),
                            center = Offset(size.width, size.height * 0.25f),
                            radius = size.width * 0.7f
                        )
                    }
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
                bottomBar = { BottomNavBar(navController, Routes.HOME) }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {

                    // ── HERO: Imagen de maracas full-width ──────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.home),
                                contentDescription = "Maracas xoropower",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Degradado inferior para que el texto flote encima
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                DeepBlack.copy(alpha = 0.6f),
                                                DeepBlack
                                            )
                                        )
                                    )
                            )
                            // Texto sobre la imagen
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Text(
                                    text = "BIENVENIDO",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color.White)) {
                                            append(userName.split(" ").firstOrNull() ?: userName)
                                        }
                                    },
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    style = TextStyle(
                                        shadow = Shadow(Color.Black.copy(alpha = 0.9f), Offset(3f, 3f), 8f)
                                    )
                                )
                                // Acento decorativo
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .width(28.dp)
                                            .height(3.dp)
                                            .background(ElectricBlue)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        Modifier
                                            .width(8.dp)
                                            .height(3.dp)
                                            .background(Color.White)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        Modifier
                                            .width(28.dp)
                                            .height(3.dp)
                                            .background(BrightRed)
                                    )
                                }
                            }
                        }
                    }

                    // ── MENSAJE MOTIVACIONAL ROTATIVO ───────────
                    item {
                        Spacer(Modifier.height(20.dp))
                        AnimatedContent(
                            targetState = msgIdx,
                            transitionSpec = {
                                (fadeIn(tween(500)) + slideInVertically { -it / 3 })
                                    .togetherWith(fadeOut(tween(300)))
                            },
                            label = "msg_anim"
                        ) { idx ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg)
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(ElectricBlue.copy(0.4f), BrightRed.copy(0.4f))
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎵", fontSize = 28.sp)
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            messages[idx].first,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            messages[idx].second,
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                    }

                    // ── ACCESOS RÁPIDOS ─────────────────────────
                    item {
                        Text(
                            "ACCESO RÁPIDO",
                            color = Color.White.copy(0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickAccessCard(
                                icon = Icons.Outlined.Map,
                                label = "Progreso",
                                color = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            ) { navController.navigate(Routes.PROGRESO) }

                            QuickAccessCard(
                                icon = Icons.Outlined.Book,
                                label = "Categorías",
                                color = Color(0xFFFFAA00),
                                modifier = Modifier.weight(1f)
                            ) { navController.navigate(Routes.CATEGORIES) }

                            QuickAccessCard(
                                icon = Icons.Outlined.Person,
                                label = "Perfil",
                                color = BrightRed,
                                modifier = Modifier.weight(1f)
                            ) { navController.navigate(Routes.PROFILE) }
                        }

                        // Botón exclusivo de Admin
                        if (esAdmin) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                QuickAccessCard(
                                    icon = Icons.Outlined.Add,
                                    label = "Crear Ejercicio",
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.weight(1f)
                                ) { navController.navigate(Routes.ADMIN_ADD_EXERCISE) }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }

                    // ── MÓDULOS ──────────────────────────────────
                    item {
                        Text(
                            "TUS MÓDULOS",
                            color = Color.White.copy(0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                    }

                    when (val state = moduleState) {
                        is ModuleState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = ElectricBlue, strokeWidth = 2.dp)
                                }
                            }
                        }
                        is ModuleState.Error -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BrightRed.copy(0.1f))
                                        .border(1.dp, BrightRed.copy(0.3f), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                ) {
                                    Text("Error al cargar módulos", color = BrightRed, fontSize = 14.sp)
                                }
                            }
                        }
                        is ModuleState.Success -> {
                            itemsIndexed(state.modules) { index, module ->
                                PremiumModuleCard(
                                    index = index + 1,
                                    module = module,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                ) {
                                    navController.navigate(Routes.CATEGORIES)
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── Tarjeta de acceso rápido ──────────────────────────────────
@Composable
fun QuickAccessCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, color = Color.White.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Tarjeta de módulo premium ─────────────────────────────────
@Composable
fun PremiumModuleCard(
    index: Int,
    module: com.xoropower.data.Modulo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Alterna el color de acento entre azul y rojo
    val accentColor = if (index % 2 == 1) ElectricBlue else BrightRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número del módulo
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$index",
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                style = TextStyle(
                    shadow = Shadow(accentColor.copy(0.5f), blurRadius = 8f)
                )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                module.titulo,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                module.descripcion ?: "Explora este módulo",
                color = Color.White.copy(0.45f),
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = accentColor.copy(0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}
