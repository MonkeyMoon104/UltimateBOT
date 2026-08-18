import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("ultimatebot.java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.api.publish)
}

val releaseVersion = providers.environmentVariable("RELEASE_VERSION").orElse(project.version.toString())

dependencies {
    compileOnly(project(":common"))
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)
    testImplementation(project(":common"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Jar>("jar") {
    enabled = false
}

val metricsJar =
    tasks.named<ShadowJar>("shadowJar") {
        archiveFileName.set("UltimateBot-Metrics.jar")
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        filesMatching(listOf("META-INF/services/**", "META-INF/*.kotlin_module")) {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        mergeServiceFiles()
        minimize()
    }

tasks.named("assemble") {
    dependsOn(metricsJar)
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("mavenJava") {
        artifactId = "ultimatebot-metrics"
        version = releaseVersion.get()
        artifacts.clear()
        artifact(metricsJar)
        artifact(tasks.named("sourcesJar"))
        artifact(tasks.named("javadocJar"))
        pom.packaging = "jar"
    }
}
