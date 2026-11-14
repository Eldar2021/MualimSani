# MualimSani — Claude notes

This is an Android app (Kotlin, Jetpack Compose, Hilt, Media3) that goes with the printed book "Муаллиму Саани". A user scans a QR code in the book and the matching lesson video plays. For setup, build commands and Play Store steps, see `README.md`. This file covers only what is not obvious from the code.

## Conventions

- Talk with the user in Turkish. Code comments are Turkish written without Turkish characters (for example `Gecici dosya`).
- Commit messages are Turkish in the form `type: aciklama`.
- `./gradlew` may be blocked by the sandbox. If so, run `bash ./gradlew assembleDebug`.
- Never commit secrets: `keystore.properties`, `key/` and `*.jks`. The README explains them.

## Videos (the part most likely to break)

- Videos are downloaded **from GitHub Releases, not Firebase**. The URL is `BuildConfig.VIDEOS_BASE_URL`, which points to tag `videos-v1`. Firebase was removed on purpose: there is no access to the old project, and Storage needs the paid Blaze plan. Do not add Firebase back.
- The flow: `FileDownloaderVM` reads `manifest.json` (`name` and `size`), then uses `VideoDownloader` (HttpURLConnection) to download each video. Downloads go to a `.part` file first, resume with HTTP Range, and are checked against the expected size. A file whose size matches the manifest is skipped.
- A file name is the number in the QR code. Lessons are `1–68.mp4` and suras are `101–116.mp4`. The source of truth is the `Lesson(id, title, fileNameExceptExt)` list in `HomeVM.kt`.
- To publish, run `scripts/publish-videos.sh <folder> [--dry-run] [--allow-missing]`. It reads the expected list from `HomeVM.kt`.
- The video files live in `resource/`, which is gitignored and about 260 MB. **Never commit them.**
  - `resource/videos/` holds the raw Telegram downloads, numbered 1–85.
  - `resource/videos-named/` holds the correctly renamed copies that were uploaded.
- Known gap: lessons **30, 52 and 56 have no video yet**. The app does not crash on them; the player just stays empty.

## Behaviour that looks like a bug but is not

- Tapping a lesson does nothing (only a toast) until its QR code has been scanned once. `AppPreference.SCANNED_ID` stores the scanned lessons. On a debug build you can unlock lessons for testing by editing `shared_prefs/MUALIM_APP_PREF.xml` through `adb shell run-as com.muallimsaniqr.flutter_app.debug`.
- Lessons 1 and 60 show the same alphabet video. This is intended: lesson 60 is a letter review.
