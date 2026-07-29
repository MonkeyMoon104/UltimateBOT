plugins {
    alias(libs.plugins.api.publish)
}

group = "com.monkey.mcbot"

dependencies {
    api(libs.jspecify)
    compileOnly(libs.paper.api)
}
