package com.swsnowball.dragonreborn.event;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.network.SyncDizzinessPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.*;

@Mod.EventBusSubscriber(modid = "dragonreborn")
public class DragonDizzinessHandler {
    private static final int UPDATE_INTERVAL = 5; // 每5tick更新一次

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                level.getAllEntities().forEach(entity -> {
                    long gameTime = level.getGameTime();
                    // 每5tick执行一次
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
            int newDizziness = Math.max(0, dizziness - 5); // 每次减少5刻
            setDragonDizziness(dragon, newDizziness);

            // 应用药水效果（缓慢IX）
            MobEffectInstance effect = new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    10,            // 持续25刻
                    9,              // 放大器9（缓慢IX）
                    false, true, true
            );
            dragon.addEffect(effect);

            // 强制停止飞行
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
