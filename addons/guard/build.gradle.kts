import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("ultimatebot.java-library")
    alias(libs.plugins.api.publish)
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(libs.paper.api)
    testImplementation(project(":common"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.paper.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.jar {
    archiveFileName.set("UltimateBot-Guard.jar")
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        artifactId = "ultimatebot-guard"
    }
}
