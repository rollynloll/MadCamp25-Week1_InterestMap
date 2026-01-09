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
		// You must set the actual key in res/values/strings.xml
		runCatching {
			val key = getString(R.string.kakao_native_app_key)
			if (key.isNotBlank() && !key.startsWith("YOUR_")) {
				KakaoSdk.init(this, key)
			}
		}
	}
}
