import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import proguard.gradle.ProGuardTask
import java.io.File
import java.io.FileOutputStream
import java.net.URLClassLoader
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.HexFormat
import java.util.function.IntSupplier
import java.util.function.LongSupplier
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class InvuiRetarget(val prefix: String, val invui: String, val inventoryaccess: String)

fun retargetInvuiV2References(bytes: ByteArray, invuiTarget: String, inventoryAccessTarget: String): ByteArray {
    val binary = bytes.toString(Charsets.ISO_8859_1)
        .replace("com/monkey/ultimatebot/libs/invui/v1/", invuiTarget)
        .replace("com/monkey/ultimatebot/libs/inventoryaccess/v1/", inventoryAccessTarget)
    return binary.toByteArray(Charsets.ISO_8859_1)
}

fun retargetBinaryStrings(bytes: ByteArray, from: String, to: String): ByteArray {
    if (from.length != to.length) {
        error("Binary string retarget requires equal UTF-8/Latin-1 lengths: '$from' (${from.length}) vs '$to' (${to.length})")
    }
    val binary = bytes.toString(Charsets.ISO_8859_1).replace(from, to)
    return binary.toByteArray(Charsets.ISO_8859_1)
}

fun normalizeClassMajor(bytes: ByteArray, maxClassMajor: Int): ByteArray {
    if (bytes.size < 8) {
        return bytes
    }

    if ((bytes[0].toInt() and 0xFF) != 0xCA ||
        (bytes[1].toInt() and 0xFF) != 0xFE ||
        (bytes[2].toInt() and 0xFF) != 0xBA ||
        (bytes[3].toInt() and 0xFF) != 0xBE
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

fun shouldEmitJava8Major(entryName: String): Boolean {
    // Disabled until common/core actually compile with --release 8. Rewriting majors alone
    // would lie about bytecode and break verification on Java 8 JVMs.
    return false
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// buildLogic shades NMS v26 (toolchain 25). Request JVM 25 so those variants resolve.
// common/core emit Java 8; NMS adapters keep higher majors and load only on modern JVMs.
configurations.matching { it.isCanBeResolved }.configureEach {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
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
    // InvUI 2.x variants require a high consumer JVM attribute; shaded classes are
    // still normalized to Java 17 major (61) in shadowJar for Paper 1.17.1 Commodore.
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

val invuiV2_2Shade = configurations.create("invuiV2_2Shade") {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

val caffeineLegacyShade = configurations.create("caffeineLegacyShade") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val caffeineModernShade = configurations.create("caffeineModernShade") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar")
val obfuscatedJarFile = layout.buildDirectory.file("libs/UltimateBot.jar")
val legacyObfuscatedJarFile = layout.buildDirectory.file("libs/UltimateBot-obf.jar")
val proguardMappingFile = layout.buildDirectory.file("reports/proguard/mapping.txt")
val javaToolchainService = extensions.getByType<JavaToolchainService>()
val proguardJdkHome = javaToolchainService.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
}.get().metadata.installationPath.asFile
val javaBaseJmod = File(proguardJdkHome, "jmods/java.base.jmod")
val javaLoggingJmod = File(proguardJdkHome, "jmods/java.logging.jmod")

evaluationDependsOn(":addons:metrics")
evaluationDependsOn(":addons:guard")
evaluationDependsOn(":core")
val metricsAddonJarTask = project(":addons:metrics").tasks.named<ShadowJar>("shadowJar")
val guardAddonJarTask = project(":addons:guard").tasks.named<Jar>("jar")
val metricsAddonDescriptorFile = layout.buildDirectory.file("generated/addons/metrics.properties")
val guardAddonDescriptorFile = layout.buildDirectory.file("generated/addons/guard.properties")
val releaseVersion = providers.environmentVariable("RELEASE_VERSION").orElse(project.version.toString())

val generateMetricsAddonDescriptorTask = tasks.register("generateMetricsAddonDescriptor") {
    dependsOn(metricsAddonJarTask)
    inputs.file(metricsAddonJarTask.flatMap { it.archiveFile })
    inputs.property("releaseVersion", releaseVersion)
    outputs.file(metricsAddonDescriptorFile)

    doLast {
        val addonJar = metricsAddonJarTask.get().archiveFile.get().asFile
        val digest = MessageDigest.getInstance("SHA-256")
        addonJar.inputStream().use { input ->
            val buffer = ByteArray(16_384)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        val version = releaseVersion.get()
        val descriptor = metricsAddonDescriptorFile.get().asFile
        descriptor.parentFile.mkdirs()
        descriptor.writeText(
            """
            version=$version
            url=https://repo.monkeymoon104.it/releases/com/monkey/ultimatebot/ultimatebot-metrics/$version/ultimatebot-metrics-$version.jar
            sha256=${HexFormat.of().formatHex(digest.digest())}
            size=${addonJar.length()}
            factory-class=com.monkey.ultimatebot.metrics.addon.MicrometerMetricsBackendFactory
            """.trimIndent() + "\n",
        )
    }
}

val generateGuardAddonDescriptorTask = tasks.register("generateGuardAddonDescriptor") {
    dependsOn(guardAddonJarTask)
    inputs.file(guardAddonJarTask.flatMap { it.archiveFile })
    inputs.property("releaseVersion", releaseVersion)
    outputs.file(guardAddonDescriptorFile)

    doLast {
        val addonJar = guardAddonJarTask.get().archiveFile.get().asFile
        val digest = MessageDigest.getInstance("SHA-256")
        addonJar.inputStream().use { input ->
            val buffer = ByteArray(16_384)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        val version = releaseVersion.get()
        val descriptor = guardAddonDescriptorFile.get().asFile
        descriptor.parentFile.mkdirs()
        descriptor.writeText(
            """
            version=$version
            url=https://repo.monkeymoon104.it/releases/com/monkey/ultimatebot/ultimatebot-guard/$version/ultimatebot-guard-$version.jar
            sha256=${HexFormat.of().formatHex(digest.digest())}
            size=${addonJar.length()}
            factory-class=com.monkey.ultimatebot.guard.addon.UltimateBotGuardBackendFactory
            """.trimIndent() + "\n",
        )
    }
}

fun externalClasspathFor(dependencyProject: Project): FileCollection {
    val sourceSets = dependencyProject.extensions.getByType<SourceSetContainer>()
    return sourceSets.named("main").get().compileClasspath.filter { candidate ->
        candidate.exists() && !candidate.toPath().startsWith(rootDir.toPath())
    }
}

val proguardLibraries = files(
    javaBaseJmod,
    javaLoggingJmod,
    externalClasspathFor(project(":core")),
)

if (!javaBaseJmod.exists() || !javaLoggingJmod.exists()) {
    throw GradleException("ProGuard requires a Java 21 JDK with jmods. Resolved toolchain: $proguardJdkHome")
}

dependencies {
    implementation(project(":core"))
    implementation(project(path = ":NMS:v1_17", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_17_1", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_18_1", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_18_2", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_19", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_19_2", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_19_3", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_19_4", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_20", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_20_1", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_20_2", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_20_4", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_20_6", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_1", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_3", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_4", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_5", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_6", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_7", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_8", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_9", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_10", configuration = "reobf"))
    implementation(project(path = ":NMS:v1_21_11", configuration = "reobf"))
    implementation(project(path = ":NMS:v26_1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v26_2", configuration = "runtimeElements")) {
        isTransitive = false
    }
    add(invuiV2_1Shade.name, libsCatalog.findLibrary("invui-v2-1").get())
    add(invuiV2_2Shade.name, libsCatalog.findLibrary("invui-v2-2").get())
    add(caffeineLegacyShade.name, libsCatalog.findLibrary("caffeine-legacy").get())
    add(caffeineModernShade.name, libsCatalog.findLibrary("caffeine-modern").get())
}

val relocateInvuiV2_1Task = tasks.register<ShadowJar>("relocateInvuiV2_1") {
    archiveFileName.set("invui-v2_1-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(invuiV2_1Shade)
    mergeServiceFiles()
    exclude("colors.bin")
    exclude("xyz/xenondevs/invui/util/ColorPalette.class")
    exclude("xyz/xenondevs/invui/window/CartographyWindow*.class")
    relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui.a1")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess.a1")
}

val relocateInvuiV2_2Task = tasks.register<ShadowJar>("relocateInvuiV2_2") {
    archiveFileName.set("invui-v2_2-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(invuiV2_2Shade)
    mergeServiceFiles()
    exclude("colors.bin")
    exclude("xyz/xenondevs/invui/util/ColorPalette.class")
    exclude("xyz/xenondevs/invui/window/CartographyWindow*.class")
    relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui.a2")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess.a2")
}

val relocateCaffeineLegacyTask = tasks.register<ShadowJar>("relocateCaffeineLegacy") {
    archiveFileName.set("caffeine-legacy-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(caffeineLegacyShade)
    // Equal-length rename so backend .class constant pools can be patched in-place.
    relocate("com.github.benmanes.caffeine", "com.monkey.ultimatebot.cafe2")
}

val relocateCaffeineModernTask = tasks.register<ShadowJar>("relocateCaffeineModern") {
    archiveFileName.set("caffeine-modern-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(caffeineModernShade)
    relocate("com.github.benmanes.caffeine", "com.monkey.ultimatebot.cafe3")
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(
        relocateInvuiV2_1Task,
        relocateInvuiV2_2Task,
        relocateCaffeineLegacyTask,
        relocateCaffeineModernTask,
        generateMetricsAddonDescriptorTask,
        generateGuardAddonDescriptorTask,
    )
    archiveFileName.set("UltimateBot-unobfuscated.jar")
    mergeServiceFiles()
    exclude("colors.bin")
    exclude("xyz/xenondevs/invui/util/ColorPalette.class")
    exclude("xyz/xenondevs/invui/window/CartographyWindow*.class")
    exclude("com/monkey/ultimatebot/libs/invui/a1/util/ColorPalette.class")
    exclude("com/monkey/ultimatebot/libs/invui/a1/window/CartographyWindow*.class")
    exclude("com/monkey/ultimatebot/libs/invui/a2/util/ColorPalette.class")
    exclude("com/monkey/ultimatebot/libs/invui/a2/window/CartographyWindow*.class")
    from({
        zipTree(relocateInvuiV2_1Task.get().archiveFile.get().asFile)
    })
    from({
        zipTree(relocateInvuiV2_2Task.get().archiveFile.get().asFile)
    })
    from({
        zipTree(relocateCaffeineLegacyTask.get().archiveFile.get().asFile)
    })
    from({
        zipTree(relocateCaffeineModernTask.get().archiveFile.get().asFile)
    })
    from(metricsAddonDescriptorFile) {
        into("META-INF/ultimatebot/addons")
    }
    from(guardAddonDescriptorFile) {
        into("META-INF/ultimatebot/addons")
    }
    relocate("org.bstats", "com.monkey.ultimatebot.libs.bstats")
    relocate("com.fasterxml.jackson", "com.monkey.ultimatebot.libs.jackson")
    relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui.v1") {
        exclude("com/monkey/ultimatebot/gui/v26_1/**")
        exclude("com/monkey/ultimatebot/gui/v26_2/**")
    }
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess.v1") {
        exclude("com/monkey/ultimatebot/gui/v26_1/**")
        exclude("com/monkey/ultimatebot/gui/v26_2/**")
    }
    relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui.a1") {
        include("com/monkey/ultimatebot/gui/v26_1/**")
    }
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess.a1") {
        include("com/monkey/ultimatebot/gui/v26_1/**")
    }
    relocate("xyz.xenondevs.invui", "com.monkey.ultimatebot.libs.invui.a2") {
        include("com/monkey/ultimatebot/gui/v26_2/**")
    }
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.ultimatebot.libs.inventoryaccess.a2") {
        include("com/monkey/ultimatebot/gui/v26_2/**")
    }
    doLast {
        val jarFile = archiveFile.get().asFile
        val patchedJar = File(jarFile.parentFile, "${jarFile.name}.patched")
        val v26Retargets = listOf(
            InvuiRetarget("com/monkey/ultimatebot/gui/v26_1/", "com/monkey/ultimatebot/libs/invui/a1/", "com/monkey/ultimatebot/libs/inventoryaccess/a1/"),
            InvuiRetarget("com/monkey/ultimatebot/gui/v26_2/", "com/monkey/ultimatebot/libs/invui/a2/", "com/monkey/ultimatebot/libs/inventoryaccess/a2/"),
        )
        // Do not rewrite class majors globally: common/core emit Java 8 via --release 8;
        // NMS modules and shaded InvUI keep their native majors and load only on capable JVMs.
        val maxClassMajor = 52
        val caffeine2Prefix = "com/monkey/ultimatebot/bot/ai/services/cache/Caffeine2UuidCache"
        val caffeine3Prefix = "com/monkey/ultimatebot/bot/ai/services/cache/Caffeine3UuidCache"
        // Must stay equal-length with com/github/benmanes/caffeine (and dotted form).
        val caffeineOwnerSlash = "com/github/benmanes/caffeine"
        val caffeineOwnerDot = "com.github.benmanes.caffeine"
        val caffeine2Slash = "com/monkey/ultimatebot/cafe2"
        val caffeine2Dot = "com.monkey.ultimatebot.cafe2"
        val caffeine3Slash = "com/monkey/ultimatebot/cafe3"
        val caffeine3Dot = "com.monkey.ultimatebot.cafe3"
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
                        if (entry.name.startsWith(caffeine2Prefix)) {
                            data = retargetBinaryStrings(data, caffeineOwnerSlash, caffeine2Slash)
                            data = retargetBinaryStrings(data, caffeineOwnerDot, caffeine2Dot)
                        } else if (entry.name.startsWith(caffeine3Prefix)) {
                            data = retargetBinaryStrings(data, caffeineOwnerSlash, caffeine3Slash)
                            data = retargetBinaryStrings(data, caffeineOwnerDot, caffeine3Dot)
                        }
                        // Only clamp accidental high majors in shared core packages — never touch
                        // versioned NMS adapters or third-party libs (InvUI, etc.).
                        if (shouldEmitJava8Major(entry.name)) {
                            data = normalizeClassMajor(data, maxClassMajor)
                        }
                        // Paper 1.20.5–1.21.x PluginRemapper (ASM) cannot parse Java 25 (major 69)
                        // classfiles. Clamp headers to Java 21 so remap succeeds; those classes are
                        // only executed on 26.x / Java 25+ runtimes.
                        data = normalizeClassMajor(data, 65)
                    }

                    zos.write(data)
                    zos.closeEntry()
                }
            }
        }

        try {
            Files.move(
                patchedJar.toPath(),
                jarFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(patchedJar.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

val obfuscatePluginJarTask = tasks.register<ProGuardTask>("obfuscatePluginJar") {
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

val verifyObfuscatedPluginJarTask = tasks.register("verifyObfuscatedPluginJar") {
    group = "verification"
    description = "Smoke-tests reflective dependencies and optional addons."
    dependsOn(metricsAddonJarTask, guardAddonJarTask)
    mustRunAfter(obfuscatePluginJarTask)

    doLast {
        val pluginJar = obfuscatedJarFile.get().asFile
        check(pluginJar.isFile) {
            "Obfuscated plugin jar not found: ${pluginJar.absolutePath}"
        }

        URLClassLoader(arrayOf(pluginJar.toURI().toURL()), ClassLoader.getPlatformClassLoader()).use { loader ->
            val addonJar = metricsAddonJarTask.get().archiveFile.get().asFile
            URLClassLoader(arrayOf(addonJar.toURI().toURL()), loader).use { addonLoader ->
                val backendInterface = loader.loadClass("com.monkey.ultimatebot.common.metrics.MetricsBackend")
                val factoryInterface = loader.loadClass("com.monkey.ultimatebot.common.metrics.MetricsBackendFactory")
                val contextClass = loader.loadClass("com.monkey.ultimatebot.common.metrics.MetricsBackendContext")
                val context = contextClass
                    .getConstructor(
                        Boolean::class.javaPrimitiveType,
                        String::class.java,
                        String::class.java,
                        IntSupplier::class.java,
                        LongSupplier::class.java,
                        LongSupplier::class.java,
                        LongSupplier::class.java,
                        LongSupplier::class.java,
                    ).newInstance(
                        true,
                        releaseVersion.get(),
                        "verification",
                        IntSupplier { 1 },
                        LongSupplier { 2L },
                        LongSupplier { 3L },
                        LongSupplier { 4L },
                        LongSupplier { 5L },
                    )
                val factoryClass = addonLoader.loadClass("com.monkey.ultimatebot.metrics.addon.MicrometerMetricsBackendFactory")
                check(factoryInterface.isAssignableFrom(factoryClass)) {
                    "Metrics addon factory does not implement the SPI from the obfuscated main jar"
                }
                val factory = factoryClass.getConstructor().newInstance()
                val backend = factoryInterface.getMethod("create", contextClass).invoke(factory, context)
                try {
                    backendInterface
                        .getMethod(
                            "recordRemoteRequest",
                            String::class.java,
                            String::class.java,
                            Int::class.javaPrimitiveType,
                            Long::class.javaPrimitiveType,
                        ).invoke(backend, "GET", "/verify", 200, 1_000_000L)
                    val scrape = backendInterface.getMethod("scrape").invoke(backend) as String
                    check("ultimatebot_remote_requests_seconds_count" in scrape) {
                        "Metrics addon scrape did not contain the verification request"
                    }
                } finally {
                    backendInterface.getMethod("close").invoke(backend)
                }
            }

            val guardJar = guardAddonJarTask.get().archiveFile.get().asFile
            URLClassLoader(arrayOf(guardJar.toURI().toURL()), loader).use { guardLoader ->
                val guardFactoryInterface = loader.loadClass("com.monkey.ultimatebot.common.guard.GuardBackendFactory")
                val guardFactoryClass =
                    guardLoader.loadClass("com.monkey.ultimatebot.guard.addon.UltimateBotGuardBackendFactory")
                check(guardFactoryInterface.isAssignableFrom(guardFactoryClass)) {
                    "Guard addon factory does not implement the SPI from the obfuscated main jar"
                }
            }
        }
    }
}

shadowJarTask.configure {
    finalizedBy(obfuscatePluginJarTask)
}

obfuscatePluginJarTask.configure {
    finalizedBy(verifyObfuscatedPluginJarTask)
}

tasks.named("build") {
    dependsOn(shadowJarTask)
}
