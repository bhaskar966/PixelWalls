import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.seralization)
    alias(libs.plugins.buildkonfig)
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
            // Play Services
            implementation(libs.play.services.base)
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
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
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
        versionName = "0.0.1"
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "PixelWalls"
            packageVersion = "1.0.0"

            description = "A multiplatfrom replacement for wallpaper features in Pixel Phones."
            vendor = "Bhaskar Dey"

            windows {
                shortcut = true
                menu = true
                upgradeUuid = "f3b3a3c0-1234-4321-abcd-1234567890ab"
                menuGroup = "PixelWalls"
            }

            linux {
                shortcut = true
                menuGroup = "Graphics"
            }

            macOS {
                bundleID = "com.bhaskar.pixelwalls"
                dockName = "PixelWalls"
                signing {
                    identity = null
                }
            }
        }
    }
}

buildkonfig {
    packageName = "com.bhaskar.pixelwalls"
    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }


    val geminiApiKey: String = localProperties.getProperty("GEMINI_API_KEY")
        ?: System.getenv("GEMINI_API_KEY")
        ?: ""

    defaultConfigs {
        buildConfigField(STRING, "GEMINI_API_KEY", geminiApiKey)
    }
}