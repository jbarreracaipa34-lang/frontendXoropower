package com.xoropower.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SessionManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Mock SharedPreferences
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        
        // Mock Editor returns itself for chaining
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)

        sessionManager = SessionManager(mockContext)
    }

    @Test
    fun `saveAuthToken should save all credentials`() {
        sessionManager.saveAuthToken("fake_token", "Test User", "test@test.com", "🤠")

        verify(mockEditor).putString(SessionManager.USER_TOKEN, "fake_token")
        verify(mockEditor).putString(SessionManager.USER_NAME, "Test User")
        verify(mockEditor).putString(SessionManager.USER_EMAIL, "test@test.com")
        verify(mockEditor).putString(SessionManager.USER_AVATAR, "🤠")
        verify(mockEditor).apply()
    }

    @Test
    fun `fetchAuthToken should return token when exists`() {
        `when`(mockPrefs.getString(SessionManager.USER_TOKEN, null)).thenReturn("fake_token")
        
        val token = sessionManager.fetchAuthToken()
        
        assertEquals("fake_token", token)
    }

    @Test
    fun `fetchAuthToken should return null when not exists`() {
        `when`(mockPrefs.getString(SessionManager.USER_TOKEN, null)).thenReturn(null)
        
        val token = sessionManager.fetchAuthToken()
        
        assertNull(token)
    }

    @Test
    fun `clearSession should clear all preferences`() {
        sessionManager.clearSession()
        
        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }
}
