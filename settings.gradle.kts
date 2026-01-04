pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Define the version for the KSP plugin here
        // The version must be compatible with your Kotlin version (e.g., "1.9.20-1.0.14")
        id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
        // --- ENSURE THE COMPOSE PLUGIN ID AND VERSION ARE DEFINED HERE ---
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PublicServicesLocator"
include(":app")
 