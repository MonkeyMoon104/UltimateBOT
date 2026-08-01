import org.gradle.api.tasks.testing.Test

plugins {
    `java-library`
    alias(libs.plugins.api.publish)
    alias(libs.plugins.revapi)
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

tasks.named<Test>("test") {
    useJUnitPlatform()
}

revapi {
    oldGroup.set("com.monkey.mcbot")
    oldName.set("mcbot-sdk")
    oldVersions.set(listOf("1.4.1"))
}
