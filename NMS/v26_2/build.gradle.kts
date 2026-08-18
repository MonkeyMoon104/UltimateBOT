import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("ultimatebot.nms.mojmap")
}

ultimatebotPaperweight {
    catalog("paper-bundle-26_2")
}

val invui = extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary("invui-v2-2").get()

dependencies {
    implementation(invui)
}
