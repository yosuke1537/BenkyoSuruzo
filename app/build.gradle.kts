plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "jp.benkyosuruzo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "jp.benkyosuruzo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }
    testOptions {
        // Allow android.util.Log (called by FilteringHandler) in plain JVM unit tests.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)

    // Settings screen (AndroidX Preference).
    implementation(libs.androidx.preference.ktx)

    // ML Kit: Chinese is bundled (offline-capable); other languages are unbundled via Play Services.
    implementation(libs.mlkit.text.recognition.chinese)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.play.services.mlkit.text.recognition.japanese)
    implementation(libs.play.services.mlkit.text.recognition.korean)

    testImplementation(libs.junit)
}
