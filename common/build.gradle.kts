plugins {
    `java-library`
    alias(libs.plugins.api.publish)
}

dependencies {
    api(libs.jspecify)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
