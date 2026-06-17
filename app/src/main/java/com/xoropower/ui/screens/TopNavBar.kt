package com.xoropower.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xoropower.navigation.Routes
import com.xoropower.ui.theme.*
import com.xoropower.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.automirrored.filled.Logout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun xoropowerTopBar(onMenuClick: () -> Unit, navController: NavController, authViewModel: AuthViewModel = viewModel(), showBackIcon: Boolean = false) {
    val userAvatar = authViewModel.getUserAvatar()

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                if (showBackIcon) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = XoroWhite)
                } else {
                    Icon(Icons.Outlined.Menu, contentDescription = "Menú", tint = XoroWhite)
                }
            }
        },
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
        actions = {
            IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(XoroBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userAvatar, fontSize = 18.sp)
                }
            }
        }
    )
}

@Composable
fun DrawerContent(navController: NavController, onClose: () -> Unit, authViewModel: AuthViewModel = viewModel()) {
    val userName = authViewModel.getUserName() ?: "Estudiante"
    val userAvatar = authViewModel.getUserAvatar()
    val esAdmin = authViewModel.isAdmin()

    Column(
        modifier = Modifier.fillMaxHeight().width(280.dp)
            .background(XoroSurface).padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(XoroBlue.copy(alpha = 0.15f), XoroBlue.copy(alpha = 0.3f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(userAvatar, fontSize = 30.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(userName, color = XoroWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(
            if (esAdmin) "Administrador · Acceso total" else "Nivel 1 · Aprendiz",
            color = if (esAdmin) Color(0xFFFFD700) else XoroBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.08f))

        DrawerItem(Icons.Outlined.Home,    "Inicio")            { navController.navigate(Routes.HOME);       onClose() }
        DrawerItem(Icons.Outlined.Explore, "Categorías")        {
            navController.navigate("${Routes.CATEGORIES}?reset=true")
            onClose()
        }
        DrawerItem(Icons.Outlined.BarChart,"Progreso")          { navController.navigate(Routes.PROGRESO); onClose() }
        DrawerItem(Icons.Outlined.Person,  "Mi Perfil")         { navController.navigate(Routes.PROFILE);    onClose() }
        if (esAdmin) {
            DrawerItem(Icons.Outlined.Add, "Crear Ejercicio") {
                navController.navigate(Routes.ADMIN_ADD_EXERCISE)
                onClose()
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(12.dp))
        DrawerItem(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión") {
            authViewModel.logout()
            navController.navigate(Routes.LOGIN) { 
                popUpTo(0) { inclusive = true } 
            }
            onClose()
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = XoroMuted, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = XoroWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
