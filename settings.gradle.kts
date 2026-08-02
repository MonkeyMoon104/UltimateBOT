pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.monkeymoon104.it/releases")
    }
}

rootProject.name = "UltimateBot"

include("ultimatebot-api")
include("ultimatebot-sdk")
include("common")
include("ultimatebot-core")

include("addons")
include("addons:metrics")
include("addons:guard")

include("versions:v1_21_4")
include("versions:v1_21_5")
include("versions:v1_21_6")
include("versions:v1_21_7")
include("versions:v1_21_8")
include("versions:v1_21_9")
include("versions:v1_21_10")
include("versions:v1_21_11")
include("versions:v26_1")
include("versions:v26_2")

include("plugin")
