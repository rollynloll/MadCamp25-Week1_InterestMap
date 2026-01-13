plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
}

// Ensure Gradle plugin classpath (Hilt tasks) resolves a compatible JavaPoet.
buildscript {
    configurations.classpath {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}

android {
    namespace = "com.example.madclass01"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.madclass01"
        minSdk = 32
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 네이티브 라이브러리 ABI 필터 설정
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            // 네이티브 라이브러리 디버깅 심볼 제거 방지
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi"
        )
    }
    buildFeatures {
        compose = true
    }
    
    packaging {
        resources {
            // 중복 파일 제외
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // 네이티브 라이브러리 병합
            pickFirsts += "lib/*/libpenguin.so"
            pickFirsts += "lib/*/libc++_shared.so"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material:1.7.6")
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // FlowRow for layout
    implementation("com.google.accompanist:accompanist-flowlayout:0.36.0")
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Activity Compose for file picker
    implementation("androidx.activity:activity-compose:1.8.0")

    // Kakao SDK (Login/User)
    implementation("com.kakao.sdk:v2-user:2.20.0")
    
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // QR Code Generation
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}
configurations.all {
    resolutionStrategy {
        // Hilt 에러 해결을 위해 javapoet 버전을 1.13.0으로 고정
        force("com.squareup:javapoet:1.13.0")
    }
}
