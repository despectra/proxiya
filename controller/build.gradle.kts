plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version "1.9.22"
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

            // DI
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            
            // Logging
            implementation("org.slf4j:slf4j-api:2.0.9")
            implementation("ch.qos.logback:logback-classic:1.4.14")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinx.coroutines.get()}")
            
            // YAML configuration with kotlinx.serialization
            implementation("com.charleskorn.kaml:kaml:0.95.0")
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.junit.jupiter:junit-jupiter:5.10.1")
        }
    }
}

// Настройка для создания исполняемого JAR с зависимостями
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Creates a fat JAR with all dependencies"
    
    archiveBaseName.set("proxiya-controller")
    archiveVersion.set("")
    archiveClassifier.set("")
    
    // Включаем все классы из jvmMain
    from(sourceSets["jvmMain"].output)
    
    // Включаем все зависимости в JAR
    from(configurations["jvmRuntimeClasspath"].map { config ->
        if (config.isDirectory) config else zipTree(config)
    })
    
    // Исключаем дублирующиеся файлы
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // Устанавливаем манифест с main class
    manifest {
        attributes(
            "Main-Class" to "ru.despectra.proxiya.controller.MainKt",
            "Implementation-Title" to "Proxiya Controller",
            "Implementation-Version" to "1.0.0"
        )
    }
}

// Алиас для удобства
tasks.register("buildJar") {
    group = "build"
    description = "Builds the executable JAR file"
    dependsOn("fatJar")
}
