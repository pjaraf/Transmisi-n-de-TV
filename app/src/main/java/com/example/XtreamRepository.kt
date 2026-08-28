package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class XtreamItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val streamUrl: String
)

object XtreamRepository {
    private val client = OkHttpClient()

    suspend fun getLiveStreams(): List<XtreamItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<XtreamItem>()
        try {
            val url = "${ServerConfig.SERVER_URL}/player_api.php?username=${UserSession.username}&password=${UserSession.password}&action=get_live_streams"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val streamId = obj.optString("stream_id", "")
                    val name = obj.optString("name", "Canal")
                    val icon = obj.optString("stream_icon", "")
                    val streamUrl = "${ServerConfig.SERVER_URL}/live/${UserSession.username}/${UserSession.password}/$streamId.ts"
                    if (streamId.isNotBlank()) {
                        list.add(XtreamItem(streamId, name, icon, streamUrl))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("XtreamRepo", "Error fetching live streams", e)
        }
        list
    }

    suspend fun getVodStreams(): List<XtreamItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<XtreamItem>()
        try {
            val url = "${ServerConfig.SERVER_URL}/player_api.php?username=${UserSession.username}&password=${UserSession.password}&action=get_vod_streams"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val streamId = obj.optString("stream_id", "")
                    val name = obj.optString("name", "Película")
                    val icon = obj.optString("stream_icon", "")
                    val extension = obj.optString("container_extension", "mp4")
                    val streamUrl = "${ServerConfig.SERVER_URL}/movie/${UserSession.username}/${UserSession.password}/$streamId.$extension"
                    if (streamId.isNotBlank()) {
                        list.add(XtreamItem(streamId, name, icon, streamUrl))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("XtreamRepo", "Error fetching vod streams", e)
        }
        list
    }

    suspend fun getSeries(): List<XtreamItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<XtreamItem>()
        try {
            val url = "${ServerConfig.SERVER_URL}/player_api.php?username=${UserSession.username}&password=${UserSession.password}&action=get_series"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val seriesId = obj.optString("series_id", "")
                    val name = obj.optString("name", "Serie")
                    val icon = obj.optString("cover", "")
                    // For series, fallback stream or detail
                    val streamUrl = "${ServerConfig.SERVER_URL}/series/${UserSession.username}/${UserSession.password}/$seriesId.mp4"
                    if (seriesId.isNotBlank()) {
                        list.add(XtreamItem(seriesId, name, icon, streamUrl))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("XtreamRepo", "Error fetching series", e)
        }
        list
    }
}
