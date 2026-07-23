import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.material.icons.extended)
            implementation(libs.filekit.core)
            implementation(libs.androidx.startup.runtime)
            implementation(libs.coil.compose)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            implementation(libs.coil.network.ktor3)
            implementation(libs.ktor.client.cio)
            implementation(project(":shared"))
            implementation(libs.google.accompanist.systemuicontroller)
            implementation(libs.androidx.core.ktx.v1120)
            implementation(libs.google.accompanist.systemuicontroller)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.uiTest)
            implementation(libs.androidx.uiautomator)
        }
        androidUnitTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.androidx.testExt.junit)
        }
    }
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "org.example.project.composeapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    testImplementation(libs.junit.junit)
    debugImplementation(libs.compose.uiTooling)
    androidTestImplementation(libs.androidx.uiautomator.v240)
}

