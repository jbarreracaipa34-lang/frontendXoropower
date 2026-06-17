package com.xoropower.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xoropower.navigation.Routes
import com.xoropower.ui.theme.*
import com.xoropower.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val userName = authViewModel.getUserName() ?: "Estudiante"
    val userEmail = authViewModel.getUserEmail() ?: ""
    val userAvatar = authViewModel.getUserAvatar()
    val esAdmin = authViewModel.isAdmin()
    var emailPromover by remember { mutableStateOf("") }
    var passwordAdmin by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mensajePromover by remember { mutableStateOf<String?>(null) }
    var promoverOk by remember { mutableStateOf<Boolean?>(null) }
    var promoviendo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = XoroBlack,
        bottomBar = { BottomNavBar(navController, Routes.PROFILE) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 48.dp, bottom = 24.dp)) {
                    Text("CUENTA", color = XoroMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Perfil", color = XoroWhite, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(XoroSurface2, RoundedCornerShape(24.dp))
                        .border(1.dp, XoroBlue.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Brush.linearGradient(listOf(XoroBlue.copy(alpha = 0.15f), XoroBlue.copy(alpha = 0.3f))))
                            .border(2.dp, XoroBlue.copy(alpha = 0.4f), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Representa el avatar dinámico cargado desde la base de datos (se utiliza 🤠 por defecto).
                        Text(userAvatar, fontSize = 48.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(userName, color = XoroWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text(userEmail, color = XoroMuted, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = if (esAdmin) Color(0xFFFFD700).copy(alpha = 0.12f) else XoroBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.dp,
                            if (esAdmin) Color(0xFFFFD700).copy(alpha = 0.35f) else XoroBlue.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            if (esAdmin) "ADMINISTRADOR" else "ESTUDIANTE ACTIVO",
                            color = if (esAdmin) Color(0xFFFFD700) else XoroBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ProfileInfoRow(Icons.Outlined.Person, "NOMBRE DE USUARIO", userName)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoRow(Icons.Outlined.Mail, "CORREO ELECTRÓNICO", userEmail)
                if (esAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileInfoRow(Icons.Outlined.AdminPanelSettings, "ROL", "Administrador — acceso completo a todos los niveles")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (esAdmin) {
                item {
                    Button(
                        onClick = { navController.navigate(Routes.ADMIN_ADD_EXERCISE) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Icon(Icons.Outlined.Add, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Ejercicio", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(XoroSurface2, RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Text(
                            "PROMOVER ADMINISTRADOR",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Solo un admin puede dar acceso admin. El usuario debe estar registrado como estudiante.",
                            color = XoroMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = emailPromover,
                            onValueChange = { emailPromover = it },
                            label = { Text("Correo del usuario a promover") },
                            placeholder = { Text("usuario@correo.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = XoroWhite,
                                unfocusedTextColor = XoroWhite,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = passwordAdmin,
                            onValueChange = { passwordAdmin = it },
                            label = { Text("Tu contraseña (confirmación)") },
                            placeholder = { Text("••••••••") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible)
                                androidx.compose.ui.text.input.VisualTransformation.None
                            else
                                androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                        tint = XoroMuted
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = XoroWhite,
                                unfocusedTextColor = XoroWhite,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        mensajePromover?.let { msg ->
                            Text(
                                msg,
                                color = if (promoverOk == true) Color(0xFF81C784) else XoroRed,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                if (emailPromover.isBlank()) {
                                    mensajePromover = "Indica el correo del usuario a promover."
                                    promoverOk = false
                                    return@Button
                                }
                                if (passwordAdmin.isBlank()) {
                                    mensajePromover = "Debes confirmar tu contraseña."
                                    promoverOk = false
                                    return@Button
                                }
                                promoviendo = true
                                mensajePromover = null
                                authViewModel.promoverAdmin(emailPromover, passwordAdmin) { ok, msg ->
                                    promoviendo = false
                                    promoverOk = ok
                                    mensajePromover = msg
                                    if (ok) { emailPromover = ""; passwordAdmin = "" }
                                }
                            },
                            enabled = !promoviendo,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E))
                        ) {
                            if (promoviendo) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Promover a admin", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                ProfileMenuRow(Icons.Outlined.Info, "Acerca de xoropower", "Versión 1.0.0 Stable", XoroMuted)
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                OutlinedButton(
                    onClick = { 
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) { 
                            popUpTo(0) { inclusive = true } 
                        } 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, XoroRed.copy(alpha = 0.25f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = XoroRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(XoroSurface, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = XoroMuted, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = XoroMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(value, color = XoroWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, sub: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(XoroSurface, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = XoroWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(sub, color = XoroMuted, fontSize = 12.sp)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(16.dp))
    }
}
