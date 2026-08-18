import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("ultimatebot.java")
}

repositories {
    maven("https://repo.codemc.io/repository/nms/")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":common"))
    compileOnly(catalog.findLibrary("jspecify").get())
}
