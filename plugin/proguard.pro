-dontshrink
-dontoptimize

# Preserve metadata used by Bukkit listeners, stack traces and modern Java class features.
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Record,NestHost,NestMembers,PermittedSubclasses,MethodParameters

# Keep the Bukkit entrypoint declared in plugin.yml.
-keep class com.monkey.mcbot.MinecraftBot { *; }

# Keep the public API untouched for third-party plugin compatibility.
-keep class com.monkey.mcbot.api.** { *; }

# Keep version-specific bridge classes because NMSBridgeManager loads them by name.
-keep class com.monkey.mcbot.nms.NMSBridge_v* { *; }

# Keep the seam shared directly between the core and version modules.
-keep class com.monkey.mcbot.nms.INMSBridge { *; }
-keep class com.monkey.mcbot.nms.NMSBridgeManager { *; }

# Allow more internal runtime types to be obfuscated while preserving member
# names where enum/string contracts or cross-package calls exist.
-keepclassmembernames class com.monkey.mcbot.bot.BotOptions { *; }
-keepclassmembernames class com.monkey.mcbot.bot.BotType { *; }
-keepclassmembernames class com.monkey.mcbot.bot.BotCreationSource { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.BotAI { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.BotAI$* { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.ITrainingBot { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.TrainingBotLogic { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.TrainingBot_v* { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.TrainingBot_v26_1* { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.fakeplayer.** { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.rank.** { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.services.** { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.controllers.** { *; }

# GUI and armor helpers can also be obfuscated; member names stay stable for
# enum/material/config driven behavior.
-keepclassmembernames class com.monkey.mcbot.gui.** { *; }
-keepclassmembernames class com.monkey.mcbot.utils.armor.** { *; }

# Keep shaded Xenondevs GUI/runtime libraries intact because they resolve
# version-specific implementations through hardcoded reflection strings.
-keep class xyz.xenondevs.invui.** { *; }
-keep class xyz.xenondevs.inventoryaccess.** { *; }
-keep class com.monkey.mcbot.libs.invui.v1.** { *; }
-keep class com.monkey.mcbot.libs.inventoryaccess.v1.** { *; }
-keep class com.monkey.mcbot.libs.invui.v2.** { *; }
-keep class com.monkey.mcbot.libs.inventoryaccess.v2.** { *; }

# Keep Jackson intact. The licensing client uses ObjectMapper at startup and
# Jackson relies on enum metadata/lookup methods that break under aggressive
# obfuscation.
-keep class com.fasterxml.jackson.** { *; }

# Keep relocated bStats classes stable. Metrics starts during bootstrap and is
# not worth obfuscating.
-keep class com.monkey.mcbot.libs.bstats.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Optional or server-provided libraries are not bundled in the final jar.
-dontwarn org.bukkit.**
-dontwarn io.papermc.**
-dontwarn net.minecraft.**
-dontwarn com.mojang.**
-dontwarn me.clip.placeholderapi.**
-dontwarn com.github.sirblobman.**
-dontwarn org.jetbrains.annotations.**
-dontwarn org.intellij.lang.annotations.**
-ignorewarnings
