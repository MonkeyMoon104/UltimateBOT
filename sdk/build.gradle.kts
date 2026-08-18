import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("ultimatebot.java-library")
    alias(libs.plugins.api.publish)
    alias(libs.plugins.revapi)
}

ultimatebotJava {
    release.set(21)
    nullAway.set(false)
    werror.set(false)
}

dependencies {
    api(project(":common"))
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.jspecify)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.awaitility)
    testRuntimeOnly(libs.junit.platform.launcher)
}

revapi {
    oldGroup.set("com.monkey.ultimatebot")
    oldName.set("sdk")
    oldVersions.set(emptyList())
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        artifactId = "sdk"
    }
}
