package com.example.careplus.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.careplus.data.model.User
import com.example.careplus.services.PusherService
import com.google.gson.Gson

class SessionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val PREFS_NAME = "care_plus_prefs"
        const val USER_TOKEN = "user_token"
        const val USER_DATA = "user_data"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUser(user: User) {
        val editor = prefs.edit()
        editor.putString(USER_DATA, gson.toJson(user))
        editor.apply()
    }

    fun getUser(): User? {
        val userData = prefs.getString(USER_DATA, null)
        return if (userData != null) {
            gson.fromJson(userData, User::class.java)
        } else null
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null && getUser() != null
    }

    fun startPusherService() {
        getUser()?.let { user ->
            Intent(context, PusherService::class.java).also { intent ->
                intent.putExtra(PusherService.EXTRA_PATIENT_ID, user.id.toString())
                context.startService(intent)
            }
        }
    }

    fun stopPusherService() {
        context.stopService(Intent(context, PusherService::class.java))
    }

    fun clearSession() {
        stopPusherService()
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun saveFcmToken(token: String) {
        prefs.edit().putString("fcm_token", token).apply()
    }

    fun getFcmToken(): String? {
        return prefs.getString("fcm_token", null)
    }
} 