# MualimSani — Claude notes

This is an Android app (Kotlin, Jetpack Compose, Hilt, Media3) that goes with the printed book "Муаллиму Саани". A user scans a QR code in the book and the matching lesson video plays. For setup, build commands and Play Store steps, see `README.md`. This file covers only what is not obvious from the code.

## Conventions

- Talk with the user in Turkish. Code comments are Turkish written without Turkish characters (for example `Gecici dosya`).
- Commit messages are Turkish in the form `type: aciklama`.
- `./gradlew` may be blocked by the sandbox. If so, run `bash ./gradlew assembleDebug`.
- Never commit secrets: `keystore.properties`, `key/` and `*.jks`. The README explains them.

## Videos (the part most likely to break)

- Videos are downloaded **from GitHub Releases, not Firebase**. The URL is `BuildConfig.VIDEOS_BASE_URL`, which points to tag `videos-v1`. Firebase was removed on purpose: there is no access to the old project, and Storage needs the paid Blaze plan. Do not add Firebase back.
- **On-demand flow (default):** there is no bulk download at startup. When a lesson is opened (QR scan or tap), `HomeVM.openVideo` calls `VideoRepository.getVideo`. A local file plays immediately; otherwise it is downloaded with a progress UI in the player area, then plays. This path does not use the manifest: the size comes from `Content-Length` / `Content-Range`. A 404 becomes `VideoNotAvailableException`, and no retry button is shown for it.
- **Download all (side drawer):** `FileDownloaderVM` reads `manifest.json` (`name` and `size`) and downloads every video, skipping files that already have the right size.
- **Both paths** share `VideoRepository.fetch`. It downloads to a `.part` file, resumes with HTTP Range, retries 3 times, and uses a per-file mutex so the same file is never downloaded twice in parallel.
- **Known limitation:** If a video is replaced on the server under the same name, phones that already have it keep the old copy. Only "download all" notices the size change. To ship a new version, prefer a new tag or file name.
- QR codes in the book contain only the lesson number (for example `5` or `101`); `HomeVM.getFileNameExceptExtByScannerResult` matches it to `Lesson.id`. A file name is the number in the QR code. Lessons are `1–68.mp4` and suras are `101–117.mp4`. `117` is Fatiha; it is listed first among the suras but keeps QR number 117. The source of truth is the `Lesson(id, title, fileNameExceptExt)` list in `HomeVM.kt`.
- To publish, run `scripts/publish-videos.sh <folder> [--dry-run] [--allow-missing]`. It reads the expected list from `HomeVM.kt`.
- The video files live in `resource/`, which is gitignored and about 260 MB. **Never commit them.**
  - `resource/videos-named/` holds all 85 correctly named videos, the same set that is on the release.
- If a video is missing, the app does not crash; the player just stays empty. Since 2026-11-15 all 85 videos are published.

## Testing on the emulator
- The emulator's `adb emu network speed edge/umts` profiles stall TLS connections to GitHub; downloads time out there even though they work at full speed. Do not treat that as an app bug without checking on a real device.

## Behaviour that looks like a bug but is not

- Tapping a lesson does nothing (only a toast) until its QR code has been scanned once. `AppPreference.SCANNED_ID` stores the scanned lessons. On a debug build you can unlock lessons for testing by editing `shared_prefs/MUALIM_APP_PREF.xml` through `adb shell run-as com.muallimsaniqr.flutter_app.debug`.
- Lessons 1 and 60 show the same alphabet video. This is intended: lesson 60 is a letter review.
