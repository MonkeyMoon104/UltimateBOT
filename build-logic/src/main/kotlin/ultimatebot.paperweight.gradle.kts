import com.monkey.ultimatebot.gradle.UltimateBotPaperweightExtension
import io.papermc.paperweight.tasks.JavaLauncherTask
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

plugins {
    id("ultimatebot.java")
    id("io.papermc.paperweight.userdev")
}

val ultimatebotPaperweight = extensions.create<UltimateBotPaperweightExtension>("ultimatebotPaperweight")
ultimatebotPaperweight.reobfuscate.convention(true)
ultimatebotPaperweight.launcherVersion.convention(21)

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}

dependencies {
    paperweight.paperDevBundle(ultimatebotPaperweight.paperBundle)
}

val toolchains = extensions.getByType<JavaToolchainService>()

tasks.withType<JavaLauncherTask>().configureEach {
    launcher.set(
        toolchains.launcherFor {
            languageVersion.set(ultimatebotPaperweight.launcherVersion.map { JavaLanguageVersion.of(it) })
        },
    )
}

afterEvaluate {
    if (!ultimatebotPaperweight.reobfuscate.get()) {
        tasks.named("reobfJar") {
            enabled = false
        }
    }
}
