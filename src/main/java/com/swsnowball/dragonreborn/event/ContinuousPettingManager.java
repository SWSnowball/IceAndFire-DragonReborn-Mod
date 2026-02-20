package com.swsnowball.dragonreborn.event;

import java.util.HashSet;
import java.util.Set;

public class ContinuousPettingManager {
    // 核心：一个Set，里面只存放“正在被抚摸的龙”的实体ID。
    // 用Set可以防止重复，且我们只需要知道“是否在抚摸”这个状态。
    private static final Set<Integer> ACTIVE_PETTING_DRAGONS = new HashSet<>();

    /**
     * 检查一条龙是否正在被抚摸
     */
    public static boolean isDragonBeingPetted(int dragonId) {
        return ACTIVE_PETTING_DRAGONS.contains(dragonId);
    }

    /**
     * 标记一条龙开始被抚摸
     */
    public static void startPetting(int dragonId) {
        ACTIVE_PETTING_DRAGONS.add(dragonId);
    }

    /**
     * 标记一条龙停止被抚摸
     */
    public static void stopPetting(int dragonId) {
        ACTIVE_PETTING_DRAGONS.remove(dragonId);
    }

    // 可选：当服务器关闭或需要重置时清理
    public static void clearAll() {
        ACTIVE_PETTING_DRAGONS.clear();
    }
}