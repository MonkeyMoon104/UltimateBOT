import io.papermc.paperweight.tasks.JavaLauncherTask
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-processing", "-Werror"))
        options.errorprone {
            allSuggestionsAsWarnings.set(true)
            check("NullAway", CheckSeverity.ERROR)
            check("RequireExplicitNullMarking", CheckSeverity.OFF)
            option("NullAway:OnlyNullMarked", "true")
            option("NullAway:JSpecifyMode", "true")
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
        val targetVersion = if (project.path == ":NMS:v26_1" || project.path == ":NMS:v26_2") 25 else 21

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

tasks.register<Sync>("publishApiDocs") {
    group = "documentation"
    description = "Generates API Javadocs and copies them to docs/api/."

    dependsOn(":api:javadoc")
    from(project(":api").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/api"))

    doLast {
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register<Sync>("publishSdkDocs") {
    group = "documentation"
    description = "Generates SDK Javadocs and copies them to docs/sdk/."

    dependsOn(":sdk:javadoc")
    from(project(":sdk").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/sdk"))

    doLast {
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register("publishAllDocs") {
    group = "documentation"
    description = "Generates and copies every public Javadoc site."
    dependsOn("publishApiDocs", "publishSdkDocs")
}
