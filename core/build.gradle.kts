import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("ultimatebot.paperweight")
    alias(libs.plugins.shadow)
}

ultimatebotPaperweight {
    catalog("paper-bundle-1_21_4")
}

fun relocConfig(name: String) = configurations.create(name) {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

val compileLibJackson = relocConfig("compileLibJackson")
val compileLibLamp = relocConfig("compileLibLamp")
val compileLibConfigurate = relocConfig("compileLibConfigurate")
val compileLibPathetic = relocConfig("compileLibPathetic")
val compileLibBstats = relocConfig("compileLibBstats")
val compileLibCaffeine = relocConfig("compileLibCaffeine")
val compileLibInvui = relocConfig("compileLibInvui")

fun relocateLib(taskName: String, configuration: Configuration, archiveName: String): TaskProvider<ShadowJar> =
    tasks.register<ShadowJar>(taskName) {
        archiveFileName.set(archiveName)
        destinationDirectory.set(layout.buildDirectory.dir("relocated-compile"))
        configurations = listOf(configuration)
        mergeServiceFiles()
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        relocate("com.fasterxml.jackson", "com.monkey.ultimatebot.libs.jackson")
        relocate("revxrsal.commands", "com.monkey.ultimatebot.libs.lamp")
        relocate("org.spongepowered.configurate", "com.monkey.ultimatebot.libs.configurate")
        relocate("io.leangen.geantyref", "com.monkey.ultimatebot.libs.geantyref")
        relocate("net.kyori.option", "com.monkey.ultimatebot.libs.option")
        relocate("de.bsommerfeld.pathetic", "com.monkey.ultimatebot.libs.pathetic")
        relocate("org.bstats", "com.monkey.ultimatebot.libs.bstats")
        relocate("com.github.benmanes.caffeine", "com.monkey.ultimatebot.libs.caffeine")
        relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui")
        relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess")
        relocate("org.jetbrains.annotations", "com.monkey.ultimatebot.libs.jetbrains.annotations")
        relocate("org.checkerframework", "com.monkey.ultimatebot.libs.checkerframework")
        relocate("com.google.errorprone.annotations", "com.monkey.ultimatebot.libs.errorprone.annotations")
    }

val relocateJacksonCompile = relocateLib("relocateJacksonCompile", compileLibJackson, "jackson-relocated-compile.jar")
val relocateLampCompile = relocateLib("relocateLampCompile", compileLibLamp, "lamp-relocated-compile.jar")
val relocateConfigurateCompile =
    relocateLib("relocateConfigurateCompile", compileLibConfigurate, "configurate-relocated-compile.jar")
val relocatePatheticCompile = relocateLib("relocatePatheticCompile", compileLibPathetic, "pathetic-relocated-compile.jar")
val relocateBstatsCompile = relocateLib("relocateBstatsCompile", compileLibBstats, "bstats-relocated-compile.jar")
val relocateCaffeineCompile = relocateLib("relocateCaffeineCompile", compileLibCaffeine, "caffeine-relocated-compile.jar")
val relocateInvuiCompile = relocateLib("relocateInvuiCompile", compileLibInvui, "invui-relocated-compile.jar")

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))

    add(compileLibJackson.name, libs.jackson.databind)
    add(compileLibJackson.name, libs.jackson.datatype.jsr310)
    add(compileLibLamp.name, libs.lamp.bukkit)
    add(compileLibConfigurate.name, libs.configurate.yaml)
    add(compileLibPathetic.name, libs.pathetic.engine)
    add(compileLibBstats.name, libs.bstats.bukkit)
    add(compileLibCaffeine.name, libs.caffeine.legacy)
    add(compileLibInvui.name, libs.invui.v1)

    implementation(libs.boosted.yaml)

    compileOnly(files(relocateJacksonCompile.map { it.archiveFile }))
    compileOnly(files(relocateLampCompile.map { it.archiveFile }))
    compileOnly(files(relocateConfigurateCompile.map { it.archiveFile }))
    compileOnly(files(relocatePatheticCompile.map { it.archiveFile }))
    compileOnly(files(relocateBstatsCompile.map { it.archiveFile }))
    compileOnly(files(relocateCaffeineCompile.map { it.archiveFile }))
    compileOnly(files(relocateInvuiCompile.map { it.archiveFile }))

    compileOnly(libs.placeholderapi)
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.sirblobman.core)
    compileOnly(libs.combatlogx.api)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit.junit5)
    testImplementation(files(relocateConfigurateCompile.map { it.archiveFile }))
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named("compileTestJava").configure {
    dependsOn(relocateConfigurateCompile)
}

tasks.named("compileJava").configure {
    dependsOn(
        relocateJacksonCompile,
        relocateLampCompile,
        relocateConfigurateCompile,
        relocatePatheticCompile,
        relocateBstatsCompile,
        relocateCaffeineCompile,
        relocateInvuiCompile,
    )
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
