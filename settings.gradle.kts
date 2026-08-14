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

include("NMS:v1_16_R1")
include("NMS:v1_16_R2")
include("NMS:v1_16_R3")
include("NMS:v1_17")
include("NMS:v1_17_1")
include("NMS:v1_18_1")
include("NMS:v1_18_2")
include("NMS:v1_19")
include("NMS:v1_19_2")
include("NMS:v1_19_3")
include("NMS:v1_19_4")
include("NMS:v1_20")
include("NMS:v1_20_1")
include("NMS:v1_20_2")
include("NMS:v1_20_4")
include("NMS:v1_20_6")
include("NMS:v1_21_1")
include("NMS:v1_21_3")
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
