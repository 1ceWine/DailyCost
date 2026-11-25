import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/* 正式签名证书配置（密码不入库，仅保存在本机 keystore.properties） */
val signingProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) FileInputStream(f).use { load(it) }
}

android {
    namespace = "com.dailycost.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dailycost.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(signingProps.getProperty("storeFile") ?: "signing/dailycost-release.jks")
            storePassword = signingProps.getProperty("storePassword") ?: ""
            keyAlias = signingProps.getProperty("keyAlias") ?: "dailycost"
            keyPassword = signingProps.getProperty("keyPassword") ?: ""
        }
    }

    buildTypes {
        /* 调试包与正式包共用同一张证书，后续覆盖安装不会因签名冲突丢数据 */
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
    lint {
        /* 离线构建环境无 lint-gradle 依赖，正式包不做 lintVital 检查 */
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
}
