import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless")
}

group = "com.monkey.ultimatebot"
version = rootProject.version

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
    maven("https://repo.monkeymoon104.it/releases")
}

extensions.configure<SpotlessExtension> {
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

val modulePath = path
val moduleBuildStep =
    tasks.register("moduleBuildStep") {
        group = "verification"
        description = "Builds $modulePath and prints a module success marker."
        doLast {
            logger.lifecycle("SUCCESS $modulePath")
        }
    }

pluginManager.withPlugin("java") {
    moduleBuildStep.configure {
        dependsOn(tasks.named("build"))
    }
}
