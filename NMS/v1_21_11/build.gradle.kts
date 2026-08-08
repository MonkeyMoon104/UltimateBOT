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
    paperweight.paperDevBundle(libsCatalog.findVersion("paper-bundle-1_21_11").get().requiredVersion)
    compileOnly(project(":core"))
    compileOnly(project(":common"))
}
