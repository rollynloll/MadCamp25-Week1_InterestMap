package com.example.madclass01.core

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
    }
    
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
