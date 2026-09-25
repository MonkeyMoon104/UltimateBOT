plugins {
    id("ultimatebot.nms")
}

ultimatebotJava {
    release.set(16)
}

ultimatebotPaperweight {
    catalog("paper-bundle-1_17_1")
    launcherVersion.set(17)
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.50.0")
}
