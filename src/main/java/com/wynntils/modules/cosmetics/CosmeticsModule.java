/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.cosmetics;

import com.wynntils.McIf;
import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.interfaces.annotations.ModuleInfo;
import com.wynntils.modules.cosmetics.configs.CosmeticsConfig;
//import com.wynntils.modules.cosmetics.layers.LayerCape;
//import com.wynntils.modules.cosmetics.layers.LayerElytra;
//import com.wynntils.modules.cosmetics.layers.LayerFoxEars;
//TODO Uncomment and fix imports above^^
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.player.PlayerModelPart;

@ModuleInfo(name = "capes", displayName = "Capes")
public class CosmeticsModule extends Module {

    public void onEnable() {
        registerSettings(CosmeticsConfig.class);
    }

    public void postEnable() {
        McIf.mc().options.setModelPart(PlayerModelPart.CAPE, true);

        for (PlayerRenderer render : McIf.mc().getEntityRenderDispatcher().getSkinMap().values()) {
//            render.addLayer(new LayerCape(render));
//            render.addLayer(new LayerElytra(render));
//            render.addLayer(new LayerFoxEars(render));
        }
    }

}
