-dontshrink
-dontoptimize

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Record,NestHost,NestMembers,PermittedSubclasses,MethodParameters

-keep class com.monkey.ultimatebot.UltimateBot { *; }

-keep class com.monkey.ultimatebot.api.** { *; }
-keep class com.monkey.ultimatebot.common.** { *; }

-keep class com.monkey.ultimatebot.remote.** { *; }

# Public SPI used by the optional metrics addon loaded from an isolated class loader.

-keep class com.monkey.ultimatebot.nms.NMSBridge_v* { *; }

-keep class com.monkey.ultimatebot.nms.INMSBridge { *; }
-keep class com.monkey.ultimatebot.nms.NMSBridgeManager { *; }
-keep class com.monkey.ultimatebot.bot.ai.services.cache.Caffeine2UuidCache { *; }
-keep class com.monkey.ultimatebot.bot.ai.services.cache.Caffeine3UuidCache { *; }

-keepclassmembernames class com.monkey.ultimatebot.bot.BotOptions { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.BotType { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.BotCreationSource { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.BotAI { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.BotAI$* { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.ITrainingBot { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.TrainingBotLogic { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.TrainingBot_v* { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.TrainingBot_v26_* { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.fakeplayer.** { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.difficulty.** { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.services.** { *; }
-keepclassmembernames class com.monkey.ultimatebot.bot.ai.controllers.** { *; }

-keepclassmembernames class com.monkey.ultimatebot.gui.** { *; }
-keepclassmembernames class com.monkey.ultimatebot.utils.armor.** { *; }

-keep class xyz.xenondevs.invui.** { *; }
-keep class xyz.xenondevs.inventoryaccess.** { *; }
-keep class com.monkey.ultimatebot.libs.invui.v1.** { *; }
-keep class com.monkey.ultimatebot.libs.inventoryaccess.v1.** { *; }
-keep class com.monkey.ultimatebot.libs.invui.a1.** { *; }
-keep class com.monkey.ultimatebot.libs.inventoryaccess.a1.** { *; }
-keep class com.monkey.ultimatebot.libs.invui.a2.** { *; }
-keep class com.monkey.ultimatebot.libs.inventoryaccess.a2.** { *; }

-keep class com.fasterxml.jackson.** { *; }
-keep class com.monkey.ultimatebot.libs.jackson.** { *; }

-keep class com.monkey.ultimatebot.libs.bstats.** { *; }

# Caffeine selects generated cache/node implementations by runtime class name.
# Dual shaded lines: cafe2 = Java 8, cafe3 = Java 11+.
-keep class com.monkey.ultimatebot.cafe2.cache.** { *; }
-keep class com.monkey.ultimatebot.cafe3.cache.** { *; }
-keep class com.monkey.ultimatebot.libs.caffeine.cache.** { *; }

# Keep the shaded pathfinding engine stable for its extension interfaces and reflective smoke test.
-keep class com.monkey.ultimatebot.libs.pathetic.** { *; }
-keep class com.monkey.ultimatebot.libs.lamp.** { *; }
-keep class com.monkey.ultimatebot.libs.configurate.** { *; }
-keep class com.monkey.ultimatebot.libs.geantyref.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn org.bukkit.**
-dontwarn io.papermc.**
-dontwarn net.minecraft.**
-dontwarn com.mojang.**
-dontwarn me.clip.placeholderapi.**
-dontwarn com.github.sirblobman.**
-dontwarn org.jetbrains.annotations.**
-dontwarn org.intellij.lang.annotations.**
-ignorewarnings
