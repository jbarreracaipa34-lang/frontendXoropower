package com.xoropower.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xoropower.data.LoginDto
import com.xoropower.data.PromoverAdminDto
import com.xoropower.data.SessionManager
import com.xoropower.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xoropower.data.ApiResponse
import com.xoropower.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val api = RetrofitClient.apiService

    init {
        checkSession()
    }

    private fun checkSession() {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = api.login(LoginDto(email, pass))
                val body = response.body()
                
                if (response.isSuccessful && body?.success == true && body.data != null) {
                    val data = body.data!!
                    sessionManager.saveAuthToken(
                        token = data.accessToken,
                        name = data.usuario.nombreCompleto,
                        email = data.usuario.email,
                        avatar = data.usuario.avatar ?: "🤠",
                        rol = data.usuario.rol ?: "estudiante"
                    )
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = body?.error ?: "Error en el inicio de sesión"
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }
    
    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = api.register(com.xoropower.data.RegistroDto(name, email, pass))
                if (response.isSuccessful && response.body()?.success == true) {
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = response.body()?.error ?: "Error en el registro"
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthState.Idle
        checkSession()
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Métodos para el perfil
    fun getUserName() = sessionManager.fetchUserName()
    fun getUserEmail() = sessionManager.fetchUserEmail()
    fun getUserAvatar() = sessionManager.fetchUserAvatar()
    fun isAdmin() = sessionManager.isAdmin()

    fun promoverAdmin(email: String, password: String, onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.promoverAdmin(PromoverAdminDto(email.trim(), password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onResult(true, body.message ?: "Usuario promovido a administrador.")
                    } else {
                        onResult(false, body?.message ?: body?.error ?: "No se pudo promover al usuario.")
                    }
                } else {
                    // Leer el cuerpo del error (4xx, 5xx)
                    val errorJson = response.errorBody()?.string()
                    val errorMsg = try {
                        val type = object : TypeToken<ApiResponse<Usuario>>() {}.type
                        val errorBody = Gson().fromJson<ApiResponse<Usuario>>(errorJson, type)
                        errorBody?.message ?: errorBody?.error ?: "Error ${response.code()}"
                    } catch (_: Exception) {
                        errorJson ?: "Error ${response.code()}"
                    }
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) {
                onResult(false, "Error de conexión: ${e.message}")
            }
        }
    }
}
