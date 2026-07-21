import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.paperweight.userdev)
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.named("reobfJar") {
    enabled = false
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-26_1").get().requiredVersion)
    compileOnly(project(":mcbot-core"))
    implementation(libsCatalog.findLibrary("invui-v2-1").get())
}
