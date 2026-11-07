import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// local.properties is never committed: it holds the SDK path, the optional Google key and the
// signing key settings for this PC.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.bunyu.dich"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bunyu.dich"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"

        // Optional Google Cloud Translation key, read from local.properties (never committed):
        //   google.translate.apiKey=AIza...
        // When set, the APK uses Google online translation without typing the key on the phone.
        val googleKey = localProps.getProperty("google.translate.apiKey").orEmpty().trim()
        buildConfigField("String", "GOOGLE_API_KEY", "\"$googleKey\"")
    }

    // One fixed signing key, so a new APK installs over the old one without uninstalling.
    // The keystore lives outside the repo; its path and passwords go in local.properties:
    //   dich.storeFile=D:/work/dich-keys/dich-release.jks
    //   dich.storePassword=...
    //   dich.keyAlias=dich
    //   dich.keyPassword=...
    val storePath = localProps.getProperty("dich.storeFile")?.trim().orEmpty()
    val hasReleaseKey = storePath.isNotEmpty() && file(storePath).exists()
    signingConfigs {
        if (hasReleaseKey) {
            create("dich") {
                storeFile = file(storePath)
                storePassword = localProps.getProperty("dich.storePassword")
                keyAlias = localProps.getProperty("dich.keyAlias")
                keyPassword = localProps.getProperty("dich.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Without the key (e.g. on GitHub Actions) release falls back to the debug key.
            signingConfig = if (hasReleaseKey) signingConfigs.getByName("dich") else signingConfigs.getByName("debug")
            // Her Redmi Note 14 is 64-bit ARM: leave out other CPU types to shrink the APK a lot.
            ndk { abiFilters += "arm64-v8a" }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    val camerax = "1.4.1"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")

    // On-device ML Kit: OCR (bundled model, works offline), translation, language detection.
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:language-id:17.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
