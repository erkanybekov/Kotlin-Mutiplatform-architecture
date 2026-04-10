import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseAppDistribution)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

fun env(name: String): String? = providers.environmentVariable(name).orNull?.trim()?.takeIf { it.isNotEmpty() }

val firebaseArtifactType = env("FIREBASE_APP_DIST_ARTIFACT_TYPE") ?: "APK"
val firebaseTesters = env("FIREBASE_APP_DIST_TESTERS")
val firebaseGroups = env("FIREBASE_APP_DIST_GROUPS")
val firebaseReleaseNotes = env("FIREBASE_APP_DIST_RELEASE_NOTES")
val firebaseReleaseNotesFile = env("FIREBASE_APP_DIST_RELEASE_NOTES_FILE")
val firebaseCredentialsPath = env("GOOGLE_APPLICATION_CREDENTIALS")
val firebaseAppId = env("FIREBASE_APP_ID")

val releaseKeystorePath = env("ANDROID_RELEASE_KEYSTORE_PATH")
val releaseKeystorePassword = env("ANDROID_RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias = env("ANDROID_RELEASE_KEY_ALIAS")
val releaseKeyPassword = env("ANDROID_RELEASE_KEY_PASSWORD")

android {
    namespace = "com.erkan.experimentkmp.android"
    compileSdk = 36

    signingConfigs {
        if (
            releaseKeystorePath != null &&
            releaseKeystorePassword != null &&
            releaseKeyAlias != null &&
            releaseKeyPassword != null
        ) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.erkan.experimentkmp.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            firebaseAppDistribution {
                artifactType = firebaseArtifactType
                firebaseTesters?.let { testers = it }
                firebaseGroups?.let { groups = it }
                firebaseReleaseNotesFile?.let { releaseNotesFile = it }
                if (firebaseReleaseNotesFile == null) {
                    firebaseReleaseNotes?.let { releaseNotes = it }
                }
                firebaseCredentialsPath?.let { serviceCredentialsFile = it }
                firebaseAppId?.let { appId = it }
            }
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfigs.findByName("release")?.let { signingConfig = it }
            firebaseAppDistribution {
                artifactType = firebaseArtifactType
                firebaseTesters?.let { testers = it }
                firebaseGroups?.let { groups = it }
                firebaseReleaseNotesFile?.let { releaseNotesFile = it }
                if (firebaseReleaseNotesFile == null) {
                    firebaseReleaseNotes?.let { releaseNotes = it }
                }
                firebaseCredentialsPath?.let { serviceCredentialsFile = it }
                firebaseAppId?.let { appId = it }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    // Shared Module
    implementation(projects.shared)
}
