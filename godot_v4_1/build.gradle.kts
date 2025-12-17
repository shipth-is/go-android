plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
}

android {
    namespace = "com.shipthis.go"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    packaging {
        // TODO: include the most recent ver in the parent?
        jniLibs {
            excludes += "**/libc++_shared.so"
        }
    }
}

dependencies {
    implementation(project(":app"))
    implementation("androidx.fragment:fragment:1.8.5")
    val godotVersion = "4.1"
    val godotVersionDash = godotVersion.replace('.', '-')
    val buildType = "debug"

    val localAarPath =
        file("../app/libs/godot-lib-v$godotVersion.template_$buildType.aar")

    if (localAarPath.exists()) {
        implementation(files(localAarPath))
    } else {
        implementation("shipth.is:godot-lib-v$godotVersionDash:+:template-$buildType@aar")
    }
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.play:core:1.10.3")
}
