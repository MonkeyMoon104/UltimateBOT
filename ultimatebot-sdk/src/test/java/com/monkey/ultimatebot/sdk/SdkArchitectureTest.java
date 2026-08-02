package com.monkey.ultimatebot.sdk;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.monkey.ultimatebot.sdk",
        importOptions = ImportOption.DoNotIncludeTests.class,
        cacheMode = CacheMode.PER_CLASS)
class SdkArchitectureTest {
    @ArchTest
    static final ArchRule SDK_MUST_REMAIN_SERVER_INDEPENDENT = noClasses()
            .that()
            .resideInAPackage("..sdk..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.bukkit..", "net.minecraft..", "com.monkey.ultimatebot.api..", "com.monkey.ultimatebot.bot..")
            .because("the remote SDK must work in applications without Bukkit, NMS or the in-server API");
}
