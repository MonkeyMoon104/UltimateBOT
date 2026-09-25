plugins {
    id("ultimatebot.nms.legacy")
}

ultimatebotJava {
    release.set(16)
}

val maps: Configuration = configurations.create("maps")
val specialSource: Configuration = configurations.create("specialSource")
val mojangServer: Configuration = configurations.create("mojangServer")
val obfServer: Configuration = configurations.create("obfServer")

dependencies {
    compileOnly("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")
    compileOnly("com.google.errorprone:error_prone_annotations:2.50.0")

    mojangServer("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
    obfServer("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-obf")

    maps("org.spigotmc:minecraft-server:1.17-R0.1-SNAPSHOT:maps-mojang@txt")
    maps("org.spigotmc:minecraft-server:1.17-R0.1-SNAPSHOT:maps-spigot@csrg")
    maps("org.spigotmc:minecraft-server:1.17-R0.1-SNAPSHOT:maps-spigot-fields@csrg")
    specialSource("net.md-5:SpecialSource:1.11.6")
}

tasks.withType<JavaCompile>().configureEach {
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
