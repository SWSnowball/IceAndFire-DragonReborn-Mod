package com.swsnowball.dragonreborn.client;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.network.PetDragonPacket;
import com.swsnowball.dragonreborn.network.StopPettingPacket;
import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dragonreborn", value = Dist.CLIENT)
public class ClientInteractionHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.PET_DRAGON_KEY.consumeClick()) {
            EntityDragonBase dragon = DragonInteractionUtil.unifiedClientDetection();
            if (dragon != null) {
                int dragonId = dragon.getId();

                // --- 核心：本地开关判断 ---
                if (SimplePettingAnimation.hasAnimation(dragonId)) {
                    // 情况1：本地有动画 -> 发“停止请求”包
                    DragonRebornMod.NETWORK.sendToServer(new StopPettingPacket(dragonId));
                } else {
                    // 情况2：本地没动画 -> 发“开始请求”包
                    DragonRebornMod.NETWORK.sendToServer(new PetDragonPacket(dragonId));
                }
            }
        }
    }
}