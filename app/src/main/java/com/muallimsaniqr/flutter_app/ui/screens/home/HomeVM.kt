package com.muallimsaniqr.flutter_app.ui.screens.home

import android.content.Context
import com.muallimsaniqr.flutter_app.R
import com.muallimsaniqr.flutter_app.common.base.BaseVM
import com.muallimsaniqr.flutter_app.data.storage.AppPreference
import com.muallimsaniqr.flutter_app.domain.model.HeaderItem
import com.muallimsaniqr.flutter_app.domain.model.Language
import com.muallimsaniqr.flutter_app.domain.model.Lesson
import com.muallimsaniqr.flutter_app.domain.model.Lessons
import com.muallimsaniqr.flutter_app.domain.repository.MualimRepository
import com.muallimsaniqr.flutter_app.utils.language.LocaleUtils
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.muallimsaniqr.flutter_app.data.videos.VideoNotAvailableException
import com.muallimsaniqr.flutter_app.data.videos.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** Ana ekrandaki oynaticinin durumu. */
sealed interface VideoState {
    /** Henuz ders secilmedi. */
    data object Idle : VideoState

    /** Video cihazda yok, indiriliyor. [percent] 0..100; baglanti kurulurken null. */
    data class Downloading(val fileName: Int, val percent: Int?) : VideoState

    /** Video hazir, oynatilabilir. */
    data class Ready(val fileName: Int, val file: File) : VideoState

    /** Indirilemedi. [notAvailable] true ise video sunucuda hic yok (tekrar denemek ise yaramaz). */
    data class Failed(val fileName: Int, val notAvailable: Boolean) : VideoState
}

@HiltViewModel
class HomeVM @Inject constructor(
    private val repo: MualimRepository,
    private val localStorage: AppPreference,
    private val videos: VideoRepository,
): BaseVM() {

    private val _videoState = MutableStateFlow<VideoState>(VideoState.Idle)
    val videoState: StateFlow<VideoState> = _videoState.asStateFlow()

    private var videoJob: Job? = null

    /**
     * [fileName] dersinin videosunu acar: cihazda varsa hemen oynatilir, yoksa indirilir.
     * Baska bir ders secilirse onceki indirme iptal edilir; yarim kalan dosya bir sonraki
     * seferde kaldigi yerden devam eder.
     */
    fun openVideo(fileName: Int) {
        val current = _videoState.value
        val sameLesson = when (current) {
            is VideoState.Ready -> current.fileName == fileName
            is VideoState.Downloading -> current.fileName == fileName
            else -> false
        }
        if (sameLesson) return

        videoJob?.cancel()
        videos.localVideo(fileName)?.let {
            _videoState.value = VideoState.Ready(fileName, it)
            return
        }

        _videoState.value = VideoState.Downloading(fileName, null)
        videoJob = viewModelScope.launch {
            try {
                val file = videos.getVideo(fileName) { percent ->
                    _videoState.value = VideoState.Downloading(fileName, percent)
                }
                _videoState.value = VideoState.Ready(fileName, file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HomeVM", "Video acilamadi: $fileName", e)
                _videoState.value = VideoState.Failed(fileName, e is VideoNotAvailableException)
            }
        }
    }

    /** Hata sonrasi "tekrar dene". */
    fun retryVideo() {
        val failed = _videoState.value as? VideoState.Failed ?: return
        _videoState.value = VideoState.Idle
        openVideo(failed.fileName)
    }

    val isOnBoardingShown = repo.isOnboardingShown()

    val areVideosDownloaded = repo.getAreVideosDownloaded()

    fun setOnboardingShown(isShown: Boolean) {
        repo.setOnboardingShown(isShown)
    }


    fun checkBeforeScanned(id: Int): Boolean {
        val scannedIds = repo.getScannedIds()
        return id in scannedIds
    }

    fun setScannedId(rawId: Int) {
        val lessonId = getLessonIdByFileNameExceptExt(rawId)
        if (lessonId != 0) repo.setScannedId(lessonId)
    }

    val items = listOf<Lessons>(

        HeaderItem(R.string.arabic_letters),

        Lesson(1, R.string.lesson_1, 1),
        Lesson(2, R.string.lesson_2, 2),
        Lesson(3, R.string.lesson_3, 3),
        Lesson(4, R.string.lesson_4, 4),
        Lesson(5, R.string.lesson_5, 5),
        Lesson(6, R.string.lesson_6, 6),
        Lesson(7, R.string.lesson_7, 7),
        Lesson(8, R.string.lesson_8, 8),
        Lesson(9, R.string.lesson_9, 9),
        Lesson(10, R.string.lesson_10, 10),
        Lesson(11, R.string.lesson_11, 11),
        Lesson(12, R.string.lesson_12, 12),
        Lesson(13, R.string.lesson_13, 13),
        Lesson(14, R.string.lesson_14, 14),
        Lesson(15, R.string.lesson_15, 15),
        Lesson(16, R.string.lesson_16, 16),
        Lesson(17, R.string.lesson_17, 17),
        Lesson(18, R.string.lesson_18, 18),
        Lesson(19, R.string.lesson_19, 19),
        Lesson(20, R.string.lesson_20, 20),
        Lesson(21, R.string.lesson_21, 21),
        Lesson(22, R.string.lesson_22, 22),
        Lesson(23, R.string.lesson_23, 23),
        Lesson(24, R.string.lesson_24, 24),
        Lesson(25, R.string.lesson_25, 25),
        Lesson(26, R.string.lesson_26, 26),
        Lesson(27, R.string.lesson_27, 27),
        Lesson(28, R.string.lesson_28, 28),
        Lesson(29, R.string.lesson_29, 29),
        Lesson(30, R.string.lesson_30, 30),
        Lesson(31, R.string.lesson_31, 31),
        Lesson(32, R.string.lesson_32, 32),
        Lesson(33, R.string.lesson_33, 33),
        Lesson(34, R.string.lesson_34, 34),
        Lesson(35, R.string.lesson_35, 35),
        Lesson(36, R.string.lesson_36, 36),
        Lesson(37, R.string.lesson_37, 37),
        Lesson(38, R.string.lesson_38, 38),
        Lesson(39, R.string.lesson_39, 39),
        Lesson(40, R.string.lesson_40, 40),
        Lesson(41, R.string.lesson_41, 41),
        Lesson(42, R.string.lesson_42, 42),
        Lesson(43, R.string.lesson_43, 43),
        Lesson(44, R.string.lesson_44, 44),
        Lesson(45, R.string.lesson_45, 45),
        Lesson(46, R.string.lesson_46, 46),
        Lesson(47, R.string.lesson_47, 47),
        Lesson(48, R.string.lesson_48, 48),
        Lesson(49, R.string.lesson_49, 49),
        Lesson(50, R.string.lesson_50, 50),
        Lesson(51, R.string.lesson_51, 51),
        Lesson(52, R.string.lesson_52, 52),
        Lesson(53, R.string.lesson_53, 53),
        Lesson(54, R.string.lesson_54, 54),
        Lesson(55, R.string.lesson_55, 55),
        Lesson(56, R.string.lesson_56, 56),
        Lesson(57, R.string.lesson_57, 57),
        Lesson(58, R.string.lesson_58, 58),
        Lesson(59, R.string.lesson_59, 59),
        Lesson(60, R.string.lesson_60, 60),
        Lesson(61, R.string.lesson_61, 61),
        Lesson(62, R.string.lesson_62, 62),
        Lesson(63, R.string.lesson_63, 63),
        Lesson(64, R.string.lesson_64, 64),
        Lesson(65, R.string.lesson_65, 65),
        Lesson(66, R.string.lesson_66, 66),
        Lesson(67, R.string.lesson_67, 67),
        Lesson(68, R.string.lesson_68, 68),

        HeaderItem(R.string.suras),

        // Fatiha'nin kitaptaki QR numarasi 117'dir; listede ise geleneksel olarak ilk siradadir.
        Lesson(117, R.string.suro_fatiha, 117),
        Lesson(101, R.string.suro_aadiyat, 101),
        Lesson(102, R.string.suro_zilzala, 102),
        Lesson(103, R.string.suro_takasur, 103),
        Lesson(104, R.string.suro_kooriya, 104),
        Lesson(105, R.string.suro_fil, 105),
        Lesson(106, R.string.suro_humaza, 106),
        Lesson(107, R.string.suro_asr, 107),
        Lesson(108, R.string.suro_kausar, 108),
        Lesson(109, R.string.suro_maaun, 109),
        Lesson(110, R.string.suro_kuraish, 110),
        Lesson(111, R.string.suro_masad, 111),
        Lesson(112, R.string.suro_nasr, 112),
        Lesson(113, R.string.suro_kaafirun, 113),
        Lesson(114, R.string.suro_nas, 114),
        Lesson(115, R.string.suro_falak, 115),
        Lesson(116, R.string.suro_ihlas, 116),
    )


    fun getFileNameExceptExtByScannerResult(scannerResult: String): Int {
        // Kitaptaki QR kodlarinda sadece ders numarasi vardir (ornegin "5" veya "101").
        val lesson = items.filterIsInstance<Lesson>().find { it.id == scannerResult.trim().toIntOrNull() }
        return lesson?.fileNameExceptExt ?: 0
    }

    fun getLessonIdByFileNameExceptExt(fileNameExceptExt: Int): Int {
        val lesson = items.filterIsInstance<Lesson>().find { it.fileNameExceptExt == fileNameExceptExt }
        return lesson?.id ?: 0
    }

    fun getLessonIndexByFileName(fileName: Int): Int {
        return items.filterIsInstance<Lesson>().indexOfFirst { it.fileNameExceptExt == fileName }
    }

    fun setLanguage(language: Language) {
        localStorage.setLanguage(language)
    }

    fun getLanguage(): Language {
        return localStorage.getLanguage()
    }

}