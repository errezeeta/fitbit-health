package dev.javier.fitbithealth

import android.content.Context
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.HealthApiFactory
import dev.javier.fitbithealth.data.settings.AppSettings

class AppContainer(context: Context) {
    private val settings = AppSettings(context)
    private val factory = HealthApiFactory()

    fun currentSettings(): AppSettings = settings

    fun apiOrNull(): HealthApi? {
        val url = settings.gatewayUrl
        val token = settings.gatewayToken
        if (url.isBlank() || token.isBlank()) return null
        return runCatching { factory.create(url, token) }.getOrNull()
    }
}
