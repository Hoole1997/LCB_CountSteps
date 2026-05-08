import java.util.Properties

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
}
val buildConfigFile = file("build.config.properties")
val buildConfig = Properties()
if (buildConfigFile.exists()) {
    buildConfig.load(buildConfigFile.inputStream())
}

fun githubCredential(propertyName: String, vararg envNames: String): String? {
    return buildConfig.getProperty(propertyName)?.trim()?.takeIf { it.isNotEmpty() }
        ?: envNames.asSequence()
            .mapNotNull { name -> System.getenv(name)?.trim()?.takeIf { it.isNotEmpty() } }
            .firstOrNull()
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://artifact.bytedance.com/repository/pangle/")
        maven("https://repo.itextsupport.com/android")
        maven("https://repo.dgtverse.cn/repository/maven-public/")
        maven("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven("https://android-sdk.is.com/")
        maven("https://jfrog.anythinktech.com/artifactory/overseas_sdk")
        maven("https://artifacts.applovin.com/android")
        maven("https://repo.dgtverse.cn/repository/maven-public")
        maven {
            url = uri("https://maven.pkg.github.com/toukaRemax/remax_sdk")
            credentials {
                username = githubCredential("github.user", "GH_PACKAGES_USER", "GITHUB_ACTOR")
                password = githubCredential("github.token", "GH_PACKAGES_TOKEN", "GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "LCB_CountSteps"
include(":app")
include(":metrics")
