import com.monkey.ultimatebot.gradle.UltimateBotJavaExtension
import com.monkey.ultimatebot.gradle.UltimateBotPaperweightExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("ultimatebot.nms")
}

the<UltimateBotJavaExtension>().release.set(25)
the<UltimateBotJavaExtension>().toolchain.set(25)
the<UltimateBotJavaExtension>().injectReleaseArg.set(false)
the<UltimateBotPaperweightExtension>().reobfuscate.set(false)
the<UltimateBotPaperweightExtension>().launcherVersion.set(25)

dependencies {
    compileOnly(project(":api"))
}
