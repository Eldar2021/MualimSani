# Muallim Sani 2

QR kod ile Kuran okumayı öğreten Android uygulaması. Kullanıcı "Муаллиму Саани"
kitabındaki QR kodu tarar, ilgili ders videosu uygulamada açılır. Videolar ilk
açılışta Firebase Storage'dan indirilip cihazda saklanır, sonrasında internetsiz
izlenebilir.

- **Paket adı:** `com.muallimsaniqr.flutter_app`
- **Teknolojiler:** Kotlin, Jetpack Compose, Hilt, Media3 (ExoPlayer), CameraX,
  ML Kit (barkod), Firebase Storage
- **Desteklenen diller:** Kırgızca, Rusça, Özbekçe, İngilizce
- **minSdk 26** (Android 8.0) / **targetSdk 36** (Android 16)

---

## 1. Geliştirme ortamı

| Gereksinim | Sürüm |
|---|---|
| Android Studio | Güncel sürüm |
| JDK | 17 |
| Android SDK Platform | 37 (`compileSdk`) |
| Android SDK Build-Tools | 37.0.0 |
| Gradle | 9.7.1 (wrapper ile otomatik iner) |
| Android Gradle Plugin | 9.3.2 |
| Kotlin | 2.3.21 (AGP içinde gelir) |

Android Studio'da **Tools → SDK Manager** üzerinden "Android SDK Platform 37" ve
"Android SDK Build-Tools 37" kurulu olmalıdır.

---

## 2. Repoda BULUNMAYAN dosyalar

Aşağıdaki dosyalar güvenlik nedeniyle `.gitignore` ile hariç tutulmuştur.
Projeyi derlemek için bunları elle eklemeniz gerekir:

### `app/google-services.json`
Firebase istemci yapılandırması. Firebase Console → Proje ayarları →
"Uygulamalarınız" bölümünden indirilir ve `app/` klasörüne konur.
Bu dosya olmadan proje **derlenmez**.

### `keystore.properties` (proje kök dizininde)
Release imzalama bilgileri. Şu formatta oluşturun:

```properties
storeFile=key/key.jks
storePassword=********
keyAlias=key
keyPassword=********
```

### `key/` klasörü
İmzalama anahtarı (`key.jks`). **Bu dosya kaybolursa Play Store'daki uygulamanın
güncellemesi bir daha yayınlanamaz.** Yedeğini güvenli bir yerde (şifre yöneticisi,
şifreli disk) saklayın.

> `keystore.properties` yoksa proje yine derlenir, ancak release çıktısı imzasız olur.

---

## 3. Derleme

Terminalde proje kök dizininde:

```bash
# Test için APK (telefona doğrudan kurulabilir)
./gradlew assembleRelease
# -> app/build/outputs/apk/release/app-release.apk

# Google Play'e yüklemek için AAB
./gradlew bundleRelease
# -> app/build/outputs/bundle/release/app-release.aab

# Geliştirme sırasında hata ayıklama sürümü
./gradlew assembleDebug
```

Debug sürümü ayrı paket adıyla (`...flutter_app.debug`) kurulur, yani release
sürümüyle aynı telefonda yan yana durabilir.

### Sürüm numarasını yükseltme
Her Play Store yüklemesinde `app/build.gradle.kts` içindeki `versionCode`
bir artırılmalı, `versionName` ise kullanıcıya görünen sürümdür:

```kotlin
versionCode = 7        // her yüklemede +1
versionName = "1.3.0"  // görünen sürüm
```

---

## 4. Firebase

Videolar `Firebase Storage → videos/` klasöründen indirilir. Dosya adları ders
numarasıyla eşleşir: `1.mp4`, `2.mp4`, ... `101.mp4` (sureler).

Klasör adı `app/build.gradle.kts` içinde tanımlıdır:

```kotlin
buildConfigField("String", "FIREBASE_VIDEOS_FOLDER", "\"videos/\"")
```

> **Önemli:** Google, Eylül 2024 duyurusuyla Cloud Storage for Firebase'i ücretsiz
> (Spark) planda kullanımdan kaldırdı. Proje Spark planındaysa video indirme
> isteklerine **HTTP 402** döner ve uygulama videoları indiremez. Videoların
> çalışması için Firebase projesinin **Blaze (kullandıkça öde)** planında olması
> gerekir.

Storage kuralları, videoların herkes tarafından okunabilmesine izin vermelidir:

```
service firebase.storage {
  match /b/{bucket}/o {
    match /videos/{file} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

---

## 5. Proje yapısı

```
app/src/main/java/com/muallimsaniqr/flutter_app/
├── App.kt                     Uygulama sınıfı (Hilt + Firebase başlatma)
├── MainActivity.kt            Tek activity; açılış ekranını ve dili belirler
├── common/base/               Ortak temel sınıflar
├── data/
│   ├── repository/            Repository uygulaması
│   └── storage/               SharedPreferences sarmalayıcı
├── di/                        Hilt modülleri
├── domain/                    Model ve repository arayüzleri
├── ui/
│   ├── navigation/            Ekran geçişleri (NavHost)
│   ├── screens/
│   │   ├── onboarding/        Kitap tanıtımı / iletişim ekranı
│   │   ├── downloading/       Video indirme ekranı
│   │   ├── home/              Ders listesi + video oynatıcı
│   │   └── scanner/           QR tarayıcı (ayrı activity)
│   ├── theme/                 Renkler, tipografi
│   └── widgets/               Yeniden kullanılan bileşenler
└── utils/                     Dil ve barkod yardımcıları
```

---

## 6. Play Store'a yükleme

1. `versionCode` değerini artırın.
2. `./gradlew bundleRelease` ile AAB üretin.
3. [Play Console](https://play.google.com/console) → Uygulama → Üretim →
   Yeni sürüm oluştur → AAB dosyasını yükleyin.
4. Sürüm notlarını yazıp incelemeye gönderin.

İmzalama anahtarının Play Console'da kayıtlı yükleme anahtarıyla aynı olması
gerekir. Mevcut anahtarın parmak izi:

```
SHA-1: 36:0D:C2:8D:F1:E4:25:1D:22:1D:DF:8F:41:BA:2D:41:E3:A3:E1:84
```
