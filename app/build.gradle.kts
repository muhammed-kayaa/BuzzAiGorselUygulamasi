plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.buzzai"
    compileSdk = 36 // Senin yazdığın karmaşık yapı yerine sadeleştirdik

    defaultConfig {
        applicationId = "com.example.buzzai"
        minSdk = 24
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
    } // <-- BURAYI KAPATTIK (Önceki hatan buradaydı)

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Retrofit & Gson (Yapay zeka API'si ile haberleşme ve JSON dönüşümü)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging (Ağ hatalarını konsolda görmek için)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines (Resim yüklenirken uygulamanın donmasını engellemek için)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Glide (Resim yükleme ve gösterme kütüphanesi)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

}
