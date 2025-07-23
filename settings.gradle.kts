// Settings for O3DE Rider Plugin
// This file configures the Gradle build environment and optimizations

rootProject.name = "o3de-rider-plugin"

// Enable version catalogs
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Dependency resolution management
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://www.jetbrains.com/intellij-repository/snapshots")
    }
}

// Plugin management
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
}

// Build cache configuration
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
    
    // Remote cache can be configured for CI/CD environments
    // remote<HttpBuildCache> {
    //     url = uri("https://your-build-cache-server.com/cache/")
    //     isPush = System.getenv("CI") != null
    // }
}

// Gradle Enterprise configuration (optional)
// plugins {
//     id("com.gradle.enterprise") version "3.16.1"
// }
// 
// gradleEnterprise {
//     buildScan {
//         termsOfServiceUrl = "https://gradle.com/terms-of-service"
//         termsOfServiceAgree = "yes"
//         publishAlways()
//     }
// }