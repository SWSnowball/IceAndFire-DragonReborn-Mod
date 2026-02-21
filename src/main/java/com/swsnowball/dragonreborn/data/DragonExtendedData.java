// com.swsnowball.dragonreborn.data.DragonExtendedData.java
package com.swsnowball.dragonreborn.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class DragonExtendedData {
    // 核心数据
    private float closeness = 0.0f;      // 亲密度 0.0-1.0
    private float moodWeight = 0.5f;     // 心情权重 1.0到1.0
    private float loneliness = 0.0f;     // 孤独值 0.0-1.0
    private boolean lonely = false;
    private boolean player_isNear = false;
    private float closeness_bonus = 1.0f;
    private int interaction_request_cooldown = 6000;
    private boolean is_waiting_for_player = false;
    private int waiting_time = 0;

    // 基础信息
    private String dragonName = "";

    // ================= 基础方法 =================

    // 序列化到NBT（保存数据）
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("closeness", closeness);
        tag.putFloat("moodWeight", moodWeight);
        tag.putFloat("loneliness", loneliness);
        tag.putString("dragonName", dragonName);
        tag.putBoolean("lonely", lonely);
        tag.putBoolean("player_isNear", player_isNear);
        tag.putFloat("closeness_bonus", closeness_bonus);
        tag.putInt("interaction_request_cooldown", interaction_request_cooldown);
        tag.putBoolean("is_waiting_for_player", is_waiting_for_player);
        tag.putInt("waiting_time", waiting_time);
        return tag;
    }

    // 从NBT反序列化（读取数据）
    public static DragonExtendedData deserialize(CompoundTag tag) {
        DragonExtendedData data = new DragonExtendedData();
        data.closeness = tag.getFloat("closeness");
        data.moodWeight = tag.getFloat("moodWeight");
        data.loneliness = tag.getFloat("loneliness");
        data.dragonName = tag.getString("dragonName");
        data.lonely = tag.getBoolean("lonely");
        data.player_isNear = tag.getBoolean("player_isNear");
        data.closeness_bonus = tag.getFloat("closeness_bonus");
        data.interaction_request_cooldown = tag.getInt("interaction_request_cooldown");
        data.is_waiting_for_player = tag.getBoolean("is_waiting_for_player");
        data.waiting_time = tag.getInt("waiting_time");
        return data;
    }

    // ================= Getter和Setter =================
    // 这些方法确保数值在合理范围内

    public float getCloseness() { return closeness; }
    public void setCloseness(float value) {
        this.closeness = Math.max(0.0f, Math.min(1.0f, value));
    }

    public boolean getLonelyStatus() { return lonely; }
    public void setLonelyStatus(boolean value) {
        this.lonely = value;
    }

    public boolean getPlayerIsNear() { return player_isNear; }
    public void setPlayerIsNear(boolean value) {
        this.player_isNear = value;
    }

    public int getIRC() {return interaction_request_cooldown;}
    public void setIRC(int value) {this.interaction_request_cooldown = value;}

    public boolean getWaitingForPlayer() {return is_waiting_for_player;}
    public void setWaitingForPlayer(boolean value) {this.is_waiting_for_player = value;}

    public int getWaitingTime() {return waiting_time;}
    public void setWaitingTime(int value) {this.waiting_time = value;}

    public float getCloseness_bonus() {return closeness_bonus;}
    public void setCloseness_bonus(float value) {this.closeness_bonus = value;}

    public float getMoodWeight() { return moodWeight; }
    public void setMoodWeight(float value) {
        this.moodWeight = Math.max(0.0f, Math.min(1.0f, value));
    }

    public float getLoneliness() { return loneliness; }
    public void setLoneliness(float value) {
        this.loneliness = Math.max(0.0f, Math.min(1.0f, value));
    }

    public String getDragonName() { return dragonName; }
    public void setDragonName(String name) { this.dragonName = name; }

    // ================= 辅助方法 =================

    // 增加亲密度（带边界检查）
    public void addCloseness(float delta) {
        setCloseness(this.closeness + delta);
    }
    public void addLoneliness(float delta) {setLoneliness(this.loneliness + delta); }

    // 增加心情权重
    public void addMoodWeight(float delta) {
        setMoodWeight(this.moodWeight + delta);
    }

    // 获取心情描述（简单版）
    public String getMoodDescription() {
        if (moodWeight > 0.7f) return String.valueOf(Component.translatable("dragon.data.mood.rate.happy"));
        if (moodWeight < 0.3f) return String.valueOf(Component.translatable("dragon.data.mood.rate.sad"));
        return String.valueOf(Component.translatable("dragon.data.mood.rate.calm"));
    }

    // 获取亲密度描述（简单版）
    public String getClosenessDescription() {
        if (closeness > 0.7f) return String.valueOf(Component.translatable("dragon.data.closeness.pretty"));
        if (closeness > 0.5f) return String.valueOf(Component.translatable("dragon.data.closeness.high"));
        if (closeness > 0.2f) return String.valueOf(Component.translatable("dragon.data.closeness.medium"));
        return String.valueOf(Component.translatable("dragon.data.closeness.low"));
    }
}