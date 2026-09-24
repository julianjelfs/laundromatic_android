import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val firebaseApiKey = localProperties.getProperty("firebase.apiKey")
    ?: providers.environmentVariable("LAUNDROMATIC_FIREBASE_API_KEY").orNull
    ?: throw GradleException(
        "Missing Firebase API key. Set firebase.apiKey in local.properties " +
            "or the LAUNDROMATIC_FIREBASE_API_KEY environment variable.",
    )

android {
    val releaseKeystoreFile = providers.environmentVariable("LAUNDROMATIC_KEYSTORE_FILE").orNull
    val releaseStorePassword = providers.environmentVariable("LAUNDROMATIC_KEYSTORE_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("LAUNDROMATIC_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("LAUNDROMATIC_KEY_PASSWORD").orNull

    namespace = "com.julianjelfs.laundromatic"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.julianjelfs.laundromatic"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
    }

    signingConfigs {
        if (
            !releaseKeystoreFile.isNullOrBlank() &&
            !releaseStorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()
        ) {
            create("releaseEnv") {
                storeFile = rootProject.file(releaseKeystoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            signingConfigs.findByName("releaseEnv")?.let { signingConfig = it }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    val firebaseBom = platform("com.google.firebase:firebase-bom:33.1.2")

    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(firebaseBom)

    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
