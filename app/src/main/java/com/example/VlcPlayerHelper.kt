package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object VlcPlayerHelper {
    fun playUrl(context: Context, url: String, title: String = "Streaming") {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(url), "video/*")
                setPackage("org.videolan.vlc")
                putExtra("title", title)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If VLC app is not installed, fallback to general video player intent
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(url), "video/*")
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "No se encontró reproductor compatible (VLC)", Toast.LENGTH_LONG).show()
            }
        }
    }
}
