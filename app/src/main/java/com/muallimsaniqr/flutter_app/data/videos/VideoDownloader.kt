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
        downloadFile(video.name, video.size, partFile, onTotal = {}, onBytes = onBytes)
    }

    /**
     * [name] dosyasini [partFile] icine indirir. [expectedSize] bilinmiyorsa (null) boyut
     * sunucunun yanitindan (Content-Length / Content-Range) alinir ve [onTotal] ile bildirilir.
     * Boylece tek bir video icin once manifest.json'u indirmek gerekmez.
     *
     * @throws java.io.FileNotFoundException dosya sunucuda yoksa (HTTP 404).
     */
    fun downloadFile(
        name: String,
        expectedSize: Long?,
        partFile: File,
        onTotal: (Long) -> Unit,
        onBytes: (Long) -> Unit,
    ) {
        var existing = if (partFile.exists()) partFile.length() else 0L
        if (expectedSize != null && existing >= expectedSize) {
            // Beklenenden buyuk/esit kismi dosya: guvenmek yerine bastan indir.
            partFile.delete()
            existing = 0
        }

        val connection = open(name)
        val total: Long
        try {
            if (existing > 0) connection.setRequestProperty("Range", "bytes=$existing-")

            val append = when (connection.responseCode) {
                HttpURLConnection.HTTP_PARTIAL -> true
                HttpURLConnection.HTTP_OK -> false // sunucu Range desteklemiyor: bastan yaz
                HttpURLConnection.HTTP_NOT_FOUND -> throw java.io.FileNotFoundException(name)
                // Kismi dosya zaten tam ise sunucu 416 doner: bastan indir.
                416 -> {
                    partFile.delete()
                    connection.disconnect()
                    return downloadFile(name, expectedSize, partFile, onTotal, onBytes)
                }
                else -> throw IOException("$name: HTTP ${connection.responseCode}")
            }
            total = expectedSize ?: serverTotal(connection, append, existing)
                ?: throw IOException("$name: sunucu dosya boyutunu bildirmedi")
            onTotal(total)
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
        if (actual != total) {
            throw IOException("$name: boyut uyusmuyor ($actual != $total)")
        }
    }

    /** Dosyanin toplam boyutu: 206'da "Content-Range: bytes a-b/TOPLAM", 200'de Content-Length. */
    private fun serverTotal(connection: HttpURLConnection, partial: Boolean, existing: Long): Long? {
        if (partial) {
            connection.getHeaderField("Content-Range")
                ?.substringAfterLast('/')?.toLongOrNull()?.let { return it }
            return connection.contentLengthLong.takeIf { it >= 0 }?.plus(existing)
        }
        return connection.contentLengthLong.takeIf { it >= 0 }
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
