package com.example.madclass01

import android.app.Application
import com.example.madclass01.R
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MadClass01Application : Application() {
	override fun onCreate() {
		super.onCreate()
		// Initialize Kakao SDK with native app key from resources
		try {
			val key = getString(R.string.kakao_native_app_key)
			if (key.isNotBlank() && !key.startsWith("YOUR_")) {
				KakaoSdk.init(this, key)
				android.util.Log.d("KakaoSDK", "Kakao SDK initialized successfully")
			} else {
				android.util.Log.e("KakaoSDK", "Invalid Kakao Native App Key! Please set a valid key in strings.xml")
				throw IllegalStateException("Kakao Native App Key is not set. Please add your key to strings.xml")
			}
		} catch (e: Exception) {
			android.util.Log.e("KakaoSDK", "Failed to initialize Kakao SDK", e)
			throw e
		}
	}
}
