pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.monkeymoon104.it/releases")
    }
}

rootProject.name = "UltimateBot"

include("api")
include("sdk")
include("common")
include("core")

include("addons")
include("addons:metrics")
include("addons:guard")

include("NMS:v1_21_4")
include("NMS:v1_21_5")
include("NMS:v1_21_6")
include("NMS:v1_21_7")
include("NMS:v1_21_8")
include("NMS:v1_21_9")
include("NMS:v1_21_10")
include("NMS:v1_21_11")
include("NMS:v26_1")
include("NMS:v26_2")

include("buildLogic")
