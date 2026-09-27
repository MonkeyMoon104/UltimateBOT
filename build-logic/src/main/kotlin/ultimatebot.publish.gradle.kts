import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

plugins {
    `java-library`
    `maven-publish`
}

System.getenv("RELEASE_VERSION")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { version = it }

extensions.getByType<JavaPluginExtension>().apply {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as? StandardJavadocDocletOptions)?.addStringOption("Xdoclint:none", "-quiet")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description ?: project.name)
            }
        }
    }
    repositories {
        maven {
            name = "localReleaseRepo"
            url = uri(rootProject.layout.buildDirectory.dir("repo/releases"))
        }
    }
}
