package com.monkey.ultimatebot.gradle

import org.gradle.api.provider.Property

abstract class UltimateBotJavaExtension {
    abstract val release: Property<Int>
    abstract val testRelease: Property<Int>
    abstract val toolchain: Property<Int>
    abstract val injectReleaseArg: Property<Boolean>
    abstract val nullAway: Property<Boolean>
    abstract val werror: Property<Boolean>
}
