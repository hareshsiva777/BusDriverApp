plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // KSP processes the Room database annotations.
    id("com.google.devtools.ksp") version "2.3.10"
}

android {
    namespace = "com.example.busdriverapp"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.busdriverapp"

        // Minimum Android version supported by the application.
        minSdk = 26

        // Android version used to test the application's behaviour.
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }

    buildFeatures {
        // Enables Jetpack Compose UI.
        compose = true
    }
}

dependencies {

    /*
     * Room database
     *
     * Room stores drivers, routes, trips,
     * and GPS location points locally.
     */
    val roomVersion = "2.8.4"

    implementation(
        "androidx.room:room-runtime:$roomVersion"
    )

    implementation(
        "androidx.room:room-ktx:$roomVersion"
    )

    ksp(
        "androidx.room:room-compiler:$roomVersion"
    )

    /*
     * Navigation support for Jetpack Compose screens.
     */
    implementation(
        "androidx.navigation:navigation-compose:2.9.8"
    )

    /*
     * ViewModel and lifecycle components.
     */
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.9.4"
    )

    implementation(
        "androidx.lifecycle:lifecycle-service:2.9.4"
    )

    /*
     * WorkManager performs persistent trip synchronization
     * when an internet connection becomes available.
     */
    implementation(
        "androidx.work:work-runtime-ktx:2.11.2"
    )

    /*
     * Google Play Services location library.
     *
     * Used by the foreground service to collect GPS points.
     */
    implementation(
        "com.google.android.gms:play-services-location:21.4.0"
    )

    /*
     * Jetpack Compose platform and UI dependencies.
     */
    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    /*
     * Unit testing dependencies.
     */
    testImplementation(
        libs.junit
    )

    /*
     * Android instrumented testing dependencies.
     */
    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    /*
     * Compose debugging and preview tools.
     */
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}