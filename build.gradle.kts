import io.papermc.paperweight.tasks.JavaLauncherTask
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

fun File.normalizePublishedHtml() {
    walkTopDown().filter { it.isFile && it.extension == "html" }.forEach { html ->
        val original = html.readText(Charsets.UTF_8)
        val normalized = original.replace(Regex("[\\t ]+(?=\\r?\\n)"), "")
        if (normalized != original) {
            html.writeText(normalized, Charsets.UTF_8)
        }
    }
}

fun Project.publishDeveloperPortal(
    product: String,
    docsVersion: String,
) {
    val docsRoot = layout.projectDirectory.dir("docs").asFile
    val siteSource = docsRoot.resolve("site-src")
    val productOutput = docsRoot.resolve(product)
    val generatedIndex = productOutput.resolve("index.html")
    check(generatedIndex.isFile) { "Generated $product Javadoc index is missing" }

    productOutput.normalizePublishedHtml()
    generatedIndex.copyTo(productOutput.resolve("reference.html"), overwrite = true)
    copy {
        from(siteSource.resolve("assets"))
        into(docsRoot.resolve("assets"))
    }

    fun render(
        template: File,
        output: File,
    ) {
        val rendered = template.readText(Charsets.UTF_8).replace("{{VERSION}}", docsVersion)
        check("{{VERSION}}" !in rendered) { "Unresolved documentation version in ${template.name}" }
        output.writeText(rendered, Charsets.UTF_8)
    }

    render(siteSource.resolve("index.html"), docsRoot.resolve("index.html"))
    render(siteSource.resolve(product).resolve("index.html"), generatedIndex)
}

plugins {
    java
    alias(libs.plugins.paperweight.userdev) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.errorprone) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.revapi) apply false
}

val resolvedProjectVersion = providers.gradleProperty("ultimatebot.version").get()

version = resolvedProjectVersion

repositories {
    mavenCentral()
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("repositoryMetadata") {
        target(".gitignore", "gradle/*.toml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "net.ltgt.errorprone")
    apply(plugin = "com.diffplug.spotless")

    group = "com.monkey.ultimatebot"
    version = resolvedProjectVersion

    repositories {
        mavenCentral()
        maven("https://repo.monkeymoon104.it/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.xenondevs.xyz/releases")
        maven("https://repo.spongepowered.org/maven")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://nexus.sirblobman.xyz/public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    extensions.configure<JavaPluginExtension> {
        // Compile with JDK 21+ so Paper 1.21+/26 APIs resolve; emit Java 8 for common/core
        // (MC 1.8 floor / Java 8 JVMs). NMS modules keep a higher --release and load only on
        // matching modern servers.
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // Do not set options.release at configuration time: Gradle would stamp TargetJvmVersion
        // on compileClasspath and reject Paper APIs that require 21+.
        // Inject --release in doFirst so dependency resolution stays on the toolchain JVM.
        options.release.unset()
        options.compilerArgs.addAll(
                // -Xlint:-classfile: deps may reference ElementType.MODULE (Java 9+); --release 8
            // cannot resolve that enum constant and would fail under -Werror.
            listOf(
                "-parameters",
                "-Xlint:all",
                "-Xlint:-processing",
                "-Xlint:-options",
                "-Xlint:-classfile",
                "-Xlint:-deprecation",
                "-Werror"))
        options.errorprone {
            allSuggestionsAsWarnings.set(false)
            check("NullAway", CheckSeverity.ERROR)
            check("RequireExplicitNullMarking", CheckSeverity.OFF)
            // Java 8 emission: keep checks that assume newer language features off.
            check("PatternMatchingInstanceof", CheckSeverity.OFF)
            check("StatementSwitchToExpressionSwitch", CheckSeverity.OFF)
            // Record→class desugar left class-level @param tags; method @params stay documented in source.
            check("InvalidParam", CheckSeverity.OFF)
            option("NullAway:OnlyNullMarked", "true")
            option("NullAway:JSpecifyMode", "true")
        }
        doFirst {
            val path = project.path
            if (path == ":NMS:v26_1" || path == ":NMS:v26_2") {
                return@doFirst
            }
            val releaseTarget =
                when {
                    // Legacy Spigot NMS (v1_*_R*): emit Java 8 so Class.forName works on Java 8 JVMs.
                    path.matches(Regex(""":NMS:v1_\d+_R\d+""")) -> "8"
                    // Paper 1.17 / 1.17.1 run on Java 16.
                    path == ":NMS:v1_17" || path == ":NMS:v1_17_1" -> "16"
                    path.startsWith(":NMS:") -> "17"
                    // Phase 1: shared modules (api/common/core/sdk/addons) emit Java 8 for the MC 1.8 floor.
                    else -> "8"
                }
            val args = options.compilerArgs
            val cleaned = ArrayList<String>(args.size)
            var skipNext = false
            for (arg in args) {
                if (skipNext) {
                    skipNext = false
                    continue
                }
                if (arg == "--release") {
                    skipNext = true
                    continue
                }
                cleaned.add(arg)
            }
            args.clear()
            args.addAll(listOf("--release", releaseTarget))
            args.addAll(cleaned)
            // jspecify @NullMarked @Target includes ElementType.MODULE; --release 8 cannot resolve it.
            // -Xlint:-classfile / -Xlint:none do not suppress that diagnostic; -nowarn does, while
            // Error Prone / NullAway still report real issues (NullAway remains ERROR).
            if (releaseTarget == "8") {
                args.removeAll(listOf("-Werror"))
                args.add("-Xlint:-classfile")
                args.add("-nowarn")
            }
        }
    }

    // it.monkeymoon104.api-publish forces options.release 21 at config time; clear it so
    // compileClasspath TargetJvmVersion stays on the toolchain (21+). v26 sets its own release.
    afterEvaluate {
        if (path == ":NMS:v26_1" || path == ":NMS:v26_2") {
            return@afterEvaluate
        }
        tasks.withType<JavaCompile>().configureEach {
            options.release.unset()
        }
    }

    tasks.withType<Test>().configureEach {
        jvmArgs("--enable-native-access=ALL-UNNAMED", "-Xshare:off")
    }

    dependencies {
        add("errorprone", rootProject.libs.errorprone.core)
        add("errorprone", rootProject.libs.nullaway)
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/**/*.java")
            palantirJavaFormat()
            formatAnnotations()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }

        kotlinGradle {
            target("*.gradle.kts", "**/*.gradle.kts")
            ktlint().editorConfigOverride(mapOf("ktlint_standard_property-naming" to "disabled"))
            trimTrailingWhitespace()
            endWithNewline()
        }

        format("resources") {
            target("src/**/*.yml", "src/**/*.yaml", "src/**/*.json")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    plugins.withId("io.papermc.paperweight.userdev") {
        val toolchains = extensions.getByType<JavaToolchainService>()
        // Pre-1.19.3 paperweight setup needs JDK 17 for reproducible decomp/patching.
        val targetVersion =
            when (project.path) {
                ":NMS:v26_1", ":NMS:v26_2" -> 25
                ":NMS:v1_17_1", ":NMS:v1_18_1", ":NMS:v1_18_2", ":NMS:v1_19", ":NMS:v1_19_2" -> 17
                else -> 21
            }

        tasks.withType<JavaLauncherTask>().configureEach {
            launcher.set(
                toolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(targetVersion))
                },
            )
        }
    }

    val modulePath = path

    tasks.register("moduleBuildStep") {
        group = "verification"
        description = "Builds $modulePath and prints a module success marker."
        dependsOn(tasks.named("build"))
        doLast {
            logger.lifecycle("SUCCESS $modulePath")
        }
    }
}

tasks.register("moduleBuildSteps") {
    group = "verification"
    description = "Runs each module build as a visible success step."
    dependsOn(subprojects.map { it.tasks.named("moduleBuildStep") })
    doLast {
        logger.lifecycle("SUCCESS all modules")
    }
}

tasks.named("build") {
    dependsOn("moduleBuildSteps")
}

tasks.register("verifyDocsPortalSources") {
    group = "verification"
    description = "Validates the maintained API and SDK developer portal sources."
    inputs.dir(layout.projectDirectory.dir("docs/site-src"))

    doLast {
        val siteSource = layout.projectDirectory.dir("docs/site-src").asFile
        val requiredFiles =
            listOf(
                siteSource.resolve("index.html"),
                siteSource.resolve("api/index.html"),
                siteSource.resolve("sdk/index.html"),
                siteSource.resolve("assets/docs.css"),
                siteSource.resolve("assets/docs.js"),
            )
        requiredFiles.forEach { file -> check(file.isFile && file.length() > 0L) { "Missing docs portal source: $file" } }
        requiredFiles.filter { it.extension == "html" }.forEach { file ->
            val source = file.readText(Charsets.UTF_8)
            check("{{VERSION}}" in source) { "Documentation version placeholder is missing from $file" }
            check("<main" in source && "</main>" in source) { "Documentation main landmark is missing from $file" }
        }
    }
}

tasks.register<Sync>("publishApiDocs") {
    group = "documentation"
    description = "Generates API Javadocs and copies them to docs/api/."

    dependsOn(":api:javadoc", "verifyDocsPortalSources")
    inputs.dir(layout.projectDirectory.dir("docs/site-src"))
    from(project(":api").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/api"))

    doLast {
        project.publishDeveloperPortal("api", resolvedProjectVersion)
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register<Sync>("publishCommonDocs") {
    group = "documentation"
    description = "Generates shared public-contract Javadocs and copies them to docs/common/."

    dependsOn(":common:javadoc")
    from(project(":common").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/common"))

    doLast {
        layout.projectDirectory
            .dir("docs/common")
            .asFile
            .normalizePublishedHtml()
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register<Sync>("publishSdkDocs") {
    group = "documentation"
    description = "Generates SDK Javadocs and copies them to docs/sdk/."

    dependsOn(":sdk:javadoc", "verifyDocsPortalSources")
    inputs.dir(layout.projectDirectory.dir("docs/site-src"))
    from(project(":sdk").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/sdk"))

    doLast {
        project.publishDeveloperPortal("sdk", resolvedProjectVersion)
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register("publishAllDocs") {
    group = "documentation"
    description = "Generates and copies every public Javadoc site."
    dependsOn("publishApiDocs", "publishCommonDocs", "publishSdkDocs")
}

if (layout.projectDirectory
        .dir("docs/site-src")
        .asFile.isDirectory
) {
    tasks.named("build") {
        dependsOn("publishAllDocs")
    }
}
