package com.pix.pixelmonfix;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.listener.PixelmonStatisticsPixelmonListener;
import com.pix.pixelmonfix.commands.PixelmonFixCommand;
import com.pix.pixelmonfix.commands.FixDexCommand;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.fixes.CaptureFix;
import com.pix.pixelmonfix.fixes.ExpBarFix;
import com.pix.pixelmonfix.fixes.PokedexFix;
import com.pix.pixelmonfix.fixes.RaidHelperFix;
import com.pix.pixelmonfix.fixes.CurryCrashFix;
import com.pix.pixelmonfix.fixes.ResearchPersistenceFix;
import com.pix.pixelmonfix.fixes.ResearchTasksFix;
import com.pix.pixelmonfix.fixes.GolemBossFix;
import com.pix.pixelmonfix.fixes.TradePokedexFix;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("pixelmonfix")
public class PixelmonFixMain {

    public static final Logger LOGGER = LogManager.getLogger("PixelmonFix");

    public PixelmonFixMain(IEventBus modEventBus) {
        LOGGER.info("Initializing Custom Pixelmon 1.21.1 Server Fixes...");

        ConfigManager.loadConfig();

        modEventBus.addListener(this::onCommonSetup);

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

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Pixelmon.EVENT_BUS.unregister(PixelmonStatisticsPixelmonListener.class);
            } catch (Exception e) {
            }
        });
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        PixelmonFixCommand.register(event.getDispatcher());
        FixDexCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onMysteryBoxMineSpeed(PlayerEvent.BreakSpeed event) {
        try {
            String blockName = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString().toLowerCase();
            if (blockName.contains("mystery_box")) {
                ItemStack tool = event.getEntity().getMainHandItem();

                if (tool.is(ItemTags.PICKAXES) || tool.is(ItemTags.AXES)) {
                    event.setNewSpeed(5.0F);
                }
            }
        } catch (Exception e) {
        }
    }

    @SubscribeEvent
    public void onMysteryBoxBreak(BlockEvent.BreakEvent event) {
        try {
            if (event.getPlayer().isCreative() || event.getLevel().isClientSide()) return;

            String blockName = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString().toLowerCase();

            if (blockName.contains("mystery_box")) {
                Item boxItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse("pixelmon:mystery_box"));

                if (boxItem != null && boxItem != Items.AIR) {
                    ItemEntity dropEntity = new ItemEntity(
                            (ServerLevel) event.getLevel(),
                            event.getPos().getX() + 0.5,
                            event.getPos().getY() + 0.5,
                            event.getPos().getZ() + 0.5,
                            new ItemStack(boxItem)
                    );
                    event.getLevel().addFreshEntity(dropEntity);
                }
            }
        } catch (Exception e) {
        }
    }
}