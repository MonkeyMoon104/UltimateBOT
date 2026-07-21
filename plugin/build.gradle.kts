import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import proguard.gradle.ProGuardTask
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class InvuiRetarget(val prefix: String, val invui: String, val inventoryaccess: String)

fun retargetInvuiV2References(bytes: ByteArray, invuiTarget: String, inventoryAccessTarget: String): ByteArray {
    val binary = bytes.toString(Charsets.ISO_8859_1)
        .replace("com/monkey/mcbot/libs/invui/v1/", invuiTarget)
        .replace("com/monkey/mcbot/libs/inventoryaccess/v1/", inventoryAccessTarget)
    return binary.toByteArray(Charsets.ISO_8859_1)
}

fun normalizeClassMajor(bytes: ByteArray, maxClassMajor: Int): ByteArray {
    if (bytes.size < 8) {
        return bytes
    }

    if ((bytes[0].toInt() and 0xFF) != 0xCA
        || (bytes[1].toInt() and 0xFF) != 0xFE
        || (bytes[2].toInt() and 0xFF) != 0xBA
        || (bytes[3].toInt() and 0xFF) != 0xBE
    ) {
        return bytes
    }

    val major = ((bytes[6].toInt() and 0xFF) shl 8) or (bytes[7].toInt() and 0xFF)
    if (major > maxClassMajor) {
        bytes[6] = ((maxClassMajor shr 8) and 0xFF).toByte()
        bytes[7] = (maxClassMajor and 0xFF).toByte()
    }
    return bytes
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    alias(libs.plugins.shadow)
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

configurations.configureEach {
    if (isCanBeResolved) {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
}

tasks.withType<ShadowJar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching(listOf("META-INF/services/**", "META-INF/*.kotlin_module")) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

val invuiV2_1Shade = configurations.create("invuiV2_1Shade") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val invuiV2_2Shade = configurations.create("invuiV2_2Shade") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar")
val obfuscatedJarFile = layout.buildDirectory.file("libs/MinecraftBot.jar")
val legacyObfuscatedJarFile = layout.buildDirectory.file("libs/MinecraftBot-obf.jar")
val proguardMappingFile = layout.buildDirectory.file("reports/proguard/mapping.txt")
val javaToolchainService = extensions.getByType<JavaToolchainService>()
val proguardJdkHome = javaToolchainService.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
}.get().metadata.installationPath.asFile
val javaBaseJmod = File(proguardJdkHome, "jmods/java.base.jmod")
val javaLoggingJmod = File(proguardJdkHome, "jmods/java.logging.jmod")

fun externalClasspathFor(dependencyProject: Project): FileCollection {
    val sourceSets = dependencyProject.extensions.getByType<SourceSetContainer>()
    return sourceSets.named("main").get().compileClasspath.filter { candidate ->
        candidate.exists() && !candidate.toPath().startsWith(rootDir.toPath())
    }
}

val proguardLibraries = files(
    javaBaseJmod,
    javaLoggingJmod,
    externalClasspathFor(project(":mcbot-core")),
    externalClasspathFor(project(":versions:v1_21_4")),
    externalClasspathFor(project(":versions:v1_21_5")),
    externalClasspathFor(project(":versions:v1_21_6")),
    externalClasspathFor(project(":versions:v1_21_7")),
    externalClasspathFor(project(":versions:v1_21_8")),
    externalClasspathFor(project(":versions:v1_21_9")),
    externalClasspathFor(project(":versions:v1_21_10")),
    externalClasspathFor(project(":versions:v1_21_11")),
    externalClasspathFor(project(":versions:v26_1")),
    externalClasspathFor(project(":versions:v26_2"))
)

if (!javaBaseJmod.exists() || !javaLoggingJmod.exists()) {
    throw GradleException("ProGuard requires a Java 21 JDK with jmods. Resolved toolchain: $proguardJdkHome")
}

dependencies {
    implementation(project(":mcbot-core"))
    implementation(project(path = ":versions:v1_21_4", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_5", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_6", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_7", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_8", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_9", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_10", configuration = "reobf"))
    implementation(project(path = ":versions:v1_21_11", configuration = "reobf"))
    implementation(project(path = ":versions:v26_1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":versions:v26_2", configuration = "runtimeElements")) {
        isTransitive = false
    }
    add(invuiV2_1Shade.name, libsCatalog.findLibrary("invui-v2-1").get())
    add(invuiV2_2Shade.name, libsCatalog.findLibrary("invui-v2-2").get())
}

val relocateInvuiV2_1Task = tasks.register<ShadowJar>("relocateInvuiV2_1") {
    archiveFileName.set("invui-v2_1-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(invuiV2_1Shade)
    mergeServiceFiles()
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.a1")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.a1")
}

val relocateInvuiV2_2Task = tasks.register<ShadowJar>("relocateInvuiV2_2") {
    archiveFileName.set("invui-v2_2-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(invuiV2_2Shade)
    mergeServiceFiles()
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.a2")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.a2")
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(relocateInvuiV2_1Task, relocateInvuiV2_2Task)
    archiveFileName.set("MinecraftBot-unobfuscated.jar")
    mergeServiceFiles()
    from({
        zipTree(relocateInvuiV2_1Task.get().archiveFile.get().asFile)
    })
    from({
        zipTree(relocateInvuiV2_2Task.get().archiveFile.get().asFile)
    })
    relocate("org.bstats", "com.monkey.mcbot.libs.bstats")
    relocate("com.fasterxml.jackson", "com.monkey.mcbot.libs.jackson")
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.v1") {
        exclude("com/monkey/mcbot/gui/v26_1/**")
        exclude("com/monkey/mcbot/gui/v26_2/**")
    }
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.v1") {
        exclude("com/monkey/mcbot/gui/v26_1/**")
        exclude("com/monkey/mcbot/gui/v26_2/**")
    }
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.a1") {
        include("com/monkey/mcbot/gui/v26_1/**")
    }
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.a1") {
        include("com/monkey/mcbot/gui/v26_1/**")
    }
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.a2") {
        include("com/monkey/mcbot/gui/v26_2/**")
    }
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.a2") {
        include("com/monkey/mcbot/gui/v26_2/**")
    }
    doLast {
        val jarFile = archiveFile.get().asFile
        val patchedJar = File(jarFile.parentFile, "${jarFile.name}.patched")
        val v26Retargets = listOf(
            InvuiRetarget("com/monkey/mcbot/gui/v26_1/", "com/monkey/mcbot/libs/invui/a1/", "com/monkey/mcbot/libs/inventoryaccess/a1/"),
            InvuiRetarget("com/monkey/mcbot/gui/v26_2/", "com/monkey/mcbot/libs/invui/a2/", "com/monkey/mcbot/libs/inventoryaccess/a2/")
        )
        val maxClassMajor = 65

        ZipFile(jarFile).use { zip ->
            ZipOutputStream(FileOutputStream(patchedJar)).use { zos ->
                zip.entries().asSequence().forEach { entry ->
                    val newEntry = ZipEntry(entry.name)
                    newEntry.time = entry.time
                    zos.putNextEntry(newEntry)
                    var data = zip.getInputStream(entry).use { it.readBytes() }

                    if (!entry.isDirectory && entry.name.endsWith(".class")) {
                        v26Retargets.firstOrNull { entry.name.startsWith(it.prefix) }?.let { retargetRule ->
                            data = retargetInvuiV2References(data, retargetRule.invui, retargetRule.inventoryaccess)
                        }
                        data = normalizeClassMajor(data, maxClassMajor)
                    }

                    zos.write(data)
                    zos.closeEntry()
                }
            }
        }

        if (!jarFile.delete() || !patchedJar.renameTo(jarFile)) {
            throw GradleException("Failed to patch ${jarFile.name} with v26 InvUI v2 references")
        }
    }
}

tasks.register<ProGuardTask>("obfuscatePluginJar") {
    dependsOn(shadowJarTask)

    doFirst {
        delete(legacyObfuscatedJarFile.get().asFile)
    }

    injars(shadowJarTask.get().archiveFile.get().asFile)
    outjars(obfuscatedJarFile.get().asFile)

    configuration(file("$projectDir/proguard.pro"))
    libraryjars(proguardLibraries)
    printmapping(proguardMappingFile.get().asFile)
}

shadowJarTask.configure {
    finalizedBy(tasks.named("obfuscatePluginJar"))
}

tasks.named("build") {
    dependsOn(shadowJarTask)
}
