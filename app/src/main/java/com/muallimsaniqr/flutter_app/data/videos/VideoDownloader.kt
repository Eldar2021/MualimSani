package com.muallimsaniqr.flutter_app.data.videos

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Videolari dogrudan HTTPS ile indirir (GitHub Releases veya herhangi bir statik sunucu).
 *
 * [baseUrl] `/` ile bitmelidir; `manifest.json` ve videolar bu adresin altinda aranir.
 * Firebase veya baska bir SDK gerekmez.
 */
class VideoDownloader(private val baseUrl: String) {

    fun fetchManifest(): VideoManifest {
        val connection = open("manifest.json")
        try {
            connection.expectStatus(HttpURLConnection.HTTP_OK)
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return VideoManifest.parse(body)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * [video] dosyasini [partFile] icine indirir. [partFile] zaten kismen doluysa
     * indirme kaldigi yerden devam eder (HTTP Range). Okunan her blok icin
     * [onBytes] yeni okunan bayt sayisiyla cagrilir.
     */
    fun download(video: RemoteVideo, partFile: File, onBytes: (Long) -> Unit) {
        val existing = if (partFile.exists()) partFile.length() else 0L
        if (existing >= video.size) {
            // Beklenenden buyuk/esit kismi dosya: guvenmek yerine bastan indir.
            partFile.delete()
            return download(video, partFile, onBytes)
        }

        val connection = open(video.name)
        try {
            if (existing > 0) connection.setRequestProperty("Range", "bytes=$existing-")

            val append = when (connection.responseCode) {
                HttpURLConnection.HTTP_PARTIAL -> true
                HttpURLConnection.HTTP_OK -> false // sunucu Range desteklemiyor: bastan yaz
                else -> throw IOException("${video.name}: HTTP ${connection.responseCode}")
            }
            if (append) onBytes(existing) else partFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(partFile, append).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        onBytes(read.toLong())
                    }
                }
            }
        } finally {
            connection.disconnect()
        }

        val actual = partFile.length()
        if (actual != video.size) {
            throw IOException("${video.name}: boyut uyusmuyor ($actual != ${video.size})")
        }
    }

    private fun open(path: String): HttpURLConnection =
        (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true // GitHub, dosyalari CDN adresine yonlendirir
            setRequestProperty("Accept-Encoding", "identity") // boyut dogrulamasi icin sikistirma yok
        }

    private fun HttpURLConnection.expectStatus(code: Int) {
        if (responseCode != code) throw IOException("${url}: HTTP $responseCode")
    }

    private companion object {
        const val TIMEOUT_MS = 30_000
        const val BUFFER_SIZE = 64 * 1024
    }
}
