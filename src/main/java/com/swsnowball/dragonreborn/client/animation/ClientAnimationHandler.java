package com.swsnowball.dragonreborn.client.animation;

import com.swsnowball.dragonreborn.client.SimplePettingAnimation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dragonreborn", value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientAnimationHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 1. 更新所有抚摸动画
            SimplePettingAnimation.updateAllAnimations();
        }
    }
}