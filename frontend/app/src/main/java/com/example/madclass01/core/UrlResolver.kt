package com.example.madclass01.core

object UrlResolver {
    fun resolve(url: String?): String? {
        if (url.isNullOrBlank()) {
            return url
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        val trimmedBase = ApiConfig.BASE_URL.trimEnd('/')
        val normalizedPath = if (url.startsWith("/")) url.drop(1) else url
        return "$trimmedBase/$normalizedPath"
    }
}
