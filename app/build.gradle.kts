plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}
apply {
    from("and_res_guard.gradle")
}

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.2"

    signingConfigs {
        register("release") {
            storeFile = file("android-key.jks")
            storePassword = System.getenv("KSTOREPWD")
            keyAlias = System.getenv("KEYALIAS")
            keyPassword = System.getenv("KEYPWD")
        }
    }

    val gitCommitId = "git rev-parse --short HEAD".runCommand(project.rootDir)
    val gitCommitCount = "git rev-list --count HEAD".runCommand(project.rootDir).toInt()
    val baseVersionName = "1.8.1"

    defaultConfig {
        applicationId = "com.absinthe.libchecker"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = gitCommitCount
        versionName = "${baseVersionName}.r${gitCommitCount}.${gitCommitId}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isZipAlignEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // To inline the bytecode built with JVM target 1.8 into
    // bytecode that is being built with JVM target 1.6. (e.g. navArgs)

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-XXLanguage:+InlineClasses")
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }
}

configurations.all {
    exclude(group = "androidx.appcompat", module = "appcompat")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("com.absinthe.libraries.me:me:1.0.6")
    implementation("com.absinthe.libraries.utils:utils:1.0.4")

    val appCenterSdkVersion = "3.3.0"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")

    implementation("androidx.fragment:fragment-ktx:1.2.5")

    // Lifecycle
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-common-java8:${lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycleVersion}")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")

    // Room components
    val roomVersion = "2.2.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.core:core-ktx:1.5.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha05")
    implementation("androidx.browser:browser:1.2.0")

    implementation("com.google.android.material:material:1.2.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4")
    implementation("com.drakeet.about:about:2.4.1")
    implementation("com.drakeet.multitype:multitype:4.2.0")
    implementation("com.airbnb.android:lottie:3.4.2")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.blankj:utilcodex:1.29.0")
    implementation("com.jonathanfinerty.once:once:1.3.0")
    implementation("net.dongliu:apk-parser:2.6.10")
    implementation("io.coil-kt:coil:1.0.0-rc1")

    implementation("rikka.appcompat:appcompat:1.2.0-rc01")
    implementation("rikka.core:core:1.2.3")
    implementation("rikka.material:material:1.4.3")
    implementation("rikka.recyclerview:recyclerview-utils:1.1.0")

    val rikkaPreference = "4.2.0-alpha03"
    implementation("moe.shizuku.preference:preference-appcompat:$rikkaPreference")
    implementation("moe.shizuku.preference:preference-simplemenu-appcompat:$rikkaPreference")

    //XML layout to Java code
    annotationProcessor("com.zhangyue.we:x2c-apt:1.1.2")
    implementation("com.zhangyue.we:x2c-lib:1.0.6")

    //Network
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okio:okio:2.7.0")

    testImplementation("junit:junit:4.13")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.4")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}