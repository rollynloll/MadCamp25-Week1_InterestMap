# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ZXing (Barcode Scanner) 라이브러리 규칙
-keep class com.google.zxing.** { *; }
-keep interface com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

-keep class com.journeyapps.barcodescanner.** { *; }
-keep interface com.journeyapps.barcodescanner.** { *; }
-dontwarn com.journeyapps.barcodescanner.**

# ZXing 리소스 및 네이티브 메서드 보호
-keepclassmembers class * {
    native <methods>;
}

# 네이티브 라이브러리 유지 (libpenguin.so 등)
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class * implements java.io.Serializable { *; }

# JNI 관련 클래스 보호
-keepclasseswithmembers class * {
    public <init>(long);
}
-keepclasseswithmembers class * {
    native <methods>;
}
-keep class * {
    static { *; }
}

# Firebase 관련 규칙 (deprecated 클래스 경고 무시)
-dontwarn com.google.firebase.iid.**
-keep class com.google.firebase.** { *; }

# Kotlin 관련 규칙
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 앱의 모델 클래스 보호 (Gson 직렬화에 필요)
-keep class com.example.madclass01.data.model.** { *; }
-keep class com.example.madclass01.data.dto.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# 리플렉션 사용하는 클래스들 보호
-keepattributes *Annotation*, Signature, Exception

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Service Loader 지원 (에러 로그에서 확인된 문제)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class * implements java.util.ServiceLoader { *; }
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# AndroidX
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }