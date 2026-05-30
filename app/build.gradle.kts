plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.woodworking.calculatorpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.woodworking.calculatorpro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Strict offline app: no telemetry, no analytics SDK.
        // The only network-adjacent surface is Google Play Billing, used only
        // when the user explicitly taps "Unlock Pro". JVM unit tests live in
        // src/test and run on the host JVM (no instrumentation).
        vectorDrawables { useSupportLibrary = true }
        // Languages we ship now. Add new ones here once translations land in
        // res/values-<locale>/strings.xml — order is alphabetical for diff
        // friendliness, "en" is implicit via the default values/ folder.
        resourceConfigurations += listOf("en", "de", "es", "fr", "ja", "pt-rBR", "zh")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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
        // BuildConfig stays on so we can flip a debug-only "force Pro" flag
        // for QA without needing a real Play purchase. See BillingManager.
        buildConfig = true
    }

    testOptions {
        unitTests {
            // Domain calculators don't touch Android APIs, so we can run them
            // as plain JVM tests — no Robolectric needed.
            isIncludeAndroidResources = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Keep room schemas inside the project (helpful for migrations).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // One-time IAP for the Pro unlock. No subscription, no consumable, no
    // remote config — see BillingManager.kt for the full contract.
    implementation(libs.billing.ktx)

    testImplementation(libs.junit)
}
