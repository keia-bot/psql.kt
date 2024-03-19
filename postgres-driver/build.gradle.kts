plugins {
    `psql-module`
}

kotlin.sourceSets["commonMain"].dependencies {
    api(projects.postgresProtocol)
    implementation("io.ktor:ktor-network:2.3.7")

    // SCRAM
    implementation(kotlincrypto.hash.md5)
    implementation(kotlincrypto.hash.sha2)
    implementation(kotlincrypto.secureRandom)
    implementation(kotlincrypto.macs.hmac.sha2)
}

kotlin.sourceSets["jvmMain"].dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.11")
}