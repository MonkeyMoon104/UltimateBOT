import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("ultimatebot.paperweight")
}

ultimatebotPaperweight {
    catalog("paper-bundle-1_21_4")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))

    implementation(libs.bstats.bukkit)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    compileOnly(libs.caffeine.legacy)
    compileOnly(libs.pathetic.engine)
    compileOnly(libs.configurate.yaml)
    compileOnly(libs.lamp.common)
    compileOnly(libs.lamp.bukkit)
    implementation(libs.invui.v1)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.sirblobman.core)
    compileOnly(libs.combatlogx.api)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
