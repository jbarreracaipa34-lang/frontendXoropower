package com.xoropower.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.xoropower.R
import com.xoropower.navigation.Routes
import com.xoropower.ui.theme.*
import com.xoropower.ui.viewmodel.AuthState
import com.xoropower.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Routes.LOGIN)
            authViewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(XoroBlack)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de encabezado principal (Hero Section) con la ilustración de registro.
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.crear_usuario),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, XoroBlack),
                                startY = 300f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(top = 180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = ElectricBlue)) { append("XORO") }
                            withStyle(SpanStyle(color = Color.White)) { append("PO") }
                            withStyle(SpanStyle(color = BrightRed)) { append("WER") }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                blurRadius = 10f
                            )
                        )
                    )
                    Text("COMIENZA TU VIAJE", color = BrightRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                XoroTextField(
                    label = "NOMBRE DE USUARIO",
                    value = name,
                    onValueChange = { name = it },
                    icon = Icons.Outlined.Person,
                    placeholder = "Ej: Juan Pérez"
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                XoroTextField(
                    label = "CORREO ELECTRÓNICO",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Outlined.Mail,
                    placeholder = "tu@correo.com"
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                XoroPasswordField(
                    label = "CONTRASEÑA",
                    value = password,
                    onValueChange = { password = it },
                    visible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { authViewModel.register(name, email, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightRed),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CREAR MI CUENTA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes una cuenta? ", color = Color.White.copy(alpha = 0.5f), fontSize = 15.sp)
                Text("Inicia sesión", color = ElectricBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { navController.navigate(Routes.LOGIN) })
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}