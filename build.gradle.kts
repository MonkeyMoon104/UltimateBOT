fun File.normalizePublishedHtml() {
    walkTopDown().filter { it.isFile && it.extension == "html" }.forEach { html ->
        val original = html.readText(Charsets.UTF_8)
        val normalized = original.replace(Regex("[\\t ]+(?=\\r?\\n)"), "")
        if (normalized != original) {
            html.writeText(normalized, Charsets.UTF_8)
        }
    }
}

fun Project.publishDeveloperPortal(
    product: String,
    docsVersion: String,
) {
    val docsRoot = layout.projectDirectory.dir("docs").asFile
    val siteSource = docsRoot.resolve("site-src")
    val productOutput = docsRoot.resolve(product)
    val generatedIndex = productOutput.resolve("index.html")
    check(generatedIndex.isFile) { "Generated $product Javadoc index is missing" }

    productOutput.normalizePublishedHtml()
    generatedIndex.copyTo(productOutput.resolve("reference.html"), overwrite = true)
    copy {
        from(siteSource.resolve("assets"))
        into(docsRoot.resolve("assets"))
    }

    fun render(
        template: File,
        output: File,
    ) {
        val rendered = template.readText(Charsets.UTF_8).replace("{{VERSION}}", docsVersion)
        check("{{VERSION}}" !in rendered) { "Unresolved documentation version in ${template.name}" }
        output.writeText(rendered, Charsets.UTF_8)
    }

    render(siteSource.resolve("index.html"), docsRoot.resolve("index.html"))
    render(siteSource.resolve(product).resolve("index.html"), generatedIndex)
}

plugins {
    base
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.revapi) apply false
}

val resolvedProjectVersion = providers.gradleProperty("ultimatebot.version").get()

version = resolvedProjectVersion

repositories {
    mavenCentral()
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("repositoryMetadata") {
        target(".gitignore", "gradle/*.toml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register("moduleBuildSteps") {
    group = "verification"
    description = "Runs each module build as a visible success step."
    dependsOn(subprojects.map { "${it.path}:moduleBuildStep" })
    doLast {
        logger.lifecycle("SUCCESS all modules")
    }
}

tasks.named("build") {
    dependsOn("moduleBuildSteps")
}

tasks.register("verifyDocsPortalSources") {
    group = "verification"
    description = "Validates the maintained API and SDK developer portal sources."
    inputs.dir(layout.projectDirectory.dir("docs/site-src"))

    doLast {
        val siteSource = layout.projectDirectory.dir("docs/site-src").asFile
        val requiredFiles =
            listOf(
                siteSource.resolve("index.html"),
                siteSource.resolve("api/index.html"),
                siteSource.resolve("sdk/index.html"),
                siteSource.resolve("assets/docs.css"),
                siteSource.resolve("assets/docs.js"),
            )
        requiredFiles.forEach { file -> check(file.isFile && file.length() > 0L) { "Missing docs portal source: $file" } }
        requiredFiles.filter { it.extension == "html" }.forEach { file ->
            val source = file.readText(Charsets.UTF_8)
            check("{{VERSION}}" in source) { "Documentation version placeholder is missing from $file" }
            check("<main" in source && "</main>" in source) { "Documentation main landmark is missing from $file" }
        }
    }
}

tasks.register<Sync>("publishApiDocs") {
    group = "documentation"
    description = "Generates API Javadocs and copies them to docs/api/."

    dependsOn(":api:javadoc", "verifyDocsPortalSources")
    inputs.dir(layout.projectDirectory.dir("docs/site-src"))
    from(project(":api").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/api"))

    doLast {
        project.publishDeveloperPortal("api", resolvedProjectVersion)
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register<Sync>("publishCommonDocs") {
    group = "documentation"
    description = "Generates shared public-contract Javadocs and copies them to docs/common/."

    dependsOn(":common:javadoc")
    from(project(":common").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/common"))

    doLast {
        layout.projectDirectory
            .dir("docs/common")
            .asFile
            .normalizePublishedHtml()
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register<Sync>("publishSdkDocs") {
    group = "documentation"
    description = "Generates SDK Javadocs and copies them to docs/sdk/."

    dependsOn(":sdk:javadoc", "verifyDocsPortalSources")
    inputs.dir(layout.projectDirectory.dir("docs/site-src"))
    from(project(":sdk").layout.buildDirectory.dir("docs/javadoc"))
    into(layout.projectDirectory.dir("docs/sdk"))

    doLast {
        project.publishDeveloperPortal("sdk", resolvedProjectVersion)
        layout.projectDirectory
            .file("docs/.nojekyll")
            .asFile
            .writeText("")
    }
}

tasks.register("publishAllDocs") {
    group = "documentation"
    description = "Generates and copies every public Javadoc site."
    dependsOn("publishApiDocs", "publishCommonDocs", "publishSdkDocs")
}

if (layout.projectDirectory
        .dir("docs/site-src")
        .asFile.isDirectory
) {
    tasks.named("build") {
        dependsOn("publishAllDocs")
    }
}
