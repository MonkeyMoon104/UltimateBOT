import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.paperweight.userdev)
}

java {
    // Need JDK 25 to read Paper 26 APIs; emitted bytecode is 25 and major-normalized in shadowJar.
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    // Paper 26 + InvUI 2 need Java 21+ APIs (SequencedCollection). Emit 25; shadowJar
    // normalizeClassMajor(61) keeps Commodore on 1.17.1 happy (classes load only on 26.x).
    options.release.set(25)
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.named("reobfJar") {
    enabled = false
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-26_2").get().requiredVersion)
    compileOnly(project(":core"))
    compileOnly(project(":common"))
    compileOnly(project(":api"))
    implementation(libsCatalog.findLibrary("invui-v2-2").get())
}
