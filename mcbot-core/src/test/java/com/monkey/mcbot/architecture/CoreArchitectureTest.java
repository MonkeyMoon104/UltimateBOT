package com.monkey.mcbot.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.monkey.mcbot",
        importOptions = ImportOption.DoNotIncludeTests.class,
        cacheMode = CacheMode.PER_CLASS)
class CoreArchitectureTest {
    @ArchTest
    static final ArchRule CONFIGURATION_MUST_NOT_DEPEND_ON_RUNTIME_FEATURES = noClasses()
            .that()
            .resideInAPackage("..config..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..bot..", "..gui..", "..listener..", "..remote..")
            .because("configuration loading must remain reusable and side-effect free");

    @ArchTest
    static final ArchRule CONTROLLERS_MUST_NOT_DEPEND_ON_GUI = noClasses()
            .that()
            .resideInAPackage("..bot.ai.controllers..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..gui..")
            .because("combat and movement logic must remain independent from presentation");
}
