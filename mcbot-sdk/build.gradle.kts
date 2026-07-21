plugins {
    `java-library`
    alias(libs.plugins.api.publish)
}

dependencies {
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
}
