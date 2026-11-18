# ---------------------------------------------------------------------------
# MualimSani — R8 / ProGuard kurallari
# Release build'de kod kucultme (minify) ve kaynak temizleme acik.
# ---------------------------------------------------------------------------

# Kilitlenme raporlarinin okunabilir olmasi icin satir numaralarini koru.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn kotlin.**

# --- ML Kit (barkod okuma) ---
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# --- Media3 / ExoPlayer ---
# Media3 kendi consumer kurallarini saglar; ek olarak opsiyonel
# eklentiler icin uyarilari susturuyoruz.
-dontwarn androidx.media3.**

# --- Hilt / Dagger ---
# Hilt kendi consumer kurallarini saglar, ek kurala gerek yok.

# --- Uygulama modelleri ---
-keep class com.muallimsaniqr.flutter_app.domain.model.** { *; }
