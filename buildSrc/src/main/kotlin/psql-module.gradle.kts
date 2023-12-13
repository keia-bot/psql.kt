plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    explicitApi()

    jvm {
        withJava()

        compilations.all {
            kotlinOptions.jvmTarget = "19"
        }
    }

    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }

    linuxX64()

    sourceSets["commonMain"].dependencies {
        implementation("io.ktor:ktor-io:2.3.7")
        implementation("io.ktor:ktor-http:2.3.7")

        implementation("io.github.oshai:kotlin-logging:5.1.1")

        implementation("naibu.stdlib:naibu-io:1.4-RC.8")
        implementation("naibu.stdlib:naibu-core:1.4-RC.8")
        implementation("naibu.stdlib:naibu-ktor-io:1.4-RC.8")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        implementation("org.jetbrains.kotlinx:atomicfu:0.23.1")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    }
}

tasks {
    val jvmMainClasses by named("jvmMainClasses") {
        dependsOn("compileJava")
    }

    val jvmTestClasses by named("jvmTestClasses") {
        dependsOn("compileJava")
    }
}