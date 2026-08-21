package com.monkey.ultimatebot.common;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.monkey.ultimatebot.common",
        importOptions = ImportOption.DoNotIncludeTests.class,
        cacheMode = CacheMode.PER_CLASS)
class CommonArchitectureTest {
    @ArchTest
    static final ArchRule COMMON_MUST_REMAIN_PLATFORM_INDEPENDENT = noClasses()
            .that()
            .resideInAPackage("..common..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.bukkit..", "io.papermc..", "net.minecraft..", "com.mojang..")
            .because("common contracts must be usable without a Minecraft server runtime");

    @ArchTest
    static final ArchRule COMMON_MUST_REMAIN_DEPENDENCY_FREE = noClasses()
            .that()
            .resideInAPackage("..common..")
            .should()
            .dependOnClassesThat()
            .resideOutsideOfPackages(
                    "java..",
                    "javax..",
                    "org.jspecify..",
                    "com.monkey.ultimatebot.common..",
                    "me.lucko.jarrelocator..",
                    "org.objectweb.asm..",
                    "sun.misc..")
            .because("common production code may only use the JDK, JSpecify, jar-relocator (+ ASM), and Unsafe for classpath injection");
}
