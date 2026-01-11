plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    id("jacoco")
}

android {
    namespace = "com.example.sioribi"
    compileSdk {
        version = release(36)
    }
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.example.sioribi"
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
                "proguard-rules.pro",
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
}

jacoco {
    toolVersion = "0.8.11"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

val jacocoExclusions =
    listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

val debugKotlinClasses =
    fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(jacocoExclusions)
    }
val debugJavaClasses =
    fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
        exclude(jacocoExclusions)
    }
val jacocoSourceDirectories = files("src/main/java", "src/main/kotlin")
val jacocoExecutionData =
    fileTree(layout.buildDirectory) {
        include(
            "jacoco/testDebugUnitTest.exec",
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
        )
    }

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(debugKotlinClasses, debugJavaClasses)
    sourceDirectories.setFrom(jacocoSourceDirectories)
    executionData.setFrom(jacocoExecutionData)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(debugKotlinClasses, debugJavaClasses)
    sourceDirectories.setFrom(jacocoSourceDirectories)
    executionData.setFrom(jacocoExecutionData)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.33".toBigDecimal()
            }
        }
    }
}
