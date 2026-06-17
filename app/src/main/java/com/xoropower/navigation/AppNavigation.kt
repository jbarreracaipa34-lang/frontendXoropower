package com.xoropower.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xoropower.ui.screens.*
import com.xoropower.data.SessionManager
import com.xoropower.ui.viewmodel.AuthViewModel
import com.xoropower.ui.viewmodel.ModuleViewModel
import com.xoropower.ui.viewmodel.EjerciciosListViewModel

// RUTAS DE LA APP
object Routes {
    const val SPLASH     = "splash"
    const val LOGIN      = "login"
    const val REGISTER   = "register"
    const val HOME       = "home"
    const val PROGRESO    = "progreso"
    const val EXERCISE   = "exercise"
    const val CATEGORIES  = "categories"
    const val PROFILE     = "profile"
    const val INSTRUCTION = "instruction"
    const val INSTRUCTION_PARAM = "instruction/{tipoEjercicio}"
    const val RITMO       = "ritmo"
    const val ADMIN_ADD_EXERCISE = "admin_add_exercise"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    moduleViewModel: ModuleViewModel = viewModel(),
    ejViewModel: EjerciciosListViewModel = viewModel()
) {
    val navController = rememberNavController()

    // El splash siempre es el punto de entrada; él decide a dónde ir después
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH)     { SplashScreen(navController, authViewModel) }
        composable(Routes.LOGIN)      { LoginScreen(navController, authViewModel) }
        composable(Routes.REGISTER)   { RegisterScreen(navController, authViewModel) }
        
        // Pasamos los ViewModels compartidos a todas las pantallas
        composable(Routes.HOME)       { HomeScreen(navController, authViewModel, moduleViewModel) }
        composable(Routes.PROGRESO)   { ProgresoScreen(navController, authViewModel) }
        composable(Routes.EXERCISE)   { ExerciseScreen(onBackClick = { navController.popBackStack() }) }
        composable("${Routes.CATEGORIES}?reset={reset}") { backStackEntry ->
            val reset = backStackEntry.arguments?.getString("reset") == "true"
            CategoryScreen(navController, authViewModel, moduleViewModel, ejViewModel, reset = reset)
        }
        composable(Routes.PROFILE)    { ProfileScreen(navController, authViewModel) }
        // Instrucciones con tipo de ejercicio (derecha, izquierda, juntas)
        composable(Routes.INSTRUCTION_PARAM) { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipoEjercicio") ?: "juntas"
            InstructionScreen(navController, moduleViewModel, ejViewModel, tipoEjercicio = tipo)
        }
        // Ruta legacy sin parámetro (por compatibilidad)
        composable(Routes.INSTRUCTION) { InstructionScreen(navController, moduleViewModel, ejViewModel) }
        composable(Routes.RITMO + "/{idEjercicio}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("idEjercicio") ?: ""
            PantallaRitmo(idEjercicio = id, onVolverClick = {
                navController.navigate(Routes.CATEGORIES) {
                    popUpTo(Routes.CATEGORIES) { inclusive = true }
                }
            })
        }
        composable(Routes.ADMIN_ADD_EXERCISE) {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            if (sessionManager.isAdmin()) {
                AdminAddExerciseScreen(
                    onVolverClick = { navController.popBackStack() }
                )
            } else {
                // Redirigir si un estudiante intenta acceder directamente
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}
