package com.monkey.mcbot.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.monkey.mcbot.api",
        importOptions = ImportOption.DoNotIncludeTests.class,
        cacheMode = CacheMode.PER_CLASS)
class ApiArchitectureTest {
    @ArchTest
    static final ArchRule PUBLIC_API_MUST_NOT_DEPEND_ON_NMS = noClasses()
            .that()
            .resideInAPackage("..api..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("net.minecraft..")
            .because("version-specific NMS types must not leak through the public API");

    @ArchTest
    static final ArchRule MODELS_MUST_NOT_DEPEND_ON_EVENTS = noClasses()
            .that()
            .resideInAPackage("..api.model..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..api.event..")
            .because("immutable API payloads must remain independent from event dispatch");
}
