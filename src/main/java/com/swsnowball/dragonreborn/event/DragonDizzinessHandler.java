package com.swsnowball.dragonreborn.event;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.client.animation.DragonLookController;
import com.swsnowball.dragonreborn.client.animation.LookAnimationManager;
import com.swsnowball.dragonreborn.network.SyncDizzinessPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.*;

@Mod.EventBusSubscriber(modid = "dragonreborn")
public class DragonDizzinessHandler {
    private static final int UPDATE_INTERVAL = 20; // 每5tick更新一次

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                level.getAllEntities().forEach(entity -> {
                    long gameTime = level.getGameTime();
                    // 每20tick执行一次
                    if (gameTime % UPDATE_INTERVAL == 0) {
                        if (entity instanceof EntityDragonBase dragon) {
                            updateDragonDizziness(dragon);
                        }
                    }
                });
            }
        }
    }

    public static void updateDragonDizziness(EntityDragonBase dragon) {
        int dizziness = getDragonDizziness(dragon);
        if (dizziness > 0) {
            int newDizziness = Math.max(0, dizziness - 20); // 每次减少20刻
            setDragonDizziness(dragon, newDizziness);

            // 应用药水效果（缓慢IX）
            MobEffectInstance effect = new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    25,            // 持续25刻
                    9,              // 放大器9（缓慢IX）
                    false, true, true
            );
            dragon.addEffect(effect);

            // 强制停止飞行（可选）
            dragon.setHovering(false);
            dragon.setFlying(false);

            // 发送同步包
            if (!dragon.level().isClientSide) {
                SyncDizzinessPacket packet = new SyncDizzinessPacket(dragon.getId(), newDizziness);
                DragonRebornMod.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> dragon), packet);
            }
        }
    }
}
