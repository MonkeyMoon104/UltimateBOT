plugins {
    java
}

group = "com.example.ultimatebot"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.monkeymoon104.it/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.monkey.ultimatebot:api:2.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
