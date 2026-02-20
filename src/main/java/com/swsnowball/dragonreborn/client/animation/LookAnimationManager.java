package com.swsnowball.dragonreborn.client.animation;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.isDragonAttacking;

public class LookAnimationManager {
    public static final Logger LOGGER = LogManager.getLogger();
    /**
     * 判断是否应该看向玩家
     */
    public static boolean shouldLookAtPlayer(EntityDragonBase dragon) {
        LivingEntity owner = dragon.getOwner();

        if (owner == null || dragon.isModelDead()) return false;

        // 龙是否被驯服
        if (!dragon.isTame()) {return false;}

        // 龙处于自由活动状态不看向
        if (dragon.getCommand() == 0) {return false;}

        // 龙正在游泳、爬梯子（也算作移动）
        if (dragon.isVisuallySwimming() || dragon.isVisuallyCrawling()) {return false;}

        // 检查距离（10格内）
        double distance = dragon.distanceToSqr(owner);
        if (distance > 10 * 10 && dragon.getCommand() != 2) return false; // 超过10格不看

        // 没有其他动画干扰（比如抚摸动画）
        if (com.swsnowball.dragonreborn.client.SimplePettingAnimation.hasAnimation(dragon.getId())) {
            return false;
        }

        // 龙不在特殊状态（睡觉、战斗、被主人骑乘时等）
        if (dragon.isSleeping() || dragon.isFlying() && dragon.getCommand() != 2 || isDragonAttacking(dragon) || dragon.hasPassenger(owner)) return false;

        return true;
    }
}