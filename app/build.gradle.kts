plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.datausagetracker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.datausagetracker"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.work:work-runtime:2.10.0")
    implementation(libs.work.runtime)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") //data visualization
    annotationProcessor("androidx.room:room-compiler:2.6.1") //like lombok (getter setter)
    // Retrofit & JSON Converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // HTTP Logs
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}