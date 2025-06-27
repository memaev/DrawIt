plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.com.google.gms.google.services)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.firebase.crashlytics)
}

android {
    namespace = "com.llc.drawit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.llc.drawit"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation(libs.android.material.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    //dagger hilt
    implementation (libs.hilt.android)
    annotationProcessor (libs.hilt.compiler)

    implementation(libs.glide)
    implementation(libs.swiperefreshlayout)
    implementation(libs.circleimageview)
    implementation(libs.loading.button)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}