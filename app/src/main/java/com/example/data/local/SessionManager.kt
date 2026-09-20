package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("al_abrar_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val userAdapter = moshi.adapter(User::class.java)

    companion object {
        const val DEFAULT_BASE_URL = "https://abrar.bsmartshield.com/backend/"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER = "user_json"
        private const val KEY_LANG = "app_language"
        private const val KEY_BASE_URL = "base_url"
    }

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    var user: User?
        get() {
            val json = prefs.getString(KEY_USER, null) ?: return null
            return try {
                userAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value != null) {
                val json = userAdapter.toJson(value)
                prefs.edit().putString(KEY_USER, json).apply()
            } else {
                prefs.edit().remove(KEY_USER).apply()
            }
        }

    var language: String
        get() = prefs.getString(KEY_LANG, "ar") ?: "ar"
        set(value) {
            prefs.edit().putString(KEY_LANG, value).apply()
        }

    var baseUrl: String
        get() {
            val url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
            return if (url.endsWith("/")) url else "$url/"
        }
        set(value) {
            val clean = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_BASE_URL, clean).apply()
        }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .apply()
    }
}
