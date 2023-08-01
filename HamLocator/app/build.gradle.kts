plugins {
    id("com.android.application")
}

android {
    namespace = "dk.seahawk.hamlocator"
    compileSdk = 33

    defaultConfig {
        applicationId = "dk.seahawk.hamlocator"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // source: https://medium.com/@david.m.precopia/how-to-add-junit-5-to-your-android-project-c9851aa63a62
    // Remove this, providing that you are exclusively refactoring to JUnit 5
    // testImplementation 'junit:junit:4.13.2'

    // Required, the core dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    // Optional, if you need Parameterized Tests
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")

    // Optional, if you plan on keeping your existing JUnit 4 based tests.
    // It provides a TestEngine for running v3 and v4 based tests on the v5 platform.
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}