plugins {
    id("com.android.library") version "8.5.1"
    id("org.jetbrains.kotlin.android") version "1.9.24"
}

group = "com.zenlauncher.zenmode"

android {
    namespace = "com.zenlauncher.zenmode.coreapi"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
