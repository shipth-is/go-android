plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("LogInterceptorPlugin")
}

android {
    namespace = "com.shipthis.go"
    compileSdk = 35

    dynamicFeatures += setOf(
        ":godot_v4_5",
        ":godot_v3_x"
    )

    packaging {
        jniLibs {
            pickFirsts += "**/libc++_shared.so"
            excludes += listOf(
                // TODO: we want to remove this exclusion
                "**/armeabi-v7a/**",
                "**/x86/**",
                "**/x86_64/**"
            )
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    defaultConfig {
        applicationId = "com.shipthis.go"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val shipthisDomain = providers
            .environmentVariable("SHIPTHIS_DOMAIN")
            .orElse("shipth.is")
            .get()

        buildConfigField("String", "SHIPTHIS_DOMAIN", "\"$shipthisDomain\"")

         ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore.jks")
            storePassword = "46ef0d44-7038-412f-a4b8-1a4d9da9e122"
            keyAlias = "855ca27c-4452-4f5c-9a26-7d7696ac568f"
            keyPassword = "46ef0d44-7038-412f-a4b8-1a4d9da9e122"
        }
    }

    buildTypes {
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {

    // QR Code Scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // ---- Godot Dependencies ----
    // Imported here so we get the most recent version of libc++_shared.so
    // the dynamic feature modules exclude libc++_shared.so
    val localAarPath = file("libs/godot-lib.template_debug.aar")
    if (localAarPath.exists()) {
        implementation(files(localAarPath))
    } else {
        implementation("shipth.is:godot-lib-v4-5:0.0.30:template-debug@aar")
    }
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ---- Compose (M3) ----
    // Use BOM to keep Compose libs aligned
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.2")

    // Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ---- XML Material theme support (for Theme.Material3.DayNight.NoActionBar) ----
    implementation("com.google.android.material:material:1.12.0")

    // Navigation & Lifecycle
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.play:core:1.10.3") // Needed for Play Feature Delivery

    // Hilt
    implementation("com.google.dagger:hilt-android:2.49")
    ksp("com.google.dagger:hilt-compiler:2.49")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Coroutines (align with Kotlin 2.1.0)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
