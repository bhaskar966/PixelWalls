import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.seralization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true

            // Add the framework exports here
            // Use linkerOpts to link against iOS system frameworks
            linkerOpts.addAll(listOf(
                "-framework", "UIKit",
                "-framework", "Foundation",
                "-framework", "CoreGraphics",
                "-framework", "CoreImage",
                "-framework", "CoreVideo",
                "-framework", "Vision",
                "-framework", "ImageIO"
            ))
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.jetbrains.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.core)
            // MLKit
            implementation(libs.mlkit.subject.segmentation)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.segmentation.selfie)
            // Ktor
            implementation(libs.ktor.client.android)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.jetbrains.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            //Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            //Ktor
            implementation(libs.ktor.client.core)
            //Serialization
            implementation(libs.kotlinx.serialization.json)
            //Navigation
            implementation(libs.navigation.compose)
            //Icon
            implementation(libs.fluentui.system.icons)
            //Filekit
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            //Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose)
            //Color picker
            implementation(libs.compose.colorpicker)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            //ONNX
            implementation(libs.onnxruntime)
            //Ktor
            implementation(libs.ktor.client.java)
            //JNA
            implementation("net.java.dev.jna:jna:5.18.1")
            implementation("net.java.dev.jna:jna-platform:5.18.1")
        }
        iosMain.dependencies {
            //ktor
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.bhaskar.pixelwalls"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bhaskar.pixelwalls"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    debugImplementation(libs.ui.tooling)
}

compose.desktop {
    application {
        mainClass = "com.bhaskar.pixelwalls.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.bhaskar.pixelwalls"
            packageVersion = "1.0.0"
        }
    }
}
