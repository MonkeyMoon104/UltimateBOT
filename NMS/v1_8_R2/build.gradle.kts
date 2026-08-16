import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Spigot 1.8.3 (NMS v1_8_R2) fake-player bridge.
 *
 * <p>paperweight-userdev only supports 1.17.1+. Compiles against CodeMC's Spigot NMS jar
 * ({@code org.spigotmc:spigot:1.8.3-R0.1-SNAPSHOT}). Already Spigot-mapped — no reobf.
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
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.3-R0.1-SNAPSHOT")
    compileOnly(project(":core"))
    compileOnly(project(":common"))
    compileOnly("org.jspecify:jspecify:1.0.1")
}
