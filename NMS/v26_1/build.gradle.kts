import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("ultimatebot.nms.mojmap")
}

ultimatebotPaperweight {
    catalog("paper-bundle-26_1")
}

val invui = extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("invui-v2-1").get()

dependencies {
    implementation(invui)
}
