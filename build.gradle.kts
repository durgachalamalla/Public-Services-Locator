// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Google services Gradle plugin (for Firebase)
    id("com.google.gms.google-services") version "4.4.2" apply false

    // KSP for Room - Updated to use the Version Catalog alias
    // This ensures it uses version 2.0.20-1.0.24 to match your Kotlin 2.0.20
    alias(libs.plugins.ksp) apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}