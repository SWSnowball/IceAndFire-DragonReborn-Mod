package com.swsnowball.dragonreborn.client;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.network.PetDragonPacket;
import com.swsnowball.dragonreborn.network.StopPettingPacket;
import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "dragonreborn", value = Dist.CLIENT)
public class ClientInteractionHandler {

    public static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.PET_DRAGON_KEY.consumeClick()) {
            LOGGER.info("[Client] 抚摸键被按下");
            Entity original_dragon = DragonInteractionUtil.unifiedClientDetection();
            LOGGER.info("[Client] 检测到的实体: " + (original_dragon == null ? "null" : original_dragon.getClass().getSimpleName() + " ID=" + original_dragon.getId()));
            Entity dragon = null;
            int dragonPartId = -1;
            if (original_dragon instanceof EntityDragonPart) {
                LOGGER.info("获得实体为龙体");
                dragon = ((EntityDragonPart) original_dragon).getParent();
                dragonPartId = original_dragon.getId();
            } else if (original_dragon instanceof EntityDragonBase) {
                LOGGER.info("获得实体为龙");
                dragon = original_dragon;
            }
            if (dragon != null) {
                int dragonId = dragon.getId();

                // 本地开关判断
                if (SimplePettingAnimation.hasAnimation(dragonId)) {
                    // 本地有动画发“停止请求”包
                    DragonRebornMod.NETWORK.sendToServer(new StopPettingPacket(dragonId, dragonPartId));
                } else {
                    // 本地没动画发“开始请求”包
                    DragonRebornMod.NETWORK.sendToServer(new PetDragonPacket(dragonId, dragonPartId));
                }
            }
        }
    }
}