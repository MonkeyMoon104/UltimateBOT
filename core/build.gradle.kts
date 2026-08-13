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
    implementation(project(":api"))
    implementation(project(":common"))
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-1_21_4").get().requiredVersion)

    // Must be shaded+relocated; Paper libraries keep org.bstats and bStats refuses that package.
    implementation(libs.bstats.bukkit)
    // Shade+relocate: Paper 1.21.x ships jackson-databind 2.13 on the server classpath,
    // which collides with plugin.yml library jackson 2.22 (jsr310 NoSuchFieldError).
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

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "pathetic" to libs.versions.pathetic.get(),
        "configurate" to libs.versions.configurate.get(),
        "lamp" to libs.versions.lamp.get(),
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
