package com.xoropower.data.repository

import com.xoropower.data.Modulo
import com.xoropower.data.Seccion
import com.xoropower.data.network.RetrofitClient
import com.xoropower.data.network.XoroApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

open class ModuleRepository(private val api: XoroApiService = RetrofitClient.apiService) {

    suspend fun obtenerModulos(): Flow<List<Modulo>> = flow {
        val response = api.getModulos()
        if (response.isSuccessful) {
            response.body()?.data?.let { emit(it) }
        } else {
            emit(emptyList())
        }
    }

    suspend fun obtenerDetalleModulo(identificador: String): Flow<Modulo?> = flow {
        val response = api.getModuloDetalle(identificador)
        if (response.isSuccessful) {
            emit(response.body()?.data)
        } else {
            emit(null)
        }
    }

    suspend fun obtenerSeccion(identificador: String, nivel: String): Flow<Seccion?> = flow {
        val response = api.getSeccionPorNivel(identificador, nivel)
        if (response.isSuccessful) {
            emit(response.body()?.data)
        } else {
            emit(null)
        }
    }
}
