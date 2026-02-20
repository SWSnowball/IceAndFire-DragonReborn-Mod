package com.swsnowball.dragonreborn.util;

import com.github.alexthe666.iceandfire.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DragonInteractionUtil {

    // 核心方法：从任意实体获取龙的主实体
    public static EntityDragonBase getDragonBaseFromEntity(Entity entity) {
        if (entity instanceof EntityDragonBase) {
            return (EntityDragonBase) entity;
        } else if (entity instanceof EntityDragonPart) {
            // 部位实体需要获取父实体
            Entity parent = ((EntityDragonPart) entity).getParent();
            if (parent instanceof EntityDragonBase) {
                return (EntityDragonBase) parent;
            }
        }
        return null;
    }

    // ========== 客户端专用方法 ==========

    /**
     * 客户端的统一检测方法（用于抚摸功能）
     * 先尝试hitResult检测，失败则使用射线检测
     */
    public static EntityDragonBase unifiedClientDetection() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;

        EntityDragonBase dragon = null;

        // 方法1：使用hitResult检测（针对龙的数据实体）
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((net.minecraft.world.phys.EntityHitResult) mc.hitResult).getEntity();
            dragon = getDragonBaseFromEntity(target);

            if (dragon != null) {
                return dragon; // 方法1成功
            }
        }

        // 方法1失败，使用方法2：射线检测（针对龙的部位实体）
        dragon = clientRaycastDetection(mc.player, 5.0);
        return dragon;
    }

    /**
     * 客户端的射线检测（用于回退）
     */
    private static EntityDragonBase clientRaycastDetection(Player player, double range) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        double closestDistance = range * range;
        EntityDragonBase closestDragon = null;

        for (Entity entity : player.level().getEntities(player, new AABB(eyePos, endPos).inflate(1.0))) {
            EntityDragonBase dragon = getDragonBaseFromEntity(entity);
            if (dragon != null) {
                double distance = player.distanceToSqr(dragon);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestDragon = dragon;
                }
            }
        }
        return closestDragon;
    }

    // ========== 服务端专用方法 ==========

    /**
     * 服务端的统一检测方法（用于物品使用）
     * 先尝试简单射线检测，如果失败则尝试扩大检测范围
     */
    public static EntityDragonBase unifiedServerDetection(Player player, double range) {
        // 方法1：常规射线检测
        EntityDragonBase dragon = serverRaycastDetection(player, range);

        if (dragon != null) {
            return dragon; // 方法1成功
        }

        // 方法2：扩大检测范围
        return serverRaycastDetection(player, range * 1.5);
    }

    /**
     * 服务端的射线检测
     */
    private static EntityDragonBase serverRaycastDetection(Player player, double range) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        double closestDistance = range * range;
        EntityDragonBase closestDragon = null;

        for (Entity entity : player.level().getEntities(player, new AABB(eyePos, endPos).inflate(1.0))) {
            EntityDragonBase dragon = getDragonBaseFromEntity(entity);
            if (dragon != null) {
                double distance = player.distanceToSqr(dragon);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestDragon = dragon;
                }
            }
        }
        return closestDragon;
    }

    // ========== 向后兼容的方法（保持现有代码不变） ==========

    /**
     * 旧方法：用于抚摸功能（现在调用统一检测）
     */
    public static EntityDragonBase getTargetedPetEntity() {
        return unifiedClientDetection();
    }

    /**
     * 旧方法：用于物品使用（现在调用统一检测）
     */
    public static EntityDragonBase getTargetedEntity(Player player, double range) {
        return unifiedServerDetection(player, range);
    }
}