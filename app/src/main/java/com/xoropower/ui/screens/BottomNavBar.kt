package com.xoropower.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xoropower.navigation.Routes
import com.xoropower.ui.theme.*

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0D0D0D).copy(alpha = 0.98f), // Fondo oscuro premium
        tonalElevation = 8.dp
    ) {
        Column {
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(Icons.Default.Home, "Inicio", currentRoute == Routes.HOME) { 
                    if (currentRoute != Routes.HOME) navController.navigate(Routes.HOME) 
                }
                NavItem(Icons.AutoMirrored.Filled.LibraryBooks, "Categorías", currentRoute == Routes.CATEGORIES) { 
                    if (currentRoute != Routes.CATEGORIES) navController.navigate(Routes.CATEGORIES) 
                }
                NavItem(Icons.Default.GridView, "Progreso", currentRoute == Routes.PROGRESO) { 
                    if (currentRoute != Routes.PROGRESO) navController.navigate(Routes.PROGRESO) 
                }
                NavItem(Icons.Default.Person, "Perfil", currentRoute == Routes.PROFILE) { 
                    if (currentRoute != Routes.PROFILE) navController.navigate(Routes.PROFILE) 
                }
            }
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    val activeColor = XoroBlue
    val inactiveColor = Color(0xFF666666)
    
    Box(
        modifier = Modifier
            .height(56.dp)
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isActive) activeColor else inactiveColor,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
            
            if (isActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 2.dp)
                        .background(activeColor, CircleShape)
                )
            }
        }
    }
}
