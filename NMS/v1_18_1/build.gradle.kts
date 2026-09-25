plugins {
    id("ultimatebot.nms")
}

ultimatebotPaperweight {
    catalog("paper-bundle-1_18_1")
    launcherVersion.set(17)
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.50.0")
}
