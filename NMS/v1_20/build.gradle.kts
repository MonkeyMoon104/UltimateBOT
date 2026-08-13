import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.paperweight.userdev)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Same CraftBukkit revision as 1.20.1 (v1_20_R1); PlayerInfo Entry-list ctor differs at runtime.
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-1_20_1").get().requiredVersion)
    compileOnly(project(":core"))
    compileOnly(project(":common"))
}
