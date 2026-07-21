pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.monkeymoon104.it/releases")
    }
}

rootProject.name = "MinecraftBot"

include("mcbot-api")
include("mcbot-sdk")
include("mcbot-core")

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
