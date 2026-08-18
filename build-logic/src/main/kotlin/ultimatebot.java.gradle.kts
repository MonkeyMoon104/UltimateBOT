import com.diffplug.gradle.spotless.SpotlessExtension
import com.monkey.ultimatebot.gradle.UltimateBotJavaExtension
import com.monkey.ultimatebot.gradle.configureUltimateBotJava
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

plugins {
    id("ultimatebot.base")
    java
    id("net.ltgt.errorprone")
}

val ultimatebotJava = extensions.create<UltimateBotJavaExtension>("ultimatebotJava")
ultimatebotJava.release.convention(8)
ultimatebotJava.testRelease.convention(21)
ultimatebotJava.toolchain.convention(21)
ultimatebotJava.injectReleaseArg.convention(true)
ultimatebotJava.nullAway.convention(true)
ultimatebotJava.werror.convention(true)

configureUltimateBotJava(ultimatebotJava)

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    add("errorprone", catalog.findLibrary("errorprone-core").get())
    add("errorprone", catalog.findLibrary("nullaway").get())
}

extensions.configure<SpotlessExtension> {
    java {
        target("src/**/*.java")
        palantirJavaFormat()
        formatAnnotations()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
