import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("ultimatebot.java-library")
    alias(libs.plugins.api.publish)
    alias(libs.plugins.revapi)
}

dependencies {
    api(project(":common"))
    api(libs.jspecify)
    compileOnly(libs.paper.api)
    testImplementation(libs.paper.api)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

revapi {
    oldGroup.set("com.monkey.ultimatebot")
    oldName.set("api")
    oldVersions.set(emptyList())
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        artifactId = "api"
    }
}
