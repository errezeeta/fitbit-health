package dev.javier.fitbithealth.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Comprueba en GitHub si hay una release más nueva del APK,
 * descarga el binario y lanza el flujo de instalación de Android.
 */
class AppUpdater(private val context: Context) {

    companion object {
        private const val TAG = "AppUpdater"
        private const val REPO_API = "https://api.github.com/repos/errezeeta/fitbit-health/releases/latest"
        private const val DOWNLOAD_BASE = "https://github.com/errezeeta/fitbit-health/releases/download"
        private const val APK_NAME = "app-debug.apk"
    }

    data class UpdateInfo(
        val latestVersion: String,
        val currentVersion: String,
        val isUpdateAvailable: Boolean,
        val releaseNotes: String = "",
        val downloadUrl: String = "",
    )

    fun currentVersion(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    }.getOrDefault("0.0.0")

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(REPO_API).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "FitbitHealth-Android")
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            val code = conn.responseCode
            if (code != 200) {
                Log.w(TAG, "GitHub API respondió $code")
                return@runCatching null
            }
            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            val tag = json.optString("tag_name", "v0.0.0").removePrefix("v")
            val notes = json.optString("body", "").take(600)
            val assetUrl = json.optJSONArray("assets")?.let { assets ->
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name") == APK_NAME) {
                        return@let asset.optString("browser_download_url", "")
                    }
                }
                ""
            } ?: ""
            val current = currentVersion()
            UpdateInfo(
                latestVersion = tag,
                currentVersion = current,
                isUpdateAvailable = compareVersions(tag, current) > 0 && assetUrl.isNotBlank(),
                releaseNotes = notes,
                downloadUrl = assetUrl,
            )
        }.getOrElse { error ->
            Log.e(TAG, "Error al comprobar update: ${error.message}")
            null
        }
    }

    suspend fun downloadAndInstall(info: UpdateInfo, onProgress: ((Long, Long) -> Unit)? = null): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val url = if (info.downloadUrl.isNotBlank()) info.downloadUrl
            else "$DOWNLOAD_BASE/v${info.latestVersion}/$APK_NAME"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 60_000
            conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..399) {
                error("Descarga fallida: HTTP ${conn.responseCode}")
            }
            val total = conn.contentLengthLong.coerceAtLeast(1L)
            val target = File(context.cacheDir, "fitbit-health-$APK_NAME")
            conn.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (downloaded % (512 * 1024) < 8192) {
                            onProgress?.invoke(downloaded, total)
                        }
                    }
                    onProgress?.invoke(downloaded, total)
                }
            }
            if (!target.exists() || target.length() < 1_000_000) {
                error("APK descargado incompleto (${target.length()} bytes)")
            }
            target
        }.onFailure { Log.e(TAG, "Descarga fallida: ${it.message}") }
    }

    /** ¿Podemos instalar APKs directamente? (Android 8+ requiere permiso explícito). */
    fun canInstallUnknownApps(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    /** Lanza el diálogo del sistema para habilitar "Instalar apps desconocidas". */
    fun requestInstallPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /** Lanza el instalador de Android con el APK descargado (FileProvider). */
    fun promptInstall(apk: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Log.e(TAG, "No se pudo abrir el instalador: ${it.message}")
            // Fallback: abrir el navegador con la release
            val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/errezeeta/fitbit-health/releases/latest"))
            context.startActivity(web)
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val pa = a.split(".").mapNotNull { it.toIntOrNull() }
        val pb = b.split(".").mapNotNull { it.toIntOrNull() }
        val max = maxOf(pa.size, pb.size)
        for (i in 0 until max) {
            val va = pa.getOrElse(i) { 0 }
            val vb = pb.getOrElse(i) { 0 }
            if (va != vb) return va - vb
        }
        return 0
    }
}
