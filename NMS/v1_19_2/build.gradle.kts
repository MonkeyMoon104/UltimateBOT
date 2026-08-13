import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.paperweight.userdev)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    // Old bundles need a JDK ≤17 decompiler toolchain; Gradle 25 breaks patch application.
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-1_19_2").get().requiredVersion)
    compileOnly(project(":core"))
    compileOnly(project(":common"))
    compileOnly("com.google.errorprone:error_prone_annotations:2.11.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-classfile")
}
