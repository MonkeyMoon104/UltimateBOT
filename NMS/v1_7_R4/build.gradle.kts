import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Spigot 1.7.10 (NMS v1_7_R4) fake-player bridge.
 *
 * <p>paperweight-userdev only supports 1.17.1+. Compiles against the mapped CraftBukkit 1.7.10 jar
 * from maven.elmakers.com (CodeMC does not host this revision). Already Craft-mapped — no reobf.
 */
plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    // CodeMC does not host 1.7.10. Magic/elmakers still has the mapped CraftBukkit jar (no POM).
    ivy {
        url = uri("https://maven.elmakers.com/repository")
        patternLayout {
            artifact("[organisation]/[module]/[revision]/[artifact]-[revision].[ext]")
        }
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    compileOnly("org.bukkit:craftbukkit:1.7.10-R0.1-SNAPSHOT")
    compileOnly(project(":core"))
    compileOnly(project(":common"))
    compileOnly("org.jspecify:jspecify:1.0.1")
}
