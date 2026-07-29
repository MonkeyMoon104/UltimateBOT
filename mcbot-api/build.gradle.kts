plugins {
    alias(libs.plugins.api.publish)
    alias(libs.plugins.revapi)
}

group = "com.monkey.mcbot"

dependencies {
    api(libs.jspecify)
    compileOnly(libs.paper.api)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

revapi {
    oldGroup.set("com.monkey.mcbot")
    oldName.set("mcbot-api")
    oldVersions.set(listOf("1.3.2"))
}
