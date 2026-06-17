package com.xoropower.data.repository

import com.xoropower.data.Modulo
import com.xoropower.data.Seccion
import com.xoropower.data.ApiResponse
import com.xoropower.data.network.XoroApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response

class ModuleRepositoryTest {

    @Mock
    private lateinit var mockApi: XoroApiService

    private lateinit var repository: ModuleRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ModuleRepository(mockApi)
    }

    @Test
    fun `obtenerModulos returns list on success`() = runTest {
        val fakeModulos = listOf(
            Modulo("1", "id1", "Titulo 1", "Desc 1", null, 1),
            Modulo("2", "id2", "Titulo 2", "Desc 2", null, 2)
        )
        val apiResponse = ApiResponse(fakeModulos, null, true)

        `when`(mockApi.getModulos()).thenReturn(Response.success(apiResponse))

        val result = repository.obtenerModulos().first()

        assertEquals(2, result.size)
        assertEquals("Titulo 1", result[0].titulo)
    }

    @Test
    fun `obtenerModulos returns empty list on failure`() = runTest {
        `when`(mockApi.getModulos())
            .thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "")))

        val result = repository.obtenerModulos().first()

        assertTrue(result.isEmpty())
    }
}
