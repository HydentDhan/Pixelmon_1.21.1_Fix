package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.pokemon.boss.BossTierRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class GolemBossFix {

    @SubscribeEvent
    public void onBossGolemSpawn(EntityJoinLevelEvent event) {
        try {
            if (event.getEntity() instanceof PixelmonEntity pixelmon) {
                String name = pixelmon.getPokemon().getSpecies().getName().toLowerCase();

                if ((name.equals("golurk") || name.equals("melmetal")) && pixelmon.isBossPokemon()) {
                    pixelmon.setBossTier(BossTierRegistry.getBossTierOrNotBoss("not_boss"));
                    pixelmon.setHealth(pixelmon.getMaxHealth());
                    pixelmon.removeAllEffects();
                    pixelmon.getPokemon().setPalette("none");
                }
            }//
        } catch (Exception e) {
        }
    }
}