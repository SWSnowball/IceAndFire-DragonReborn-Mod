package com.swsnowball.dragonreborn.client.animation;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class LookToPlayerAnimation {

     // 计算龙应该朝向主人的角度
    public static class LookData {
        public final float yaw; // Yaw（偏航角）：水平旋转（左看/右看）
        public final float pitch; // Pitch（俯仰角）：垂直旋转（上看/下看）
        public final float headYaw;
        public final float headPitch;
        public final boolean hasOwner;

        public LookData(float yaw, float pitch, float headYaw, float headPitch, boolean hasOwner) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.hasOwner = hasOwner;
        }
    }

    // 在 LookToPlayerAnimation.java 中
    private static double calculateDragonEyeHeight(EntityDragonBase dragon) {
        int age = dragon.getAgeInDays();
        // 使用线性公式：y = 0.05 * age - 0.5
        double height = 0.05 * age - 0.5;
        // 确保最小高度（龙刚出生时）
        return Math.max(height, 0.3); // 最小0.3格
    }

     // 计算龙看向主人的朝向数据
    public static LookData calculateLookAtOwner(EntityDragonBase dragon) {
        LivingEntity owner = dragon.getOwner();

        if (owner == null || !owner.isAlive()) {
            return new LookData(dragon.getYRot(), dragon.getXRot(), 0, 0, false);
        }

        // 获取主人和龙的位置
        double ownerX = owner.getX();
        double ownerY = owner.getY() + owner.getEyeHeight();
        double ownerZ = owner.getZ();

        double dragonEyeHeight = calculateDragonEyeHeight(dragon);
        double dragonX = dragon.getX();
        double dragonY = dragon.getY() + dragonEyeHeight;
        double dragonZ = dragon.getZ();

        // 计算朝向角度（向量计算）
        double dx = ownerX - dragonX;
        double dy = ownerY - dragonY;
        double dz = ownerZ - dragonZ;

        // 计算水平角度 (yaw) （180 / pi）就是弧度转角度
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

        // 计算垂直角度 (pitch)
        float pitch = (float)(-(Mth.atan2(dy, horizontalDistance) * (180.0 / Math.PI)));

        // 计算头部相对于身体的旋转（限制在合理范围内）
        float headYaw = Mth.wrapDegrees(yaw - dragon.getYRot());
        float headPitch = Mth.wrapDegrees(pitch - dragon.getXRot());

        // 限制头部旋转角度
        headYaw = Mth.clamp(headYaw, -45.0F, 45.0F);
        headPitch = Mth.clamp(headPitch, -30.0F, 30.0F);

        return new LookData(yaw, pitch, headYaw, headPitch, true);
    }

     //判断是否需要旋转整个实体（当头部旋转角度过大时）
    public static boolean shouldRotateEntity(float headYaw, float headPitch) {
        // 如果头部需要旋转的角度超过阈值，则旋转整个实体
        return Math.abs(headYaw) > 40.0F || Math.abs(headPitch) > 25.0F;
    }
}