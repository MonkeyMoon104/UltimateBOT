tasks.register("moduleBuildStep") {
    group = "verification"
    description = "No-op build step for the empty addons aggregator."
    doLast {
        logger.lifecycle("SUCCESS ${project.path}")
    }
}
