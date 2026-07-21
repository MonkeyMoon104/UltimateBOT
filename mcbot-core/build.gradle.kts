import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.paperweight.userdev)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(project(":mcbot-api"))
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-1_21_4").get().requiredVersion)

    implementation(libs.bstats.bukkit)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.invui.v1)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.sirblobman.core)
    compileOnly(libs.combatlogx.api)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
