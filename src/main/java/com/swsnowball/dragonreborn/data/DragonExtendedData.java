package com.swsnowball.dragonreborn.data;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class DragonExtendedData {
    // 持有龙实体引用，所有 getter/setter 直接操作实体的 DataTracker
    private final EntityDragonBase dragon;

    // 构造函数：从实体创建
    public DragonExtendedData(EntityDragonBase dragon) {
        this.dragon = dragon;
        // 首次创建时尝试迁移旧数据（仅一次）
        migrateOldData();
    }

    // 数据迁移
    private void migrateOldData() {
        if (dragon == null) return;
        // 检查是否已经迁移过（标记存在则跳过）
        CompoundTag persistentData = dragon.getPersistentData();

        // 如果存在旧数据，则读取并写入 DataTracker
        if (persistentData.contains("DragonRebornData")) {
            CompoundTag oldData = persistentData.getCompound("DragonRebornData");

            dragon.getEntityData().set(DragonDataKeys.CLOSENESS, oldData.getFloat("closeness"));
            dragon.getEntityData().set(DragonDataKeys.MOOD_WEIGHT, oldData.getFloat("moodWeight"));
            dragon.getEntityData().set(DragonDataKeys.LONELINESS, oldData.getFloat("loneliness"));
            dragon.getEntityData().set(DragonDataKeys.LONELY, oldData.getBoolean("lonely"));
            dragon.getEntityData().set(DragonDataKeys.PLAYER_IS_NEAR, oldData.getBoolean("player_isNear"));
            dragon.getEntityData().set(DragonDataKeys.CLOSENESS_BONUS, oldData.getFloat("closeness_bonus"));
            dragon.getEntityData().set(DragonDataKeys.INTERACTION_REQUEST_COOLDOWN, oldData.getInt("interaction_request_cooldown"));
            dragon.getEntityData().set(DragonDataKeys.WAITING_FOR_PLAYER, oldData.getBoolean("is_waiting_for_player"));
            dragon.getEntityData().set(DragonDataKeys.WAITING_TIME, oldData.getInt("waiting_time"));

            // 删除旧数据
            persistentData.remove("DragonRebornData");
        }
    }

    // ================= Getter / Setter（直接操作 DataTracker） =================
    public float getCloseness() {
        return dragon.getEntityData().get(DragonDataKeys.CLOSENESS);
    }
    public void setCloseness(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        dragon.getEntityData().set(DragonDataKeys.CLOSENESS, clamped);
    }

    public float getMoodWeight() {
        return dragon.getEntityData().get(DragonDataKeys.MOOD_WEIGHT);
    }
    public void setMoodWeight(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        dragon.getEntityData().set(DragonDataKeys.MOOD_WEIGHT, clamped);
    }

    public float getLoneliness() {
        return dragon.getEntityData().get(DragonDataKeys.LONELINESS);
    }
    public void setLoneliness(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        dragon.getEntityData().set(DragonDataKeys.LONELINESS, clamped);
    }

    public boolean getLonelyStatus() {
        return dragon.getEntityData().get(DragonDataKeys.LONELY);
    }
    public void setLonelyStatus(boolean value) {
        dragon.getEntityData().set(DragonDataKeys.LONELY, value);
    }

    public boolean getPlayerIsNear() {
        return dragon.getEntityData().get(DragonDataKeys.PLAYER_IS_NEAR);
    }
    public void setPlayerIsNear(boolean value) {
        dragon.getEntityData().set(DragonDataKeys.PLAYER_IS_NEAR, value);
    }

    public float getCloseness_bonus() {
        return dragon.getEntityData().get(DragonDataKeys.CLOSENESS_BONUS);
    }
    public void setCloseness_bonus(float value) {
        dragon.getEntityData().set(DragonDataKeys.CLOSENESS_BONUS, value);
    }

    public int getIRC() {
        return dragon.getEntityData().get(DragonDataKeys.INTERACTION_REQUEST_COOLDOWN);
    }
    public void setIRC(int value) {
        int clamped = Math.max(0, value);
        dragon.getEntityData().set(DragonDataKeys.INTERACTION_REQUEST_COOLDOWN, clamped);
    }

    public boolean getWaitingForPlayer() {
        return dragon.getEntityData().get(DragonDataKeys.WAITING_FOR_PLAYER);
    }
    public void setWaitingForPlayer(boolean value) {
        dragon.getEntityData().set(DragonDataKeys.WAITING_FOR_PLAYER, value);
    }

    public int getWaitingTime() {
        return dragon.getEntityData().get(DragonDataKeys.WAITING_TIME);
    }
    public void setWaitingTime(int value) {
        dragon.getEntityData().set(DragonDataKeys.WAITING_TIME, value);
    }

    public String getDragonName() {
        return dragon.getName().getString();
    }
    public void setDragonName(String name) {
        // 龙的名字由原版管理，这里不做实际修改（保留方法避免外部报错）
    }

    // 辅助方法
    public void addCloseness(float delta) {
        setCloseness(getCloseness() + delta);
    }
    public void addLoneliness(float delta) {
        setLoneliness(getLoneliness() + delta);
    }
    public void addMoodWeight(float delta) {
        setMoodWeight(getMoodWeight() + delta);
    }

    public Component getMoodDescription() {
        if (getMoodWeight() > 0.7f) return Component.translatable("dragon.data.mood.rate.happy");
        if (getMoodWeight() < 0.3f) return Component.translatable("dragon.data.mood.rate.sad");
        return Component.translatable("dragon.data.mood.rate.calm");
    }

    public Component getClosenessDescription() {
        if (getCloseness() > 0.7f) return Component.translatable("dragon.data.closeness.pretty");
        if (getCloseness() > 0.5f) return Component.translatable("dragon.data.closeness.high");
        if (getCloseness() > 0.2f) return Component.translatable("dragon.data.closeness.medium");
        return Component.translatable("dragon.data.closeness.low");
    }
}