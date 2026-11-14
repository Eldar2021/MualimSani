package com.muallimsaniqr.flutter_app.data.videos

import org.json.JSONObject

/**
 * Sunucudaki ders videolarinin listesi.
 *
 * `manifest.json` dosyasi `scripts/publish-videos.sh` ile uretilir ve videolarla
 * birlikte GitHub Release'e yuklenir. Ornek:
 *
 * ```json
 * { "version": 1, "videos": [ { "name": "1.mp4", "size": 12345678 } ] }
 * ```
 */
data class VideoManifest(val version: Int, val videos: List<RemoteVideo>) {

    companion object {
        fun parse(json: String): VideoManifest {
            val root = JSONObject(json)
            val array = root.getJSONArray("videos")
            val videos = List(array.length()) { i ->
                val item = array.getJSONObject(i)
                RemoteVideo(name = item.getString("name"), size = item.getLong("size"))
            }
            return VideoManifest(version = root.optInt("version", 1), videos = videos)
        }
    }
}

/** Sunucudaki tek bir video. [size] bayt cinsindendir ve indirmenin tam oldugunu dogrulamak icin kullanilir. */
data class RemoteVideo(val name: String, val size: Long)
