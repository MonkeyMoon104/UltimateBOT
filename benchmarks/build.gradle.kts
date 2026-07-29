import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    alias(libs.plugins.jmh)
}

val resolvedJmhVersion =
    extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")
        .findVersion("jmh")
        .get()
        .requiredVersion

dependencies {
    implementation(project(":mcbot-sdk"))
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.generator.annprocess)
}

jmh {
    jmhVersion.set(resolvedJmhVersion)
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(2)
    failOnError.set(true)
    resultFormat.set("JSON")
}

tasks.named("check") {
    dependsOn("jmhClasses")
}

tasks.named<JavaCompile>("compileJmhJava") {
    options.errorprone.disable("ThreadPriorityCheck")
}
