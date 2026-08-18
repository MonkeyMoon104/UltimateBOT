plugins {
    `kotlin-dsl`
}

group = "com.monkey.ultimatebot.build-logic"

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(libs.errorprone.gradle)
    implementation(libs.spotless.gradle)
    implementation(libs.paperweight.userdev.gradle)
}

kotlin {
    jvmToolchain(21)
}
