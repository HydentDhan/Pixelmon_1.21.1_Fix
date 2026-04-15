package com.pix.pixelmonfix;

import com.pix.pixelmonfix.fixes.*;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pix.pixelmonfix.commands.PixelmonFixCommand;
import com.pix.pixelmonfix.commands.FixDexCommand;
import com.pix.pixelmonfix.config.ConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("pixelmonfix")
public class PixelmonFixMain {

    public static final Logger LOGGER = LogManager.getLogger("PixelmonFix");

    public PixelmonFixMain() {
        LOGGER.info("Initializing Custom Pixelmon 1.21.1 Server Fixes...");

        ConfigManager.loadConfig();

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ResearchPersistenceFix());
        NeoForge.EVENT_BUS.register(new GolemBossFix());
        Pixelmon.EVENT_BUS.register(new PokedexFix());
        Pixelmon.EVENT_BUS.register(new CaptureFix());
        Pixelmon.EVENT_BUS.register(new RaidHelperFix());
        Pixelmon.EVENT_BUS.register(new ExpBarFix());
        Pixelmon.EVENT_BUS.register(new CurryCrashFix());
        Pixelmon.EVENT_BUS.register(new ResearchTasksFix());
        Pixelmon.EVENT_BUS.register(new TradePokedexFix());
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        PixelmonFixCommand.register(event.getDispatcher());
        FixDexCommand.register(event.getDispatcher());
    }

    // NEW FIX: Force Mystery Boxes to drop themselves
    @SubscribeEvent
    public void onMysteryBoxBreak(BlockEvent.BreakEvent event) {
        try {
            if (event.getPlayer().isCreative() || event.getLevel().isClientSide()) return;

            String blockName = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString().toLowerCase();

            if (blockName.contains("mystery_box")) {

                ItemStack droppedItem = new ItemStack(event.getState().getBlock().asItem());
                ItemEntity dropEntity = new ItemEntity(
                        (ServerLevel) event.getLevel(),
                        event.getPos().getX() + 0.5,
                        event.getPos().getY() + 0.5,
                        event.getPos().getZ() + 0.5,
                        droppedItem
                );
                event.getLevel().addFreshEntity(dropEntity);
            }
        } catch (Exception e) {
        }
    }
}