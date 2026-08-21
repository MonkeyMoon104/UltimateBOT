plugins {
    id("ultimatebot.java-library")
    alias(libs.plugins.api.publish)
}

dependencies {
    api(libs.jspecify)
    implementation(libs.jar.relocator)
    implementation(libs.asm)
    implementation(libs.asm.commons)
    implementation(libs.asm.tree)
    implementation(libs.asm.analysis)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}
