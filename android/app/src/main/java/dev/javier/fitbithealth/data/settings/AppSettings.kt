package dev.javier.fitbithealth.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Ajustes de conexión con fallback seguro: si el almacenamiento cifrado
 * falla (keystore corrupto, claves previas incompatibles, migraciones),
 * se usa SharedPreferences planas para que la app nunca crashee.
 */
class AppSettings(context: Context) {

    private val preferences: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "fitbit_health_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse { error ->
        Log.w(TAG, "Encrypted prefs no disponible, usando planas: ${error.message}")
        context.getSharedPreferences("fitbit_health_settings", Context.MODE_PRIVATE)
    }

    var gatewayUrl: String
        get() = preferences.getString(KEY_GATEWAY_URL, "") ?: ""
        set(value) {
            runCatching {
                preferences.edit().putString(KEY_GATEWAY_URL, value.trim()).apply()
            }.onFailure { Log.e(TAG, "No se pudo guardar la URL", it) }
        }

    var gatewayToken: String
        get() = preferences.getString(KEY_GATEWAY_TOKEN, "") ?: ""
        set(value) {
            runCatching {
                preferences.edit().putString(KEY_GATEWAY_TOKEN, value).apply()
            }.onFailure { Log.e(TAG, "No se pudo guardar el token", it) }
        }

    fun clearCredentials() {
        runCatching {
            preferences.edit()
                .remove(KEY_GATEWAY_URL)
                .remove(KEY_GATEWAY_TOKEN)
                .apply()
        }
    }

    companion object {
        private const val TAG = "AppSettings"
        private const val KEY_GATEWAY_URL = "gateway_url"
        private const val KEY_GATEWAY_TOKEN = "gateway_token"
    }
}
