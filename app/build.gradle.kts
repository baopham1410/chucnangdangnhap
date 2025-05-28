plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "1.9.24"
    id("com.google.gms.google-services") version "4.4.2"
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.login"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.login"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.3")) // Sử dụng phiên bản bom của bạn

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0") // Đã cập nhật lên phiên bản mới nhất và loại bỏ dòng cũ.
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1") // Kiểm tra phiên bản mới nhất


    // ** CÁC THƯ VIỆN BỔ SUNG ĐỂ TẠO VÀ QUÉT QR **

    // Thư viện tạo mã QR (ZXing Core)
    implementation ("com.google.zxing:core:3.5.3")

    // Thư viện ZXing-Android-Embedded (nếu bạn dùng để quét QR trong MainActivity)
    // Nếu bạn không dùng tính năng quét, có thể không cần.
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
}