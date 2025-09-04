plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":protocols"))
            
            // Protobuf зависимости
            implementation(libs.protobuf.java)
            implementation(libs.protobuf.kotlin)
            implementation(libs.grpc.protobuf)
            implementation(libs.grpc.stub)
            implementation(libs.grpc.kotlin.stub)
            implementation(libs.grpc.netty)
            
            // Logging
            implementation("org.slf4j:slf4j-api:2.0.9")
            implementation("ch.qos.logback:logback-classic:1.4.14")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinx.coroutines.get()}")
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Настройка для создания исполняемого JAR будет добавлена позже
