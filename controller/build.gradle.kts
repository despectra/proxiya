plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":shared"))
            // gRPC зависимости будут добавлены позже
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Настройка для создания исполняемого JAR будет добавлена позже
