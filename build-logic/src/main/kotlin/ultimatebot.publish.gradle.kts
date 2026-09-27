import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.authentication.http.BasicAuthentication
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
                url.set("https://github.com/MonkeyMoon104/UltimateBOT")
                developers {
                    developer {
                        id.set("monkeymoon104")
                        name.set("MonkeyMoon104")
                    }
                }
                scm {
                    url.set("https://github.com/MonkeyMoon104/UltimateBOT")
                    connection.set("scm:git:https://github.com/MonkeyMoon104/UltimateBOT.git")
                    developerConnection.set("scm:git:ssh://git@github.com/MonkeyMoon104/UltimateBOT.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "MonkeyRepo"
            url = uri("https://repo.monkeymoon104.it/release")
            credentials {
                username = providers.gradleProperty("monkeyrepo.user").orNull
                password = providers.gradleProperty("monkeyrepo.secret").orNull
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
