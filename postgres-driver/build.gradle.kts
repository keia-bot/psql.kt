plugins {
    `psql-module`
}

kotlin.sourceSets["commonMain"].dependencies {
    api(projects.postgresProtocol)
    implementation("io.ktor:ktor-network:2.3.7")
}