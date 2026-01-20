import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.9.22"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.decompose.core)
            implementation(libs.decompose.ext.compose)
            implementation(libs.essenty.lifecycle.coroutines)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.compose.icons.feather)
            implementation(libs.compose.icons.fontawesome)
            implementation(libs.compose.icons.octicons)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.koin.core.jvm)
            implementation(project(":protocols"))
            implementation(libs.grpc.netty)
            implementation(libs.grpc.kotlin.stub)
            implementation(libs.protobuf.kotlin)
        }
    }
}


compose.desktop {
    application {
        mainClass = "ru.despectra.proxiya.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ru.despectra.proxiya"
            packageVersion = "1.0.0"
        }
    }
}
