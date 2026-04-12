package com.pix.pixelmonfix;

import com.pixelmonmod.pixelmon.Pixelmon;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
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
        Pixelmon.EVENT_BUS.register(new PokedexFix());
        Pixelmon.EVENT_BUS.register(new CaptureFix());
        Pixelmon.EVENT_BUS.register(new RaidHelperFix());
        Pixelmon.EVENT_BUS.register(new ExpBarFix());
        Pixelmon.EVENT_BUS.register(new CurryCrashFix());
        Pixelmon.EVENT_BUS.register(new ResearchTasksFix());
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        PixelmonFixCommand.register(event.getDispatcher());
        FixDexCommand.register(event.getDispatcher());
    }
}