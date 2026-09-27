import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.HexFormat

/**
 * Sync runtime library jars to MonkeyRepo Reposilite under `/libs/ultimatebot/`.
 *
 * Does not hook into `build` / `shadowJar`. Credentials come from the user Gradle home
 * (`monkeyrepo.user` / `monkeyrepo.secret` in `~/.gradle/gradle.properties`), never from the repository.
 *
 * Staging lives under `build/libs-mirror-bundle/` (covered by `build/` in .gitignore).
 *
 * Usage:
 *   ./gradlew :dist:publishLibsMirror
 *   ./gradlew :dist:publishLibsMirror -Pultimatebot.libs.mirror.dryRun=true
 *   ./gradlew :dist:verifyLibsMirror
 *
 * Alias kept for muscle memory: `:dist:bumpLibsToServer` → `publishLibsMirror`.
 */

data class MirrorArtifact(
    val group: String,
    val name: String,
    val version: String,
    val classifier: String?,
    val file: java.io.File,
) {
    val mavenPath: String
        get() {
            val groupPath = group.replace('.', '/')
            val fileName = if (classifier.isNullOrBlank()) {
                "$name-$version.jar"
            } else {
                "$name-$version-$classifier.jar"
            }
            return "$groupPath/$name/$version/$fileName"
        }

    val directoryPath: String
        get() = mavenPath.substringBeforeLast('/')

    val coordinate: String
        get() = if (classifier.isNullOrBlank()) {
            "$group:$name:$version"
        } else {
            "$group:$name:$version:$classifier"
        }
}

fun Project.mirrorProp(name: String, default: String? = null): String? {
    val fromProject = findProperty(name)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
    if (fromProject != null) {
        return fromProject
    }
    return providers.gradleProperty(name).orNull?.trim()?.takeIf { it.isNotEmpty() } ?: default
}

fun Project.mirrorFlag(name: String, default: Boolean = false): Boolean {
    val raw = mirrorProp(name) ?: return default
    return raw.equals("true", ignoreCase = true) || raw == "1" || raw.equals("yes", ignoreCase = true)
}

fun collectRuntimeMirrorArtifacts(configs: List<Configuration>): List<MirrorArtifact> {
    val byPath = linkedMapOf<String, MirrorArtifact>()
    configs.forEach { configuration ->
        configuration.resolvedConfiguration.resolvedArtifacts
            .filter { it.type == "jar" || it.extension == "jar" }
            .filterNot { it.name.endsWith("-sources") || it.name.endsWith("-javadoc") }
            .filter { it.file.isFile && it.file.length() > 0L }
            .forEach { artifact ->
                val module = artifact.moduleVersion.id
                val mirrorArtifact = MirrorArtifact(
                    group = module.group,
                    name = module.name,
                    version = module.version,
                    classifier = artifact.classifier,
                    file = artifact.file,
                )
                byPath.putIfAbsent(mirrorArtifact.mavenPath, mirrorArtifact)
            }
    }
    return byPath.values.sortedBy { it.mavenPath }
}

fun writeMinimalPom(artifact: MirrorArtifact, target: java.io.File) {
    val pom = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<project xmlns="http://maven.apache.org/POM/4.0.0"""")
        appendLine("""  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"""")
        appendLine(
            """  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">""",
        )
        appendLine("""  <modelVersion>4.0.0</modelVersion>""")
        appendLine("""  <groupId>${artifact.group}</groupId>""")
        appendLine("""  <artifactId>${artifact.name}</artifactId>""")
        appendLine("""  <version>${artifact.version}</version>""")
        appendLine("""</project>""")
    }
    target.parentFile.mkdirs()
    target.writeText(pom, StandardCharsets.UTF_8)
}

fun sha1Hex(file: java.io.File): String {
    val digest = MessageDigest.getInstance("SHA-1")
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

fun stageMirrorArtifacts(artifacts: List<MirrorArtifact>, stagingRoot: java.io.File): Set<String> {
    if (stagingRoot.exists()) {
        stagingRoot.deleteRecursively()
    }
    stagingRoot.mkdirs()
    val managed = linkedSetOf<String>()
    artifacts.forEach { artifact ->
        val jarRelative = artifact.mavenPath
        val pomRelative = "${artifact.directoryPath}/${artifact.name}-${artifact.version}.pom"
        val jarTarget = stagingRoot.resolve(jarRelative.replace('/', java.io.File.separatorChar))
        val pomTarget = stagingRoot.resolve(pomRelative.replace('/', java.io.File.separatorChar))
        jarTarget.parentFile.mkdirs()
        artifact.file.copyTo(jarTarget, overwrite = true)
        writeMinimalPom(artifact, pomTarget)
        jarTarget.resolveSibling("${jarTarget.name}.sha1").writeText(sha1Hex(jarTarget), StandardCharsets.UTF_8)
        pomTarget.resolveSibling("${pomTarget.name}.sha1").writeText(sha1Hex(pomTarget), StandardCharsets.UTF_8)
        managed += jarRelative
        managed += pomRelative
        managed += "$jarRelative.sha1"
        managed += "$pomRelative.sha1"
    }
    return managed
}

val runtimeLibConfigurationNames = listOf(
    "libJackson",
    "libLamp",
    "libConfigurate",
    "libPathetic",
    "libBstats",
    "libCaffeineLegacy",
    "libCaffeineModern",
    "libInvuiV1",
    "libInvuiV2_1",
    "libInvuiV2_2",
    "libInvuiV2_5",
)

val libsMirrorStagingDir = layout.buildDirectory.dir("libs-mirror-bundle")
val libsMirrorManifestName = ".ultimatebot-runtime-libs-manifest.txt"

fun Project.mirrorPublicBaseUrl(): String =
    mirrorProp(
        "ultimatebot.libs.mirror.publicBaseUrl",
        "https://repo.monkeymoon104.it/libs/ultimatebot",
    )!!.trimEnd('/')

fun Project.monkeyRepoBasicAuthHeader(): String {
    val user = mirrorProp("monkeyrepo.user")
        ?: error("Missing gradle property monkeyrepo.user (same credentials as :api:publish)")
    val secret = mirrorProp("monkeyrepo.secret")
        ?: error("Missing gradle property monkeyrepo.secret (same credentials as :api:publish)")
    val token = Base64.getEncoder().encodeToString("$user:$secret".toByteArray(StandardCharsets.UTF_8))
    return "Basic $token"
}

fun openMirrorConnection(url: String, method: String, authHeader: String?): HttpURLConnection {
    val connection = (URI.create(url).toURL().openConnection() as HttpURLConnection).apply {
        requestMethod = method
        instanceFollowRedirects = true
        connectTimeout = 15_000
        readTimeout = 120_000
        setRequestProperty("User-Agent", "UltimateBot-LibsMirror")
        if (authHeader != null) {
            setRequestProperty("Authorization", authHeader)
        }
    }
    return connection
}

fun httpHeadOk(url: String): Pair<Int, Long> {
    val connection = openMirrorConnection(url, "HEAD", authHeader = null)
    return try {
        val code = connection.responseCode
        val length = connection.getHeaderFieldLong("Content-Length", -1L)
        code to length
    } finally {
        connection.disconnect()
    }
}

fun httpGetBytes(url: String, authHeader: String? = null): Pair<Int, ByteArray?> {
    val connection = openMirrorConnection(url, "GET", authHeader)
    return try {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val bytes = stream?.use { it.readBytes() }
        code to bytes
    } finally {
        connection.disconnect()
    }
}

fun httpGetSha1(url: String): String {
    val (code, bytes) = httpGetBytes(url)
    check(code in 200..299 && bytes != null) { "HTTP $code for $url" }
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(bytes)
    return HexFormat.of().formatHex(digest.digest())
}

fun httpPutFile(url: String, file: java.io.File, authHeader: String): Int {
    val connection = openMirrorConnection(url, "PUT", authHeader).apply {
        doOutput = true
        setRequestProperty("Content-Type", "application/octet-stream")
        setRequestProperty("Content-Length", file.length().toString())
    }
    return try {
        file.inputStream().use { input ->
            connection.outputStream.use { output -> input.copyTo(output) }
        }
        connection.responseCode
    } finally {
        connection.disconnect()
    }
}

fun httpPutText(url: String, content: String, authHeader: String): Int {
    val bytes = content.toByteArray(StandardCharsets.UTF_8)
    val connection = openMirrorConnection(url, "PUT", authHeader).apply {
        doOutput = true
        setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
        setRequestProperty("Content-Length", bytes.size.toString())
    }
    return try {
        connection.outputStream.use { it.write(bytes) }
        connection.responseCode
    } finally {
        connection.disconnect()
    }
}

fun httpDelete(url: String, authHeader: String): Int {
    val connection = openMirrorConnection(url, "DELETE", authHeader)
    return try {
        connection.responseCode
    } finally {
        connection.disconnect()
    }
}

val prepareLibsMirrorBundle = tasks.register("prepareLibsMirrorBundle") {
    group = "publishing"
    description =
        "Internal: stages runtime library jars into build/libs-mirror-bundle (Maven layout). Prefer publishLibsMirror / verifyLibsMirror."
    outputs.dir(libsMirrorStagingDir)

    doLast {
        val runtimeLibConfigurations = runtimeLibConfigurationNames.map { configurations.getByName(it) }
        val artifacts = collectRuntimeMirrorArtifacts(runtimeLibConfigurations)
        check(artifacts.isNotEmpty()) { "No runtime library artifacts resolved" }
        val managed = stageMirrorArtifacts(artifacts, libsMirrorStagingDir.get().asFile)
        val manifest = libsMirrorStagingDir.get().asFile.resolve(libsMirrorManifestName)
        manifest.writeText(managed.sorted().joinToString("\n") + "\n", StandardCharsets.UTF_8)
        logger.lifecycle(
            "Staged ${artifacts.size} unique library jar(s) / ${managed.size} managed file(s) -> ${libsMirrorStagingDir.get().asFile}",
        )
        artifacts.forEach { artifact ->
            logger.lifecycle(" - ${artifact.coordinate} -> ${artifact.mavenPath}")
        }
    }
}

afterEvaluate {
    val runtimeLibConfigurations = runtimeLibConfigurationNames.map { configurations.getByName(it) }
    prepareLibsMirrorBundle.configure {
        inputs.files(runtimeLibConfigurations)
    }
}

val publishLibsMirror = tasks.register("publishLibsMirror") {
    group = "publishing"
    description =
        "Stage runtime libs and upload them to Reposilite at /libs/ultimatebot/ (monkeyrepo Basic auth)."
    dependsOn(prepareLibsMirrorBundle)
    inputs.dir(libsMirrorStagingDir)
    notCompatibleWithConfigurationCache("Uses HTTP credentials and remote mutation.")

    doLast {
        val publicBase = mirrorPublicBaseUrl()
        val prune = mirrorFlag("ultimatebot.libs.mirror.prune", true)
        val dryRun = mirrorFlag("ultimatebot.libs.mirror.dryRun", false)
        val auth = monkeyRepoBasicAuthHeader()

        val stagingRoot = libsMirrorStagingDir.get().asFile
        val localManifestFile = stagingRoot.resolve(libsMirrorManifestName)
        check(localManifestFile.isFile) { "Missing local mirror manifest; run prepareLibsMirrorBundle first" }
        val desiredPaths = localManifestFile.readLines(StandardCharsets.UTF_8)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toCollection(linkedSetOf())

        logger.lifecycle(
            if (dryRun) {
                "DRY-RUN publishLibsMirror -> $publicBase/"
            } else {
                "publishLibsMirror -> $publicBase/"
            },
        )

        var uploaded = 0
        desiredPaths.forEach { relative ->
            val local = stagingRoot.resolve(relative.replace('/', java.io.File.separatorChar))
            check(local.isFile) { "Staged file missing: $relative" }
            val url = "$publicBase/$relative"
            if (dryRun) {
                logger.lifecycle("DRY-RUN upload $relative")
            } else {
                val code = httpPutFile(url, local, auth)
                check(code in 200..299) { "PUT failed HTTP $code for $url" }
                uploaded++
            }
        }

        val remoteManifestUrl = "$publicBase/$libsMirrorManifestName"
        val previous = if (prune) {
            val (code, bytes) = httpGetBytes(remoteManifestUrl, auth)
            if (code in 200..299 && bytes != null) {
                bytes.toString(StandardCharsets.UTF_8)
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
            } else {
                emptySet()
            }
        } else {
            emptySet()
        }

        var pruned = 0
        if (prune) {
            val orphans = previous - desiredPaths
            orphans.forEach { relative ->
                val url = "$publicBase/$relative"
                if (dryRun) {
                    logger.lifecycle("DRY-RUN prune $relative")
                } else {
                    val code = httpDelete(url, auth)
                    if (code in 200..299 || code == 404) {
                        pruned++
                    } else {
                        logger.warn("DELETE HTTP $code for $url (continuing)")
                    }
                }
            }
        }

        val newManifest = desiredPaths.sorted().joinToString("\n") + "\n"
        if (dryRun) {
            logger.lifecycle("DRY-RUN write manifest ($libsMirrorManifestName)")
        } else {
            val code = httpPutText(remoteManifestUrl, newManifest, auth)
            check(code in 200..299) { "PUT manifest failed HTTP $code for $remoteManifestUrl" }
        }

        logger.lifecycle(
            "Mirror sync complete: ${desiredPaths.size} managed path(s), uploaded=$uploaded, pruned=$pruned, dryRun=$dryRun",
        )
        logger.lifecycle("Public base URL: $publicBase/")
        val sampleJar = desiredPaths.firstOrNull { it.endsWith(".jar") }
        if (sampleJar != null) {
            logger.lifecycle("Example: $publicBase/$sampleJar")
        }
    }
}

tasks.register("bumpLibsToServer") {
    group = "publishing"
    description = "Alias of publishLibsMirror (Reposilite /libs/ultimatebot)."
    dependsOn(publishLibsMirror)
}

tasks.register("verifyLibsMirror") {
    group = "verification"
    description =
        "HEADs/downloads every staged runtime jar from /libs/ultimatebot and checks size + SHA-1 against the local stage."
    dependsOn(prepareLibsMirrorBundle)
    inputs.dir(libsMirrorStagingDir)
    notCompatibleWithConfigurationCache("Performs live HTTP checks against the public mirror.")

    doLast {
        val publicBase = mirrorPublicBaseUrl()
        val stagingRoot = libsMirrorStagingDir.get().asFile
        val localManifestFile = stagingRoot.resolve(libsMirrorManifestName)
        check(localManifestFile.isFile) { "Missing local mirror manifest" }

        val jarPaths = localManifestFile.readLines(StandardCharsets.UTF_8)
            .map { it.trim() }
            .filter { it.endsWith(".jar") }
        check(jarPaths.isNotEmpty()) { "No jar paths in mirror manifest" }

        val failures = mutableListOf<String>()
        var ok = 0
        jarPaths.forEach { relative ->
            val local = stagingRoot.resolve(relative.replace('/', java.io.File.separatorChar))
            check(local.isFile) { "Staged jar missing: $relative" }
            val expectedSha1 = local.resolveSibling("${local.name}.sha1").readText(StandardCharsets.UTF_8).trim()
            val url = "$publicBase/$relative"
            try {
                val (headCode, headLength) = httpHeadOk(url)
                if (headCode !in 200..299) {
                    failures += "$relative -> HEAD HTTP $headCode ($url)"
                    return@forEach
                }
                if (headLength >= 0L && headLength != local.length()) {
                    failures += "$relative -> size mismatch HEAD=$headLength local=${local.length()} ($url)"
                    return@forEach
                }
                val remoteSha1 = httpGetSha1(url)
                if (!remoteSha1.equals(expectedSha1, ignoreCase = true)) {
                    failures += "$relative -> sha1 mismatch remote=$remoteSha1 local=$expectedSha1 ($url)"
                    return@forEach
                }
                ok++
                logger.lifecycle("OK $relative (${local.length()} bytes)")
            } catch (ex: Exception) {
                failures += "$relative -> ${ex.message} ($url)"
            }
        }

        logger.lifecycle("verifyLibsMirror: ok=$ok / ${jarPaths.size} against $publicBase/")
        if (failures.isNotEmpty()) {
            failures.forEach { logger.error("FAIL $it") }
            error("verifyLibsMirror failed for ${failures.size} artifact(s)")
        }
    }
}
