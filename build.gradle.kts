plugins {
    kotlin("jvm") version "2.1.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://repo.kotlin.link")
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.8.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}