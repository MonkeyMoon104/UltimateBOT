import io.papermc.paperweight.tasks.JavaLauncherTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    java
    alias(libs.plugins.paperweight.userdev) apply false
    alias(libs.plugins.shadow) apply false
}

val resolvedProjectVersion = providers.gradleProperty("mcbot.version").get()

version = resolvedProjectVersion

subprojects {
    apply(plugin = "java")

    group = "com.monkey.mcbot"
    version = resolvedProjectVersion

    repositories {
        mavenCentral()
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
    }

    plugins.withId("io.papermc.paperweight.userdev") {
        val toolchains = extensions.getByType<JavaToolchainService>()
        val targetVersion = if (project.path == ":versions:v26_1" || project.path == ":versions:v26_2") 25 else 21

        tasks.withType<JavaLauncherTask>().configureEach {
            launcher.set(toolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(targetVersion))
            })
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
    description = "Generates mcbot-api Javadocs and copies them to docs/mcbot/."

    dependsOn(":mcbot-api:javadoc")
    from(project(":mcbot-api").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/mcbot"))

    doLast {
        layout.projectDirectory.file("docs/.nojekyll").asFile.writeText("")
    }
}

tasks.register<Sync>("publishSdkDocs") {
    group = "documentation"
    description = "Generates mcbot-sdk Javadocs and copies them to docs/mcbot-sdk/."

    dependsOn(":mcbot-sdk:javadoc")
    from(project(":mcbot-sdk").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/mcbot-sdk"))

    doLast {
        layout.projectDirectory.file("docs/.nojekyll").asFile.writeText("")
    }
}

tasks.register("publishAllDocs") {
    group = "documentation"
    description = "Generates and copies every public Javadoc site."
    dependsOn("publishApiDocs", "publishSdkDocs")
}
