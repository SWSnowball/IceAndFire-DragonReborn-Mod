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
            if (target instanceof EntityDragonBase dragon) {
                if (dragon.isTame() && dragon.getOwner() != null) {
                    DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
                    if (data != null) {
                        DragonDataUI.update(dragon, data);
                    } else {
                        // 数据尚未同步，隐藏 UI 或显示占位符
                        DragonDataUI.update(null, null);
                    }
                    return;
                }
            }
        }
        DragonDataUI.update(null, null);
    }
}