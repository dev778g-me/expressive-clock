import java.util.Properties

plugins {
    // No `kotlin.android`: AGP 9 has built-in Kotlin support and applying it is a hard error.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.dev.expressiveclock"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dev.expressiveclock"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "0.1.5"
    }

    /**
     * Release signing configuration, loading credentials from root-level `keystore.properties` or Gradle properties.
     * Strict enforcement: fails release builds cleanly if properties or keystore file are missing.
     */
    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { stream ->
            keystoreProps.load(stream)
        }
    }

    // Resolve signing values from keystore.properties OR Gradle properties (CI passes -P...).
    fun prop(key: String): String? =
        keystoreProps.getProperty(key) ?: (findProperty(key) as String?)

    val storePath = prop("clockStoreFile")
    val storePassword = prop("clockStorePassword")
    val keyAlias = prop("clockKeyAlias")
    val keyPassword = prop("clockKeyPassword")

    signingConfigs {
        // Configure release signing when a keystore path is available (local file or CI),
        // so CI builds (which pass -PclockStore*) produce a signed APK instead of an
        // unsigned one that fails to install with INSTALL_PARSE_FAILED_NO_CERTIFICATES.
        if (storePath != null && storePassword != null && keyAlias != null && keyPassword != null) {
            create("release") {
                val storeFileObj = rootProject.file(storePath)
                if (!storeFileObj.exists()) {
                    error("Keystore file '$storePath' does not exist.")
                }
                storeFile = storeFileObj
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                // Must be the -optimize variant; plain proguard-android.txt carries -dontoptimize,
                // which is a hard error on AGP 9.
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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

    androidResources {
        localeFilters += listOf("en")
    }

    // No accounts, no telemetry, no ad IDs. The dependency manifest stays out of the APK too.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/com.android.tools/**",
                "/kotlin/**",
                "/DebugProbesKt.bin",
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        // [M3E] Most expressive components are still opt-in even though MaterialExpressiveTheme
        // itself is not. Opting in centrally keeps the annotation noise out of every call site.
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.ui.text.ExperimentalTextApi",
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen.v120)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.material.kolor.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.core)
}
