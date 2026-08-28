package com.example

import android.content.Context

object VlcPlayerHelper {
    fun playUrl(context: Context, url: String, title: String = "Streaming") {
        // Play directly inside our built-in video player activity in the APK
        PlayerActivity.start(context, url, title)
    }
}
