import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}


android {
    namespace = "com.skku.studyhelper"
    compileSdk = 34

    val properties : Properties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }

    // Add manifest placeholders
    defaultConfig {
        applicationId = "com.skku.studyhelper"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["kakao_native_key"] = properties["kakao_native_key"] as String
        manifestPlaceholders["kakao_native_key2"] = properties["kakao_native_key2"] as String
        manifestPlaceholders["naver_client_id"] = properties["naver_client_id"] as String
        manifestPlaceholders["naver_client_secret"] = properties["naver_client_secret"] as String


        buildConfigField("String", "KAKAO_NATIVE_KEY", properties.getProperty("kakao_native_key"))
        buildConfigField("String", "NAVER_CLIENT_ID", properties.getProperty("naver_client_id"))
        buildConfigField("String", "NAVER_CLIENT_SCRETE", properties.getProperty("naver_client_secret"))

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.facebook.android:facebook-android-sdk:latest.release")
    implementation ("com.kakao.sdk:v2-user:2.20.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.navercorp.nid:oauth-jdk8:5.9.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}