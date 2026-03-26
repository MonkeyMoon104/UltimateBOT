-dontshrink
-dontoptimize

# Preserve metadata used by Bukkit listeners, stack traces and modern Java class features.
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Record,NestHost,NestMembers,PermittedSubclasses,MethodParameters
-keepdirectories

# Keep the Bukkit entrypoint declared in plugin.yml.
-keep class com.monkey.mcbot.MinecraftBot { *; }

# Keep the public API untouched for third-party plugin compatibility.
-keep class com.monkey.mcbot.api.** { *; }

# Keep version-specific bridge classes because NMSBridgeManager loads them by name.
-keep class com.monkey.mcbot.nms.NMSBridge_v* { *; }

# Keep version module implementation classes readable and stable.
-keep class com.monkey.mcbot.bot.ai.TrainingBot_v* { *; }

# Keep the seam shared directly between the core and version modules.
-keep class com.monkey.mcbot.nms.INMSBridge { *; }
-keep class com.monkey.mcbot.nms.NMSBridgeManager { *; }
-keep class com.monkey.mcbot.bot.BotOptions { *; }
-keep class com.monkey.mcbot.bot.ai.ITrainingBot { *; }
-keep class com.monkey.mcbot.bot.ai.controllers.brain.BotBrainController { *; }
-keep class com.monkey.mcbot.bot.ai.fakeplayer.BotCraftPlayer { *; }
-keep class com.monkey.mcbot.bot.ai.services.TotemTrackerService { *; }

# Keep the AI/runtime package stable. Version-specific implementations call
# into these classes directly, and several enums rely on name()/valueOf().
-keep class com.monkey.mcbot.bot.ai.** { *; }

# Keep GUI and armor utilities stable because they rely on enum names and
# material/config lookups that break when symbols are renamed.
-keep class com.monkey.mcbot.gui.** { *; }
-keep class com.monkey.mcbot.utils.armor.** { *; }

# Keep core enums and option types whose names are part of runtime behavior.
-keep class com.monkey.mcbot.bot.BotOptions { *; }
-keep class com.monkey.mcbot.bot.BotType { *; }
-keep class com.monkey.mcbot.bot.BotCreationSource { *; }

# Keep shaded Xenondevs GUI/runtime libraries intact because they resolve
# version-specific implementations through hardcoded reflection strings.
-keep class xyz.xenondevs.invui.** { *; }
-keep class xyz.xenondevs.inventoryaccess.** { *; }

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
