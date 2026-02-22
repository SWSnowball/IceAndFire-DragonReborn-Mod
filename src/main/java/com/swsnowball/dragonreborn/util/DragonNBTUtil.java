package com.swsnowball.dragonreborn.util;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityFireDragon;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.EntityLightningDragon;
import com.github.alexthe666.iceandfire.item.ItemSummoningCrystal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.alexthe666.iceandfire.entity.EntityDragonBase.*;

// 处理龙NBT数据的工具类
public class DragonNBTUtil {

    // 自定义NBT键
    private static final String CAN_BE_LOOT_KEY = "CanBeLoot";
    private static final String OWNER_KEY = "OwnerUUID";

    public static int getDragonType(EntityDragonBase dragon) {
        if (dragon instanceof EntityFireDragon) return 0;
        if (dragon instanceof EntityIceDragon) return 1;
        if (dragon instanceof EntityLightningDragon) return 2;
        return 0;
    }

    // 检查龙是否死亡
    public static boolean isDragonDead(EntityDragonBase dragon) {
        CompoundTag nbt = new CompoundTag();
        dragon.saveWithoutId(nbt);
        return nbt.getBoolean("ModelDead");
    }

    // 复活龙
    public static void reviveDragon(EntityDragonBase dragon) {
        CompoundTag nbt = new CompoundTag();
        dragon.saveWithoutId(nbt);

        nbt.putBoolean("ModelDead", false);
        nbt.putBoolean("NoAI", false);
        nbt.putInt("DeathStage",0);

        dragon.load(nbt);
        dragon.setNoAi(false);
    }

    public static boolean getAllowDragonToSleep(EntityDragonBase dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        // 如果没有设置过，默认允许睡眠
        if (!persistentData.contains("AllowToSleep")) {
            persistentData.putBoolean("AllowToSleep", true);
        }
        return persistentData.getBoolean("AllowToSleep");
    }

    public static void setAllowDragonToSleep(EntityDragonBase dragon, boolean choice) {
        dragon.getPersistentData().putBoolean("AllowToSleep", choice);
    }

    public static int getDragonDizziness(Entity dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        // 如果没有设置过，默认允许睡眠
        if (!persistentData.contains("DizzyTime")) {
            persistentData.putInt("DizzyTime", 0);
        }
        return persistentData.getInt("DizzyTime");
    }

    public static void setDragonDizziness(Entity dragon, int time) {
        dragon.getPersistentData().putInt("DizzyTime", time);
    }

    public static void setDragonCommand(EntityDragonBase dragon, int command) {
        CompoundTag nbt = new CompoundTag();
        dragon.saveWithoutId(nbt);

        nbt.putInt("Command", command);

        dragon.load(nbt);
    }

    protected static boolean isPlayingAttackingAnimation(EntityDragonBase dragon) {
        return dragon.getAnimation() == ANIMATION_BITE
                || dragon.getAnimation() == ANIMATION_SHAKEPREY
                || dragon.getAnimation() == ANIMATION_WINGBLAST
                || dragon.getAnimation() == ANIMATION_TAILWHACK;
    }

    public static boolean isDragonAttacking(EntityDragonBase dragon) {

        return dragon.isAttacking() || dragon.isStriking() || dragon.isTackling() || dragon.getTarget() != null || dragon.isBreathingFire() || dragon.isActuallyBreathingFire() || isPlayingAttackingAnimation(dragon);
    }

    public static boolean getDragonAggroUtil(EntityDragonBase dragon) {
        CompoundTag nbt = new CompoundTag();
        dragon.saveWithoutId(nbt);
        int result = nbt.getInt("AttackDecision");
        if (result == 1) return true;
        if (result == 0) return false;
        return false;
    }

    public static void clearDragonAggroUtil(EntityDragonBase dragon) {
        CompoundTag nbt = new CompoundTag();
        dragon.saveWithoutId(nbt);

        nbt.putInt("AttackDecision", 0);

        dragon.load(nbt);
    }

    public static void setHasReset(EntityDragonBase dragon, boolean state) {
        dragon.getPersistentData().putBoolean("HasReset", state);
    }

    public static boolean getHasReset(EntityDragonBase dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        // 如果没有设置过，默认为true
        if (!persistentData.contains("HasReset")) {
            persistentData.putBoolean("HasReset", true);
        }
        return persistentData.getBoolean("HasReset");
    }

    // 检查龙是否已被保护
    public static boolean isDragonProtected(EntityDragonBase dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        // 如果不存在CanBeLoot标签，默认未被保护（true表示可搜刮）
        if (!persistentData.contains(CAN_BE_LOOT_KEY)) {
            return false;
        }
        // CanBeLoot为false表示已保护
        return !persistentData.getBoolean(CAN_BE_LOOT_KEY);
    }

    // 保护龙
    public static void protectDragon(EntityDragonBase dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        // CanBeLoot为false表示不可搜刮（已保护）
        persistentData.putBoolean(CAN_BE_LOOT_KEY, false);
    }

    // 取消保护
    public static void unprotectDragon(EntityDragonBase dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        persistentData.putBoolean(CAN_BE_LOOT_KEY, true);
    }

    // 获取龙的CanBeLoot状态
    public static boolean getCanBeLoot(EntityDragonBase dragon) {
        CompoundTag persistentData = dragon.getPersistentData();
        // 默认返回true（可搜刮）
        return !persistentData.contains(CAN_BE_LOOT_KEY) || persistentData.getBoolean(CAN_BE_LOOT_KEY);
    }

    // 检查玩家是否是龙的主人
    public static boolean isOwner(EntityDragonBase dragon, String playerUUID) {
        CompoundTag nbt = new CompoundTag();
        dragon.saveWithoutId(nbt);

        // 冰火传说的主人标签可能是"OwnerUUID"或"Owner"
        String ownerUUID = nbt.getString("OwnerUUID");
        if (ownerUUID.isEmpty()) {
            ownerUUID = nbt.getString("Owner");
        }

        return ownerUUID.equals(playerUUID);
    }

    // 统一使用新键名
    private static final String POS_TAG_KEY = "CachedPos";

    public static List<Integer> getCrystalPosition(ItemStack crystal) {
        List<Integer> pos = new ArrayList<>(3);
        CompoundTag posTag = crystal.getTagElement(POS_TAG_KEY);
        if (posTag != null) {
            pos.add(posTag.getInt("x"));
            pos.add(posTag.getInt("y"));
            pos.add(posTag.getInt("z"));
        } else {
            // 标签不存在时，返回默认值 (0,0,0)，绝不写入 NBT
            pos.add(0);
            pos.add(0);
            pos.add(0);
        }
        return pos;
    }

    public static void setCrystalPosition(ItemStack crystal, BlockPos pos) {
        CompoundTag rootTag = crystal.getOrCreateTagElement(POS_TAG_KEY);
        rootTag.putInt("x", pos.getX());
        rootTag.putInt("y", pos.getY());
        rootTag.putInt("z", pos.getZ());
    }
}