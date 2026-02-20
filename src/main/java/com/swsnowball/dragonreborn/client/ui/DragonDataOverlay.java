package com.swsnowball.dragonreborn.client.ui;

import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dragonreborn", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DragonDataOverlay {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            UIManager.tick();
        }
    }

    public static final IGuiOverlay OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().options.hideGui && gui instanceof ForgeGui) {
            DragonDataUI.render(guiGraphics, partialTick);
        }
    };
}