plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
    id("org.openapi.generator")
    alias(libs.plugins.hilt.android)
    id("com.google.devtools.ksp") version "2.2.21-2.0.4"
}

android {
    namespace = "com.stuf.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "API_BASE_URL", "\"http://37.21.130.4:5000\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(project(":app:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.dagger)
    implementation(libs.hilt.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi.converter)
    implementation(libs.retrofit.scalars.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    ksp(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

openApiGenerate {
    generatorName.set("kotlin")
    library.set("jvm-retrofit2")

    inputSpec.set("$projectDir/classroom_api.json")

    outputDir.set("$projectDir")

    packageName.set("com.stuf.data")
    apiPackage.set("com.stuf.data.api")
    modelPackage.set("com.stuf.data.model")

    globalProperties.set(
        mapOf(
            "apis" to "",
            "models" to "",
            "supportingFiles" to "",
            "apiDocs" to "false",
            "modelDocs" to "false",
            "apiTests" to "false",
            "modelTests" to "false"
        )
    )

    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "serializationLibrary" to "moshi",
            "useCoroutines" to "true"
        )
    )
}

tasks.named("preBuild") {
    dependsOn("openApiGenerate")
}

tasks.named<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate") {
    validateSpec.set(false)
}

// Удаление артефактов DataStore (*.preferences_pb), оставшихся после тестов
tasks.register("cleanDataStore") {
    doLast {
        fileTree(projectDir).matching { include("*.preferences_pb") }.files.forEach { it.delete() }
    }
}
tasks.named("clean") { dependsOn("cleanDataStore") }