package com.swsnowball.dragonreborn.client.ui;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.data.DragonDataManager;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class UIManager {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final long CHECK_INTERVAL = 20; // 检查间隔(ms)
    private static long lastCheckTime = 0;

    public static void tick() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCheckTime < CHECK_INTERVAL) {
            return;
        }

        lastCheckTime = currentTime;

        if (MC.player == null || MC.level == null) {
            DragonDataUI.update(null, null);
            return;
        }

        // 获取玩家正在看的实体
        HitResult hitResult = MC.hitResult;

        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();

            // 检查是否是龙
            if (target instanceof EntityDragonBase dragon) {
                // 检查龙是否被驯服
                if (dragon.isTame() && dragon.getOwner() != null) {
                    // 获取数据
                    DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
                    DragonDataUI.update(dragon, data);
                    return;
                }
            }
        }

        // 没有看龙，隐藏UI
        DragonDataUI.update(null, null);
    }
}