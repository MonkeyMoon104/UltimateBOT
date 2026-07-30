-dontshrink
-dontoptimize

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Record,NestHost,NestMembers,PermittedSubclasses,MethodParameters

-keep class com.monkey.mcbot.MinecraftBot { *; }

-keep class com.monkey.mcbot.api.** { *; }
-keep class com.monkey.mcbot.common.** { *; }

-keep class com.monkey.mcbot.remote.** { *; }

# Public SPI used by the optional metrics addon loaded from an isolated class loader.

-keep class com.monkey.mcbot.nms.NMSBridge_v* { *; }

-keep class com.monkey.mcbot.nms.INMSBridge { *; }
-keep class com.monkey.mcbot.nms.NMSBridgeManager { *; }

-keepclassmembernames class com.monkey.mcbot.bot.BotOptions { *; }
-keepclassmembernames class com.monkey.mcbot.bot.BotType { *; }
-keepclassmembernames class com.monkey.mcbot.bot.BotCreationSource { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.BotAI { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.BotAI$* { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.ITrainingBot { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.TrainingBotLogic { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.TrainingBot_v* { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.TrainingBot_v26_* { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.fakeplayer.** { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.rank.** { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.services.** { *; }
-keepclassmembernames class com.monkey.mcbot.bot.ai.controllers.** { *; }

-keepclassmembernames class com.monkey.mcbot.gui.** { *; }
-keepclassmembernames class com.monkey.mcbot.utils.armor.** { *; }

-keep class xyz.xenondevs.invui.** { *; }
-keep class xyz.xenondevs.inventoryaccess.** { *; }
-keep class com.monkey.mcbot.libs.invui.v1.** { *; }
-keep class com.monkey.mcbot.libs.inventoryaccess.v1.** { *; }
-keep class com.monkey.mcbot.libs.invui.a1.** { *; }
-keep class com.monkey.mcbot.libs.inventoryaccess.a1.** { *; }
-keep class com.monkey.mcbot.libs.invui.a2.** { *; }
-keep class com.monkey.mcbot.libs.inventoryaccess.a2.** { *; }

-keep class com.fasterxml.jackson.** { *; }

-keep class com.monkey.mcbot.libs.bstats.** { *; }

# Caffeine selects generated cache and node implementations by their runtime class names.
# Obfuscating this package breaks LocalCacheFactory/NodeFactory reflective lookups.
-keep class com.monkey.mcbot.libs.caffeine.cache.** { *; }

# Keep the shaded pathfinding engine stable for its extension interfaces and reflective smoke test.
-keep class com.monkey.mcbot.libs.pathetic.** { *; }

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
