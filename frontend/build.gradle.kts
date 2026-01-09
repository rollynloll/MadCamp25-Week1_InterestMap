// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Hilt Gradle plugin loads JavaPoet on the buildscript/plugin classpath.
// If an older JavaPoet is pulled in, it can crash with:
// NoSuchMethodError: com.squareup.javapoet.ClassName.canonicalName()
buildscript {
    configurations.classpath {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
}