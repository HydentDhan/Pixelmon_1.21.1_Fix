package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.pokedex.event.PokedexEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class TradePokedexFix {

    @SubscribeEvent
    public void onPokedexUpdate(PokedexEvent.Pre event) {
        try {
            if (event.isCausedByPlayerTrade() || event.isCausedByNPCTrade()) {
                event.setCanceled(true);
            }
        } catch (Exception e) {
        }
    }
}