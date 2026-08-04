package dev.javier.fitbithealth

import android.content.Context
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.HealthApiFactory
import dev.javier.fitbithealth.data.settings.AppSettings

class AppContainer(context: Context) {
    private val settings = AppSettings(context)
    private val factory = HealthApiFactory()

    fun currentSettings(): AppSettings = settings

    /** Devuelve par (url, token) si configurado, si no null. */
    fun credentialsOrNull(): Pair<String, String>? {
        val url = settings.gatewayUrl
        val token = settings.gatewayToken
        if (url.isBlank() || token.isBlank()) return null
        return url to token
    }

    fun apiOrNull(): HealthApi? {
        val (url, token) = credentialsOrNull() ?: return null
        return runCatching { factory.create(url, token) }.getOrNull()
    }

    fun factory(): HealthApiFactory = factory
}
