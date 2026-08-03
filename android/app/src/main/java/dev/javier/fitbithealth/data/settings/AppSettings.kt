package dev.javier.fitbithealth.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AppSettings(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "fitbit_health_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var gatewayUrl: String
        get() = preferences.getString(KEY_GATEWAY_URL, DEBUG_URL) ?: DEBUG_URL
        set(value) = preferences.edit().putString(KEY_GATEWAY_URL, value.trim()).apply()

    var gatewayToken: String
        get() = preferences.getString(KEY_GATEWAY_TOKEN, DEBUG_TOKEN) ?: DEBUG_TOKEN
        set(value) = preferences.edit().putString(KEY_GATEWAY_TOKEN, value).apply()

    fun clearCredentials() {
        preferences.edit()
            .remove(KEY_GATEWAY_URL)
            .remove(KEY_GATEWAY_TOKEN)
            .apply()
    }

    companion object {
        private const val KEY_GATEWAY_URL = "gateway_url"
        private const val KEY_GATEWAY_TOKEN = "gateway_token"
        // Debug defaults for emulator development
        private const val DEBUG_URL = "http://10.0.2.2:8844"
        private const val DEBUG_TOKEN = "fitbit-gateway-token-15067f877145e724"
    }
}
