import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-library`
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

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveFileName.set("MinecraftBot-Guard.jar")
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        artifactId = "minecraftbot-guard"
    }
}
