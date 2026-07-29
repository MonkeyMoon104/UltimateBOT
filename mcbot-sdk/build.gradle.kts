plugins {
    `java-library`
    alias(libs.plugins.api.publish)
}

dependencies {
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
