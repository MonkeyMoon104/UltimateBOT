import com.monkey.ultimatebot.gradle.UltimateBotJavaExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("ultimatebot.paperweight")
}

the<UltimateBotJavaExtension>().release.set(17)

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":common"))
}
