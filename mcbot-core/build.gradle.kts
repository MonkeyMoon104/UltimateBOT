import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    alias(libs.plugins.paperweight.userdev)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":mcbot-api"))
    implementation(project(":common"))
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-1_21_4").get().requiredVersion)

    implementation(libs.bstats.bukkit)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.invui.v1)
    implementation(libs.caffeine)
    implementation(libs.pathetic.engine)
    implementation(libs.configurate.yaml)
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

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
