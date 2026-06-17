package com.xoropower.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.xoropower.ui.viewmodel.AuthState
import com.xoropower.ui.viewmodel.AuthViewModel
import com.xoropower.ui.theme.*
import kotlinx.coroutines.delay

// Se utilizan los colores globales definidos en Color.kt.
private val PureWhite    = Color(0xFFFFFFFF)
private val DeepBlack    = Color(0xFF030305)

private data class SplashSlide(
    val mostrarSubtitulo: Boolean = false,
    val mostrarCuadroInfo: Boolean = false,
    val duracionMs: Long = 2500L
)

private val slides = listOf(
    SplashSlide(mostrarSubtitulo = false, mostrarCuadroInfo = false, duracionMs = 4500L),
    SplashSlide(mostrarSubtitulo = true,  mostrarCuadroInfo = false, duracionMs = 4800L),
    SplashSlide(mostrarSubtitulo = true,  mostrarCuadroInfo = true,  duracionMs = 5500L)
)

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var slideActual by remember { mutableIntStateOf(0) }
    val authState by authViewModel.authState.collectAsState()

    // Control de la transición de diapositivas (slides) y la navegación de salida de la pantalla.
    LaunchedEffect(slideActual) {
        delay(slides[slideActual].duracionMs)
        if (slideActual < slides.lastIndex) {
            slideActual++
        } else {
            // Doble validación: estado del ViewModel y verificación de existencia del token de sesión.
            val hasToken = com.xoropower.data.SessionManager(navController.context).fetchAuthToken() != null
            val destino = if (authState is AuthState.Authenticated && hasToken) Routes.HOME else Routes.LOGIN
            
            navController.navigate(destino) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    // Efecto visual de iluminación de fondo con pulsaciones suaves.
    val infiniteTransition = rememberInfiniteTransition(label = "glow_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            // Definición de bordes e iluminaciones radiales.
            .drawBehind {
                val strokeWidth = 3.dp.toPx()
                // Borde exterior con degradado suave.
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(ElectricBlue, BrightRed)
                    ),
                    size = size,
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(16.dp.toPx()) 
                )

                // Brillo de color azul (lado izquierdo).
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = pulseAlpha * 0.6f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.35f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.35f),
                    radius = size.width * 0.8f
                )
                // Brillo de color rojo (lado derecho).
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrightRed.copy(alpha = pulseAlpha * 0.6f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.35f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.35f),
                    radius = size.width * 0.8f
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Imagen representativa con animación interactiva.
            val escala = remember { Animatable(0.9f) }
            LaunchedEffect(slideActual) {
                escala.animateTo(
                    targetValue = 1.05f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maracas_splash),
                    contentDescription = "Maracas xoropower",
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(escala.value),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título principal de la aplicación.
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = ElectricBlue)) { append("XORO") }
                    withStyle(SpanStyle(color = PureWhite)) { append("PO") }
                    withStyle(SpanStyle(color = BrightRed)) { append("WER") }
                },
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(5f, 5f),
                        blurRadius = 12f
                    )
                ),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtítulo descriptivo e indicador del juego.
            AnimatedVisibility(
                visible = slides[slideActual].mostrarSubtitulo,
                enter = fadeIn(tween(800)) + slideInVertically { it / 3 },
                exit  = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "APRENDE MARACAS LLANERAS",
                        color = Color(0xFFE0E0E0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, BrightRed)
                                    )
                                )
                        )
                        Text(
                            text = "  JUGANDO  ",
                            color = ElectricBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 5.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = ElectricBlue.copy(alpha = 0.5f),
                                    blurRadius = 8f
                                )
                            )
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ElectricBlue, Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bloque informativo del tutor personal.
            AnimatedVisibility(
                visible = slides[slideActual].mostrarCuadroInfo,
                enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.95f),
                exit  = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x66000000))
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(listOf(ElectricBlue, BrightRed)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Elemento gráfico en forma de círculo con efecto neón.
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent)
                                .border(2.dp, ElectricBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💡", fontSize = 24.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Column {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = PureWhite)) { append("Hacia una experiencia\n") }
                                    withStyle(SpanStyle(color = ElectricBlue)) { append("100% interactiva.") }
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Próximamente, xoropower evolucionará para convertirse en tu tutor personal, capaz de analizar tus movimientos y guiarte paso a paso en la ejecución perfecta de cada ejercicio propuesto.",
                                color = Color(0xFFB0B0B0),
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador visual de progreso.
            PremiumProgressBar(slideActual = slideActual, totalSlides = slides.size)

            Spacer(modifier = Modifier.height(14.dp))

            // Etiqueta de carga con animación.
            PremiumLoadingText()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PremiumProgressBar(slideActual: Int, totalSlides: Int) {
    val progreso = remember { Animatable(0f) }
    val target = (slideActual + 1).toFloat() / totalSlides.toFloat()

    LaunchedEffect(slideActual) {
        progreso.animateTo(target, tween(1000, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(12.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progreso.value)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(ElectricBlue, PureWhite, BrightRed)
                    )
                )
        )
    }
}

@Composable
private fun PremiumLoadingText() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_anim")
    val animValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color_anim"
    )
    
    Row(horizontalArrangement = Arrangement.Center) {
        "LOADING...".forEachIndexed { i, c ->
            val isRed = (animValue * 10).toInt() == i
            val isBlue = (animValue * 10).toInt() - 1 == i
            
            val charColor = when {
                isRed -> BrightRed
                isBlue -> ElectricBlue
                else -> Color(0xFF666666)
            }
            
            Text(
                text  = c.toString(),
                color = charColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                style = TextStyle(
                    shadow = if (isRed || isBlue) Shadow(color = charColor.copy(alpha = 0.5f), blurRadius = 10f) else null
                )
            )
        }
    }
}
