package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.pokedex.PokedexStorage;
import com.pixelmonmod.pixelmon.api.pokedex.event.PokedexEvent;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokedex.PokeDexStorageProxy;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.utils.DiscordWebhook;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

public class PokedexFix {

    @SubscribeEvent
    public void onPokedexPre(PokedexEvent.Pre event) {
        try {
            if (!ConfigManager.CONFIG.enablePokedexFix) return;
            if (event.getNewStatus() != PokedexRegistrationStatus.CAUGHT) return;

            ServerPlayer player = event.getPlayer();
            if (player == null) return;

            Pokemon incomingPokemon = event.getPokemon();
            if (incomingPokemon == null) return;

            PokedexStorage pokedex = PokeDexStorageProxy.getStorageNow(player);
            if (pokedex == null) return;

            Species species = incomingPokemon.getSpecies();
            boolean speciesAlreadyCaught = false;

            for (var form : species.getForms()) {
                Pokemon testPoke = PokemonBuilder.builder().species(species).form(form).build();
                if (pokedex.hasCaught(testPoke)) {
                    speciesAlreadyCaught = true;
                    break;
                }
            }

            if (speciesAlreadyCaught) {
                event.setCanceled(true);
            }

        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] PokedexFix Error", e);
            DiscordWebhook.sendError("Pokedex Form Registration", e);
        }
    }
}