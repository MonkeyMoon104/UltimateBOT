import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    id("ultimatebot.nms.mojmap")
    alias(libs.plugins.shadow)
}

ultimatebotPaperweight {
    catalog("paper-bundle-26_2")
}

val invui = extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("invui-v2-2").get()

val compileLibInvui = configurations.create("compileLibInvui") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

val relocateInvuiCompile = tasks.register<ShadowJar>("relocateInvuiCompile") {
    archiveFileName.set("invui-v2-2-relocated-compile.jar")
    destinationDirectory.set(layout.buildDirectory.dir("relocated-compile"))
    configurations = listOf(compileLibInvui)
    mergeServiceFiles()
    relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess")
    relocate("org.jetbrains.annotations", "com.monkey.ultimatebot.libs.jetbrains.annotations")
    relocate("org.jspecify", "com.monkey.ultimatebot.libs.jspecify")
}

dependencies {
    add(compileLibInvui.name, invui)
    compileOnly(files(relocateInvuiCompile.map { it.archiveFile }))
}

tasks.named("compileJava").configure {
    dependsOn(relocateInvuiCompile)
}
