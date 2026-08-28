package com.example

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

object GitHubUpdater {
    // Configuración del repositorio en GitHub para actualizaciones
    private const val GITHUB_OWNER = "pjaraf"
    private const val GITHUB_REPO = "TV-Broadcast"
    
    fun checkForUpdates(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    if (jsonResponse != null) {
                        val jsonObject = JSONObject(jsonResponse)
                        val tagName = jsonObject.getString("tag_name")
                        val assets = jsonObject.getJSONArray("assets")
                        var downloadUrl: String? = null
                        
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.getString("name")
                            if (name.endsWith(".apk")) {
                                downloadUrl = asset.getString("browser_download_url")
                                break
                            }
                        }

                        val currentVersion = BuildConfig.VERSION_NAME
                        // Comparación simple de versión
                        val cleanLatest = tagName.replace("v", "")
                        val cleanCurrent = currentVersion.replace("v", "")
                        
                        if (cleanLatest != cleanCurrent && downloadUrl != null) {
                            withContext(Dispatchers.Main) {
                                showUpdateDialog(context, tagName, downloadUrl)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GitHubUpdater", "Error verificando actualizaciones", e)
            }
        }
    }

    private fun showUpdateDialog(context: Context, newVersion: String, downloadUrl: String) {
        AlertDialog.Builder(context)
            .setTitle("Actualización Disponible")
            .setMessage("La versión $newVersion está disponible para descargar. ¿Deseas actualizar ahora?")
            .setPositiveButton("Actualizar") { _, _ ->
                downloadAndInstallUpdate(context, downloadUrl)
            }
            .setNegativeButton("Más tarde", null)
            .show()
    }

    private fun downloadAndInstallUpdate(context: Context, apkUrl: String) {
        Toast.makeText(context, "Descargando actualización...", Toast.LENGTH_SHORT).show()
        
        // Eliminar APK anterior si existe para evitar conflictos
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (file.exists()) {
            file.delete()
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Actualización de la App")
            .setDescription("Descargando la nueva versión...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(context)
                    context.unregisterReceiver(this)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(context: Context) {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("GitHubUpdater", "Error al instalar el APK", e)
            Toast.makeText(context, "Error al instalar la actualización", Toast.LENGTH_LONG).show()
        }
    }
}
