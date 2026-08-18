plugins {
    id("ultimatebot.nms.legacy")
}

repositories {
    ivy {
        url = uri("https://maven.elmakers.com/repository")
        patternLayout {
            artifact("[organisation]/[module]/[revision]/[artifact]-[revision].[ext]")
        }
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    compileOnly("org.bukkit:craftbukkit:1.7.10-R0.1-SNAPSHOT")
}
