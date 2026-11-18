package com.muallimsaniqr.flutter_app.data.videos

import android.content.Context
import android.util.Log
import com.muallimsaniqr.flutter_app.BuildConfig
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.ui.extensions.getDownloadedVideoFolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ders videolarinin tek kaynagi: once cihazdaki dosyaya bakar, yoksa sunucudan indirir.
 *
 * Hem ana ekrandaki "tek ders" indirmesi hem de "tumunu indir" ekrani bunu kullanir.
 * Ayni dosya ayni anda iki yerden istenirse ([fileLocks]) ikinci istek birincinin
 * bitmesini bekler; dosya iki kez indirilmez.
 */
@Singleton
class VideoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localStorage: AppPreference,
) {

    private val downloader = VideoDownloader(BuildConfig.VIDEOS_BASE_URL)
    private val manifestLock = Mutex()
    private val fileLocks = ConcurrentHashMap<String, Mutex>()
    private var cachedManifest: VideoManifest? = null

    val videoFolder: File get() = context.getDownloadedVideoFolder()

    /** Video cihazda tam olarak varsa dosyasini, yoksa null dondurur. */
    fun localVideo(fileNameExceptExt: Int): File? =
        File(videoFolder, "$fileNameExceptExt.mp4").takeIf { it.exists() && it.length() > 0 }

    /** Sunucudaki video listesi. Uygulama acik kaldigi surece bir kez indirilir. */
    suspend fun manifest(): VideoManifest = manifestLock.withLock {
        cachedManifest ?: withContext(Dispatchers.IO) {
            withRetry("manifest.json") { downloader.fetchManifest() }
        }.also { manifest ->
            check(manifest.videos.isNotEmpty()) { "manifest.json bos" }
            manifest.videos.forEach {
                require(SAFE_NAME.matches(it.name)) { "Gecersiz dosya adi: ${it.name}" }
            }
            cachedManifest = manifest
        }
    }

    /**
     * Tek bir dersin videosunu getirir: cihazda varsa hemen, yoksa indirerek.
     * [onProgress] 0..100 arasi yuzdeyle cagrilir; boyut henuz bilinmiyorsa (baglanti
     * kuruluyor) cagrilmaz. Manifest gerekmez: boyut sunucunun yanitindan alinir.
     *
     * @throws VideoNotAvailableException video sunucuda yoksa (henuz yuklenmemis ders).
     */
    suspend fun getVideo(fileNameExceptExt: Int, onProgress: (Int) -> Unit = {}): File {
        localVideo(fileNameExceptExt)?.let { return it }

        val name = "$fileNameExceptExt.mp4"
        var total = 0L
        var done = 0L
        try {
            fetch(name, expectedSize = null, onTotal = { total = it }) { bytes ->
                done += bytes
                if (total > 0) onProgress((done * 100 / total).toInt().coerceIn(0, 100))
            }
        } catch (e: FileNotFoundException) {
            throw VideoNotAvailableException(name)
        }
        return File(videoFolder, name)
    }

    /**
     * [video] cihazda dogru boyutta degilse indirir ("tumunu indir" icin). [onBytes]
     * ilerlemeyi bayt olarak bildirir (basarisiz denemede negatif deger gelebilir).
     */
    suspend fun download(video: RemoteVideo, onBytes: (Long) -> Unit) =
        fetch(video.name, video.size, onTotal = {}, onBytes = onBytes)

    /**
     * Ortak indirme: `.part` dosyasina indirir, baglanti koparsa kaldigi yerden devam eder,
     * bitince asil dosyaya tasir. [expectedSize] null ise boyut sunucudan ogrenilir.
     */
    private suspend fun fetch(
        name: String,
        expectedSize: Long?,
        onTotal: (Long) -> Unit,
        onBytes: (Long) -> Unit,
    ) = withContext(Dispatchers.IO) {
        fileLocks.getOrPut(name) { Mutex() }.withLock {
            val folder = videoFolder
            val target = File(folder, name)

            // Boyut biliniyorsa: hem silinmis hem de sunucuda guncellenmis videolar yeniden iner.
            // Bilinmiyorsa: bu sirada baska bir istek dosyayi indirmis olabilir.
            val alreadyThere = target.exists() &&
                    (if (expectedSize != null) target.length() == expectedSize else target.length() > 0)
            if (alreadyThere) {
                onTotal(target.length())
                onBytes(target.length())
                return@withLock
            }

            val partFile = File(folder, "$name.part")
            var counted = 0L
            withRetry(name) {
                // Basarisiz denemede sayilan baytlari geri al; resume, mevcut kismi tekrar sayar.
                onBytes(-counted)
                counted = 0
                downloader.downloadFile(name, expectedSize, partFile, onTotal) { bytes ->
                    counted += bytes
                    onBytes(bytes)
                }
            }

            target.delete()
            check(partFile.renameTo(target)) { "Gecici dosya tasinamadi: $name" }
            localStorage.setAlreadyDownloadedVideo(name)
        }
    }

    private suspend fun <T> withRetry(label: String, block: () -> T): T {
        var lastError: IOException? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            currentCoroutineContext().ensureActive()
            try {
                return block()
            } catch (e: FileNotFoundException) {
                throw e // dosya sunucuda yok: tekrar denemek ise yaramaz
            } catch (e: IOException) {
                lastError = e
                Log.w(TAG, "$label: deneme ${attempt + 1}/$MAX_ATTEMPTS basarisiz", e)
                if (attempt < MAX_ATTEMPTS - 1) delay(RETRY_DELAY_MS * (attempt + 1))
            }
        }
        throw lastError!!
    }

    private companion object {
        const val TAG = "VideoRepository"
        const val MAX_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 2_000L
        val SAFE_NAME = Regex("[A-Za-z0-9._-]+")
    }
}

/** Istenen video sunucuda yok (ornegin ders videosu henuz yuklenmemis). */
class VideoNotAvailableException(name: String) : Exception("Sunucuda yok: $name")
