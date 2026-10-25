plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android") version "2.51" apply true
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.photofieldapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.photofieldapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-P", "room.schemaLocation=app/schemas",
            "-P", "hilt.incremental=true"
        )
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core Android
    val core_ktx_version = "1.13.1"
    val activity_version = "1.9.2"
    val fragment_version = "1.8.2"
    val lifecycle_version = "2.8.2"
    val room_version = "2.6.1"
    val hilt_version = "2.51"
    val navigation_version = "2.7.7"
    val datastore_version = "1.1.1"

    // Compose
    val compose_bom_version = "2024.06.00"
    val compose_ui_version = "1.6.7"
    val material3_version = "1.2.1"
    val accompanist_version = "0.32.0"

    // CameraX
    val camerax_version = "1.4.0-beta02"

    // Location
    val location_version = "21.2.0"

    // Maps (osmdroid - OpenStreetMap, no API key needed)
    val osmdroid_version = "6.1.16"
    val slf4j_version = "1.7.36"

    // Image loading
    val coil_version = "2.6.0"

    // Coroutines & Flow
    val coroutines_version = "1.8.1"

    // Serialization
    val serialization_version = "1.6.3"

    // Testing
    val junit_version = "4.13.2"
    val espresso_version = "3.5.1"
    val compose_test_version = "1.6.7"

    implementation(platform("androidx.compose:compose-bom:$compose_bom_version"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.compose.material3:material3-window-size-class:$material3_version")
    implementation("androidx.activity:activity-compose:$activity_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")
    implementation("androidx.navigation:navigation-compose:$navigation_version")
    implementation("androidx.datastore:datastore-preferences:$datastore_version")

    // Accompanist (permissions, system UI controller, etc)
    implementation("com.google.accompanist:accompanist-permissions:$accompanist_version")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist_version")
    implementation("com.google.accompanist:accompanist-drawablepainter:$accompanist_version")

    // CameraX
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-video:$camerax_version")
    implementation("androidx.camera:camera-mlkit-vision:$camerax_version")

    // Location
    implementation("com.google.android.gms:play-services-location:$location_version")

    // Maps (osmdroid - OpenStreetMap)
    implementation("org.osmdroid:osmdroid-android:$osmdroid_version")
    implementation("org.osmdroid:osmdroid-wms:$osmdroid_version")
    implementation("org.osmdroid:osmdroid-mapsforge:$osmdroid_version")
    implementation("org.slf4j:slf4j-android:$slf4j_version")

    // Room
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Hilt
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.1.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:$coil_version")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")

    // PDF Generation - using Android's built-in PdfDocument (no external dependency needed)

    // Core KTX
    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.fragment:fragment-ktx:$fragment_version")
    implementation("androidx.cardview:cardview:1.0.0")

    // Debug/Tooling
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-tooling-data:$compose_ui_version")

    // Test
    testImplementation("junit:junit:$junit_version")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_test_version")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:$compose_test_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-tooling-data:$compose_ui_version")
}

kapt {
    correctErrorTypes = true
}