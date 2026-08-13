import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Paper/Spigot 1.17.0 (Craft v1_17_R1) fake-player bridge.
 *
 * <p>paperweight-userdev only ships {@code 1.17.1+} bundles. Compiles against CodeMC's
 * Mojang-mapped Spigot 1.17 jar and reobfuscates with Spigot's official 1.17 maps so runtime names
 * match Paper 1.17 (e.g. {@code getCurrentItemAttackStrengthDelay} → {@code fB}, not 1.17.1's
 * {@code fC}).
 *
 * <p>SpecialSource {@code -l}/{@code --live} enables ClassLoader inheritance lookup (it does
 * <em>not</em> take a jar path). The matching server jar must be on the JavaExec classpath so
 * inherited NMS methods (e.g. {@code setItemSlot} → {@code setSlot}) remap correctly.
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

val maps: Configuration = configurations.create("maps")
val specialSource: Configuration = configurations.create("specialSource")
val mojangServer: Configuration = configurations.create("mojangServer")
val obfServer: Configuration = configurations.create("obfServer")

dependencies {
    compileOnly("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")
    compileOnly(project(":core"))
    compileOnly(project(":common"))
    compileOnly("org.jspecify:jspecify:1.0.1")
    compileOnly("com.google.errorprone:error_prone_annotations:2.11.0")

    mojangServer("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
    obfServer("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-obf")

    maps("org.spigotmc:minecraft-server:1.17-R0.1-SNAPSHOT:maps-mojang@txt")
    maps("org.spigotmc:minecraft-server:1.17-R0.1-SNAPSHOT:maps-spigot@csrg")
    maps("org.spigotmc:minecraft-server:1.17-R0.1-SNAPSHOT:maps-spigot-fields@csrg")
    specialSource("net.md-5:SpecialSource:1.11.4")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-classfile")
    // Spigot remapped-mojang exposes Mojang names as auxiliary classes; do not fail the build.
    options.compilerArgs.add("-Xlint:-auxiliaryclass")
    options.compilerArgs.removeAll { it == "-Werror" }
}

val reobfDir = layout.buildDirectory.dir("reobf")
val mojangToObf = reobfDir.map { it.file("mojang-to-obf.jar") }
val reobfOutput = layout.buildDirectory.file("libs/${project.name}-reobf.jar")

val reobfMojangToObf =
    tasks.register<JavaExec>("reobfMojangToObf") {
        group = "build"
        dependsOn(tasks.jar)
        // Server jar on classpath → --live can resolve Player/LivingEntity parents.
        classpath = specialSource + mojangServer
        mainClass.set("net.md_5.specialsource.SpecialSource")
        inputs.files(tasks.jar, maps, mojangServer)
        outputs.file(mojangToObf)
        doFirst {
            val mojangMaps = maps.files.first { it.name.contains("maps-mojang") }
            args(
                "-i",
                tasks.jar.get().archiveFile.get().asFile.absolutePath,
                "-o",
                mojangToObf.get().asFile.absolutePath,
                "-m",
                mojangMaps.absolutePath,
                "--reverse",
                "--live",
            )
            mojangToObf.get().asFile.parentFile.mkdirs()
        }
    }

val reobfJar =
    tasks.register<JavaExec>("reobfJar") {
        group = "build"
        description = "Reobfuscate Mojang-mapped classes to Spigot 1.17 names"
        dependsOn(reobfMojangToObf)
        // Obf server on classpath → --live resolves bkd→att→atf for inherited methods.
        classpath = specialSource + obfServer
        mainClass.set("net.md_5.specialsource.SpecialSource")
        inputs.files(reobfMojangToObf, maps, obfServer)
        outputs.file(reobfOutput)
        doFirst {
            val spigotMaps =
                maps.files.first { it.name.contains("maps-spigot.csrg") && !it.name.contains("fields") }
            val spigotFieldMaps = maps.files.first { it.name.contains("maps-spigot-fields") }
            args(
                "-i",
                mojangToObf.get().asFile.absolutePath,
                "-o",
                reobfOutput.get().asFile.absolutePath,
                "-m",
                spigotMaps.absolutePath,
                "-m",
                spigotFieldMaps.absolutePath,
                "--live",
            )
            reobfOutput.get().asFile.parentFile.mkdirs()
        }
    }

configurations {
    create("reobf") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
}

artifacts {
    add("reobf", reobfOutput) {
        builtBy(reobfJar)
    }
}
