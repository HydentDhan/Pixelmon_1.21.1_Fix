package com.pix.pixelmonfix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.pix.pixelmonfix.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PixelmonFixCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pixelmonfix")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("reload")
                        .executes(context -> {
                            ConfigManager.loadConfig();
                            context.getSource().sendSuccess(() -> Component.literal("§a[PixelmonFix] YAML Configuration reloaded!"), true);
                            return 1;
                        })
                )

                .then(Commands.literal("status")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            source.sendSuccess(() -> Component.literal("§6--- Pixelmon Fixes Status ---"), false);
                            source.sendSuccess(() -> Component.literal("Pokedex Fix: " + formatBool(ConfigManager.CONFIG.enablePokedexFix)), false);
                            source.sendSuccess(() -> Component.literal("Capture Fix: " + formatBool(ConfigManager.CONFIG.enableCaptureFix)), false);
                            source.sendSuccess(() -> Component.literal("Raid Helper Fix: " + formatBool(ConfigManager.CONFIG.enableRaidHelperFix)), false);
                            source.sendSuccess(() -> Component.literal("Exp Bar Fix: " + formatBool(ConfigManager.CONFIG.enableExpBarFix)), false);
                            return 1;
                        })
                )
        );
    }

    private static String formatBool(boolean value) {
        return value ? "§aEnabled" : "§cDisabled";
    }
}