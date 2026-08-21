import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.file.DuplicatesStrategy
import java.net.URLClassLoader
import java.security.MessageDigest
import java.util.HexFormat
import java.util.function.IntSupplier
import java.util.function.LongSupplier
import java.util.zip.ZipFile

plugins {
    id("ultimatebot.java")
    alias(libs.plugins.shadow)
}

// Maintainer-only: ultimatebot.libs-mirror.gradle.kts is gitignored and may be absent on clones.
val libsMirrorPluginScript =
    rootProject.file("build-logic/src/main/kotlin/ultimatebot.libs-mirror.gradle.kts")
if (libsMirrorPluginScript.isFile) {
    apply(plugin = "ultimatebot.libs-mirror")
}

ultimatebotJava {
    injectReleaseArg.set(false)
    release.set(17)
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations.matching { it.isCanBeResolved }.configureEach {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

tasks.withType<ShadowJar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching(listOf("META-INF/services/**", "META-INF/*.kotlin_module")) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

fun libConfiguration(name: String) = configurations.create(name) {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

val libJackson = libConfiguration("libJackson")
val libLamp = libConfiguration("libLamp")
val libConfigurate = libConfiguration("libConfigurate")
val libPathetic = libConfiguration("libPathetic")
val libBstats = libConfiguration("libBstats")
val libCaffeineLegacy = libConfiguration("libCaffeineLegacy")
val libCaffeineModern = libConfiguration("libCaffeineModern")
val libInvuiV1 = libConfiguration("libInvuiV1")
val libInvuiV2_1 = libConfiguration("libInvuiV2_1")
val libInvuiV2_2 = libConfiguration("libInvuiV2_2")

val shadowJarTask = tasks.named<ShadowJar>("shadowJar")
val pluginJarFile = layout.buildDirectory.file("libs/UltimateBot.jar")

evaluationDependsOn(":addons:metrics")
evaluationDependsOn(":addons:guard")
evaluationDependsOn(":core")
val metricsAddonJarTask = project(":addons:metrics").tasks.named<ShadowJar>("shadowJar")
val guardAddonJarTask = project(":addons:guard").tasks.named<Jar>("jar")
val metricsAddonDescriptorFile = layout.buildDirectory.file("generated/addons/metrics.properties")
val guardAddonDescriptorFile = layout.buildDirectory.file("generated/addons/guard.properties")
val libraryDescriptorDir = layout.buildDirectory.dir("generated/libs")
val releaseVersion = providers.environmentVariable("RELEASE_VERSION").orElse(project.version.toString())

data class LibrarySpec(
    val id: String,
    val track: String,
    val keyClass: String,
    val configuration: Configuration,
)

fun mavenArtifactPath(artifact: ResolvedArtifact): String {
    val module = artifact.moduleVersion.id
    val groupPath = module.group.replace('.', '/')
    val classifier = artifact.classifier
    val fileName = if (classifier.isNullOrBlank()) {
        "${module.name}-${module.version}.jar"
    } else {
        "${module.name}-${module.version}-$classifier.jar"
    }
    return "$groupPath/${module.name}/${module.version}/$fileName"
}

fun repositoryBasesFor(group: String): List<String> {
    val ordered = linkedSetOf<String>()
    when {
        group.startsWith("xyz.xenondevs") -> ordered += "https://repo.xenondevs.xyz/releases"
        group.startsWith("org.spongepowered") -> ordered += "https://repo.spongepowered.org/maven"
        group.startsWith("io.papermc") -> ordered += "https://repo.papermc.io/repository/maven-public/"
        group.startsWith("com.github.sirblobman") || group.startsWith("com.sirblobman") ->
            ordered += "https://nexus.sirblobman.xyz/public/"
        group.startsWith("me.clip") ->
            ordered += "https://repo.extendedclip.com/content/repositories/placeholderapi/"
        group.startsWith("com.sk89q") -> ordered += "https://maven.enginehub.org/repo/"
    }

    val preferredMirror = System.getenv("ULTIMATEBOT_LIBS_MIRROR")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: "https://repo.monkeymoon104.it/releases"
    ordered += preferredMirror.trimEnd('/')

    ordered += "https://maven-central.storage-download.googleapis.com/maven2"
    ordered += "https://repo.maven.apache.org/maven2"
    ordered += "https://repo1.maven.org/maven2"
    ordered += "https://repo.papermc.io/repository/maven-public/"
    ordered += "https://repo.codemc.io/repository/maven-releases/"
    ordered += "https://repo.spongepowered.org/maven"
    ordered += "https://repo.xenondevs.xyz/releases"
    return ordered.toList()
}

fun mavenDownloadUrls(artifact: ResolvedArtifact): List<String> {
    val path = mavenArtifactPath(artifact)
    return repositoryBasesFor(artifact.moduleVersion.id.group).map { base ->
        val normalized = base.trimEnd('/')
        "$normalized/$path"
    }
}

fun sha256Hex(file: java.io.File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(16_384)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            if (read > 0) digest.update(buffer, 0, read)
        }
    }
    return HexFormat.of().formatHex(digest.digest())
}

fun writeLibraryDescriptor(spec: LibrarySpec, outputDir: java.io.File) {
    val artifacts = spec.configuration.resolvedConfiguration.resolvedArtifacts
        .filter { it.type == "jar" || it.extension == "jar" }
        .filterNot { it.name.endsWith("-sources") || it.name.endsWith("-javadoc") }
        .filter { it.file.isFile && it.file.length() > 0L }
        .sortedBy { it.file.name }
    check(artifacts.isNotEmpty()) { "Library ${spec.id} resolved zero jars" }

    val builder = StringBuilder()
    builder.append("track=").append(spec.track).append('\n')
    builder.append("key-class=").append(spec.keyClass).append('\n')
    builder.append("jar-count=").append(artifacts.size).append('\n')
    artifacts.forEachIndexed { index, artifact ->
        val file = artifact.file
        val urls = mavenDownloadUrls(artifact)
        builder.append("jar.").append(index).append(".file=").append(file.name).append('\n')
        builder.append("jar.").append(index).append(".url-count=").append(urls.size).append('\n')
        urls.forEachIndexed { urlIndex, url ->
            builder.append("jar.").append(index).append(".url.").append(urlIndex).append('=').append(url).append('\n')
        }
        builder.append("jar.").append(index).append(".sha256=").append(sha256Hex(file)).append('\n')
        builder.append("jar.").append(index).append(".size=").append(file.length()).append('\n')
    }
    outputDir.mkdirs()
    outputDir.resolve("${spec.id}.properties").writeText(builder.toString())
}

val generateMetricsAddonDescriptorTask = tasks.register("generateMetricsAddonDescriptor") {
    dependsOn(metricsAddonJarTask)
    inputs.file(metricsAddonJarTask.flatMap { it.archiveFile })
    inputs.property("releaseVersion", releaseVersion)
    outputs.file(metricsAddonDescriptorFile)

    doLast {
        val addonJar = metricsAddonJarTask.get().archiveFile.get().asFile
        val version = releaseVersion.get()
        val descriptor = metricsAddonDescriptorFile.get().asFile
        descriptor.parentFile.mkdirs()
        descriptor.writeText(
            """
            version=$version
            url=https://repo.monkeymoon104.it/releases/com/monkey/ultimatebot/ultimatebot-metrics/$version/ultimatebot-metrics-$version.jar
            sha256=${sha256Hex(addonJar)}
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
        val version = releaseVersion.get()
        val descriptor = guardAddonDescriptorFile.get().asFile
        descriptor.parentFile.mkdirs()
        descriptor.writeText(
            """
            version=$version
            url=https://repo.monkeymoon104.it/releases/com/monkey/ultimatebot/ultimatebot-guard/$version/ultimatebot-guard-$version.jar
            sha256=${sha256Hex(addonJar)}
            size=${addonJar.length()}
            factory-class=com.monkey.ultimatebot.guard.addon.UltimateBotGuardBackendFactory
            """.trimIndent() + "\n",
        )
    }
}

val generateLibraryDescriptorsTask = tasks.register("generateLibraryDescriptors") {
    inputs.files(
        libJackson, libLamp, libConfigurate, libPathetic, libBstats,
        libCaffeineLegacy, libCaffeineModern, libInvuiV1, libInvuiV2_1, libInvuiV2_2,
    )
    outputs.dir(libraryDescriptorDir)

    doLast {
        val out = libraryDescriptorDir.get().asFile
        out.mkdirs()
        val specs = listOf(
            LibrarySpec("jackson", "modern", "com.monkey.ultimatebot.libs.jackson.databind.ObjectMapper", libJackson),
            LibrarySpec("lamp", "modern", "com.monkey.ultimatebot.libs.lamp.bukkit.BukkitLamp", libLamp),
            LibrarySpec(
                "configurate",
                "modern",
                "com.monkey.ultimatebot.libs.configurate.yaml.YamlConfigurationLoader",
                libConfigurate,
            ),
            LibrarySpec(
                "pathetic",
                "modern",
                "com.monkey.ultimatebot.libs.pathetic.api.pathing.Pathfinder",
                libPathetic,
            ),
            LibrarySpec("bstats", "modern", "com.monkey.ultimatebot.libs.bstats.bukkit.Metrics", libBstats),
            LibrarySpec(
                "caffeine-legacy",
                "legacy",
                "com.monkey.ultimatebot.libs.caffeine.cache.Caffeine",
                libCaffeineLegacy,
            ),
            LibrarySpec(
                "caffeine-modern",
                "modern",
                "com.monkey.ultimatebot.libs.caffeine.cache.Caffeine",
                libCaffeineModern,
            ),
            LibrarySpec("invui-v1", "legacy", "com.monkey.ultimatebot.libs.invui.gui.Gui", libInvuiV1),
            LibrarySpec("invui-v2-1", "modern", "com.monkey.ultimatebot.libs.invui.gui.Gui", libInvuiV2_1),
            LibrarySpec("invui-v2-2", "modern", "com.monkey.ultimatebot.libs.invui.gui.Gui", libInvuiV2_2),
        )
        specs.forEach { writeLibraryDescriptor(it, out) }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(path = ":NMS:v1_7_R4", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_8_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_8_R2", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_8_R3", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_9_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_9_R2", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_10_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_11_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_12_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_13_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_13_R2", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_14_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_15_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_16_R1", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_16_R2", configuration = "runtimeElements")) {
        isTransitive = false
    }
    implementation(project(path = ":NMS:v1_16_R3", configuration = "runtimeElements")) {
        isTransitive = false
    }
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

    add(libJackson.name, libsCatalog.findLibrary("jackson-databind").get())
    add(libJackson.name, libsCatalog.findLibrary("jackson-datatype-jsr310").get())
    add(libLamp.name, libsCatalog.findLibrary("lamp-bukkit").get())
    add(libConfigurate.name, libsCatalog.findLibrary("configurate-yaml").get())
    add(libPathetic.name, libsCatalog.findLibrary("pathetic-engine").get())
    add(libBstats.name, libsCatalog.findLibrary("bstats-bukkit").get())
    add(libCaffeineLegacy.name, libsCatalog.findLibrary("caffeine-legacy").get())
    add(libCaffeineModern.name, libsCatalog.findLibrary("caffeine-modern").get())
    add(libInvuiV1.name, libsCatalog.findLibrary("invui-v1").get())
    add(libInvuiV2_1.name, libsCatalog.findLibrary("invui-v2-1").get())
    add(libInvuiV2_2.name, libsCatalog.findLibrary("invui-v2-2").get())
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(
        generateMetricsAddonDescriptorTask,
        generateGuardAddonDescriptorTask,
        generateLibraryDescriptorsTask,
    )
    archiveFileName.set("UltimateBot.jar")
    mergeServiceFiles()
    // Only jar-relocator + ASM stay in the thin jar (needed in onLoad before other libs).
    relocate("me.lucko.jarrelocator", "com.monkey.ultimatebot.libs.relocator")
    relocate("org.objectweb.asm", "com.monkey.ultimatebot.libs.asm")
    from(metricsAddonDescriptorFile) {
        into("META-INF/ultimatebot/addons")
    }
    from(guardAddonDescriptorFile) {
        into("META-INF/ultimatebot/addons")
    }
    from(libraryDescriptorDir) {
        into("META-INF/ultimatebot/libs")
    }
}

val verifyPluginJarTask = tasks.register("verifyPluginJar") {
    group = "verification"
    description = "Smoke-tests reflective dependencies, library descriptors, and optional addons against UltimateBot.jar."
    dependsOn(shadowJarTask, metricsAddonJarTask, guardAddonJarTask)

    doLast {
        val pluginJar = pluginJarFile.get().asFile
        check(pluginJar.isFile) {
            "Plugin jar not found: ${pluginJar.absolutePath}"
        }

        ZipFile(pluginJar).use { zip ->
            val requiredDescriptors = listOf(
                "META-INF/ultimatebot/libs/jackson.properties",
                "META-INF/ultimatebot/libs/lamp.properties",
                "META-INF/ultimatebot/libs/configurate.properties",
                "META-INF/ultimatebot/libs/pathetic.properties",
                "META-INF/ultimatebot/libs/bstats.properties",
                "META-INF/ultimatebot/libs/caffeine-legacy.properties",
                "META-INF/ultimatebot/libs/caffeine-modern.properties",
                "META-INF/ultimatebot/libs/invui-v1.properties",
                "META-INF/ultimatebot/libs/invui-v2-1.properties",
                "META-INF/ultimatebot/libs/invui-v2-2.properties",
            )
            requiredDescriptors.forEach { path ->
                check(zip.getEntry(path) != null) { "Missing library descriptor in jar: $path" }
            }
            val jacksonDescriptor = zip.getEntry("META-INF/ultimatebot/libs/jackson.properties")
            check(jacksonDescriptor != null)
            zip.getInputStream(jacksonDescriptor).bufferedReader().use { reader ->
                val text = reader.readText()
                check(text.contains("key-class=com.monkey.ultimatebot.libs.jackson.databind.ObjectMapper")) {
                    "jackson descriptor must use relocated key-class"
                }
            }
            val hasRelocator = zip.entries().asSequence().any {
                it.name.startsWith("com/monkey/ultimatebot/libs/relocator/")
            }
            check(hasRelocator) { "Plugin jar must shade jar-relocator under libs.relocator" }
            val relocatedLeak = zip.entries().asSequence()
                .map { it.name }
                .filter { name ->
                    name.startsWith("com/monkey/ultimatebot/cafe2/") ||
                        name.startsWith("com/monkey/ultimatebot/cafe3/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/invui/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/jackson/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/lamp/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/configurate/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/pathetic/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/bstats/") ||
                        name.startsWith("com/monkey/ultimatebot/libs/caffeine/") ||
                        name.startsWith("me/lucko/jarrelocator/") ||
                        name.startsWith("com/github/benmanes/caffeine/") ||
                        name.startsWith("xyz/xenondevs/invui/") ||
                        name.startsWith("com/fasterxml/jackson/") ||
                        name.startsWith("revxrsal/commands/")
                }
                .take(5)
                .toList()
            check(relocatedLeak.isEmpty()) {
                "Plugin jar still contains shaded/third-party library classes, e.g. $relocatedLeak"
            }
        }

        URLClassLoader(arrayOf(pluginJar.toURI().toURL()), ClassLoader.getPlatformClassLoader()).use { loader ->
            loader.loadClass("com.monkey.ultimatebot.common.lib.LibraryLoader")
            loader.loadClass("com.monkey.ultimatebot.lib.RuntimeLibraryBootstrap")

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
                    "Metrics addon factory does not implement the SPI from the main jar"
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
                    "Guard addon factory does not implement the SPI from the main jar"
                }
            }
        }
    }
}

shadowJarTask.configure {
    finalizedBy(verifyPluginJarTask)
}

tasks.named("build") {
    dependsOn(shadowJarTask)
}
