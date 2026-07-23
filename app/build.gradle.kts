import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application") version "8.5.1"
    id("org.jetbrains.kotlin.android") version "1.9.24"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.zenlauncher.zenmode"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.zenlauncher.zenmode"
        minSdk = 28
        targetSdk = 35
        versionCode = 9
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Inject Web Client ID as a BuildConfig field
        val webClientId = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID") ?: "YOUR_WEB_CLIENT_ID"
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE") ?: ""
            if (storeFilePath.isNotEmpty()) {
                storeFile = file(storeFilePath)
            }
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            ndk {
                debugSymbolLevel = "full"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("com.zenlauncher.zenmode:core-api")
    
    val usePrivateCore = rootProject.file("../zenmode_core_private").exists()
    if (usePrivateCore) {
        runtimeOnly("com.zenlauncher.zenmode:core-private:1.0.0")
    } else {
        runtimeOnly(project(":core-mock"))
    }

    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("io.coil-kt:coil-compose:2.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.credentials:credentials:1.2.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test:rules:1.5.0")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
}

koverReport {
    defaults {
        mergeWith("debug")
        filters {
            excludes {
                classes(
                    "com.zenlauncher.zenmode.*Activity*",
                    "com.zenlauncher.zenmode.*Fragment*",
                    "com.zenlauncher.zenmode.*Adapter*",
                    "com.zenlauncher.zenmode.*ProgressBar*"
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}