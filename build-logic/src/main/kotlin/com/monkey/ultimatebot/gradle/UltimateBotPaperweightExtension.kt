package com.monkey.ultimatebot.gradle

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.getByType

abstract class UltimateBotPaperweightExtension
    @Inject
    constructor(
        private val project: Project,
    ) {
        abstract val paperBundle: Property<String>
        abstract val reobfuscate: Property<Boolean>
        abstract val launcherVersion: Property<Int>

        fun catalog(versionKey: String) {
            paperBundle.set(
                project.extensions
                    .getByType<VersionCatalogsExtension>()
                    .named("libs")
                    .findVersion(versionKey)
                    .orElseThrow { IllegalArgumentException("Missing version catalog key '$versionKey'") }
                    .requiredVersion,
            )
        }
    }
