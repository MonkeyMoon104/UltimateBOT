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
import java.time.Duration
import java.util.HexFormat
import java.util.function.IntSupplier
import java.util.function.LongSupplier
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

evaluationDependsOn(":addons:metrics")
evaluationDependsOn(":addons:guard")
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
            url=https://repo.monkeymoon104.it/releases/com/monkey/mcbot/minecraftbot-metrics/$version/minecraftbot-metrics-$version.jar
            sha256=${HexFormat.of().formatHex(digest.digest())}
            size=${addonJar.length()}
            factory-class=com.monkey.mcbot.metrics.addon.MicrometerMetricsBackendFactory
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
            url=https://repo.monkeymoon104.it/releases/com/monkey/mcbot/minecraftbot-guard/$version/minecraftbot-guard-$version.jar
            sha256=${HexFormat.of().formatHex(digest.digest())}
            size=${addonJar.length()}
            factory-class=com.monkey.mcbot.guard.addon.MinecraftBotGuardBackendFactory
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
    externalClasspathFor(project(":versions:v26_2")),
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
    exclude("colors.bin")
    exclude("xyz/xenondevs/invui/util/ColorPalette.class")
    exclude("xyz/xenondevs/invui/window/CartographyWindow*.class")
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.a1")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.a1")
}

val relocateInvuiV2_2Task = tasks.register<ShadowJar>("relocateInvuiV2_2") {
    archiveFileName.set("invui-v2_2-relocated.jar")
    destinationDirectory.set(layout.buildDirectory.dir("tmp/shadow"))
    configurations = listOf(invuiV2_2Shade)
    mergeServiceFiles()
    exclude("colors.bin")
    exclude("xyz/xenondevs/invui/util/ColorPalette.class")
    exclude("xyz/xenondevs/invui/window/CartographyWindow*.class")
    relocate("xyz.xenondevs.invui", "com.monkey.mcbot.libs.invui.a2")
    relocate("xyz.xenondevs.inventoryaccess", "com.monkey.mcbot.libs.inventoryaccess.a2")
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(
        relocateInvuiV2_1Task,
        relocateInvuiV2_2Task,
        generateMetricsAddonDescriptorTask,
        generateGuardAddonDescriptorTask,
    )
    archiveFileName.set("MinecraftBot-unobfuscated.jar")
    mergeServiceFiles()
    exclude("colors.bin")
    exclude("xyz/xenondevs/invui/util/ColorPalette.class")
    exclude("xyz/xenondevs/invui/window/CartographyWindow*.class")
    exclude("com/monkey/mcbot/libs/invui/a1/util/ColorPalette.class")
    exclude("com/monkey/mcbot/libs/invui/a1/window/CartographyWindow*.class")
    exclude("com/monkey/mcbot/libs/invui/a2/util/ColorPalette.class")
    exclude("com/monkey/mcbot/libs/invui/a2/window/CartographyWindow*.class")
    from({
        zipTree(relocateInvuiV2_1Task.get().archiveFile.get().asFile)
    })
    from({
        zipTree(relocateInvuiV2_2Task.get().archiveFile.get().asFile)
    })
    from(metricsAddonDescriptorFile) {
        into("META-INF/minecraftbot/addons")
    }
    from(guardAddonDescriptorFile) {
        into("META-INF/minecraftbot/addons")
    }
    relocate("org.bstats", "com.monkey.mcbot.libs.bstats")
    relocate("com.fasterxml.jackson", "com.monkey.mcbot.libs.jackson")
    relocate("com.github.benmanes.caffeine", "com.monkey.mcbot.libs.caffeine")
    relocate("de.bsommerfeld.pathetic", "com.monkey.mcbot.libs.pathetic")
    relocate("org.spongepowered.configurate", "com.monkey.mcbot.libs.configurate")
    relocate("org.yaml.snakeyaml", "com.monkey.mcbot.libs.snakeyaml")
    relocate("io.leangen.geantyref", "com.monkey.mcbot.libs.geantyref")
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
            InvuiRetarget("com/monkey/mcbot/gui/v26_2/", "com/monkey/mcbot/libs/invui/a2/", "com/monkey/mcbot/libs/inventoryaccess/a2/"),
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
            val caffeineClass = loader.loadClass("com.monkey.mcbot.libs.caffeine.cache.Caffeine")
            val cacheClass = loader.loadClass("com.monkey.mcbot.libs.caffeine.cache.Cache")
            val builder = caffeineClass.getMethod("newBuilder").invoke(null)

            caffeineClass.getMethod("maximumSize", Long::class.javaPrimitiveType).invoke(builder, 2_048L)
            caffeineClass.getMethod("expireAfterWrite", Duration::class.java).invoke(builder, Duration.ofMillis(250))
            caffeineClass.getMethod("recordStats").invoke(builder)

            val cache = caffeineClass.getMethod("build").invoke(builder)
            cacheClass.getMethod("put", Any::class.java, Any::class.java).invoke(cache, "probe", "ok")
            val value = cacheClass.getMethod("getIfPresent", Any::class.java).invoke(cache, "probe")
            check(value == "ok") {
                "Caffeine smoke test returned an unexpected value from the obfuscated plugin jar"
            }

            val patheticFactoryClass =
                loader.loadClass("com.monkey.mcbot.libs.pathetic.engine.factory.AStarPathfinderFactory")
            patheticFactoryClass.getConstructor().newInstance()

            val addonJar = metricsAddonJarTask.get().archiveFile.get().asFile
            URLClassLoader(arrayOf(addonJar.toURI().toURL()), loader).use { addonLoader ->
                val backendInterface = loader.loadClass("com.monkey.mcbot.common.metrics.MetricsBackend")
                val factoryInterface = loader.loadClass("com.monkey.mcbot.common.metrics.MetricsBackendFactory")
                val contextClass = loader.loadClass("com.monkey.mcbot.common.metrics.MetricsBackendContext")
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
                val factoryClass = addonLoader.loadClass("com.monkey.mcbot.metrics.addon.MicrometerMetricsBackendFactory")
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
                    check("minecraftbot_remote_requests_seconds_count" in scrape) {
                        "Metrics addon scrape did not contain the verification request"
                    }
                } finally {
                    backendInterface.getMethod("close").invoke(backend)
                }
            }

            val guardJar = guardAddonJarTask.get().archiveFile.get().asFile
            URLClassLoader(arrayOf(guardJar.toURI().toURL()), loader).use { guardLoader ->
                val guardFactoryInterface = loader.loadClass("com.monkey.mcbot.common.guard.GuardBackendFactory")
                val guardFactoryClass =
                    guardLoader.loadClass("com.monkey.mcbot.guard.addon.MinecraftBotGuardBackendFactory")
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
