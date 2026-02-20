package com.swsnowball.dragonreborn.util;

import net.minecraft.ChatFormatting;

import java.util.Random;

public class DragonDataUtil {
    public static String getHealthDescription(float current, float max_value) {
        float hp_perc = current / max_value; // 生命值百分比（Health Point Percent）
        if (hp_perc == 1) {
            return "毫发无损";
        } else if (hp_perc >= 0.8) {
            return "健康";
        } else if (hp_perc >= 0.7 && hp_perc < 0.8) {
            return "轻伤";
        } else if (hp_perc >= 0.4 && hp_perc < 0.7) {
            return "受创";
        } else if (hp_perc >= 0.1 && hp_perc < 0.4) {
            return "重伤";
        } else if (hp_perc > 0 && hp_perc < 0.1) {
            return "濒死";
        } else if (hp_perc == 0.0) {
            return "死亡";
        }
        return null;
    }

    public static ChatFormatting getHealthTextColor(float current, float max_value) {
        float hp_perc = current / max_value;
        if (hp_perc == 1) {
            return ChatFormatting.DARK_GREEN;
        } else if (hp_perc >= 0.8) {
            return ChatFormatting.GREEN;
        } else if (hp_perc >= 0.7 && hp_perc < 0.8) {
            return ChatFormatting.DARK_AQUA;
        } else if (hp_perc >= 0.4 && hp_perc < 0.7) {
            return ChatFormatting.YELLOW;
        } else if (hp_perc >= 0.1 && hp_perc < 0.4) {
            return ChatFormatting.RED;
        } else if (hp_perc > 0 && hp_perc < 0.1) {
            return ChatFormatting.DARK_RED;
        } else if (hp_perc == 0.0 || hp_perc == 0) {
            return ChatFormatting.GRAY;
        }
        return null;
    }

    public static String getHealthTextColorCode(float current, float max_value) {
        float hp_perc = current / max_value;
        if (hp_perc == 1) {
            return "§2";
        } else if (hp_perc >= 0.8) {
            return "§a";
        } else if (hp_perc >= 0.7 && hp_perc < 0.8) {
            return "§3";
        } else if (hp_perc >= 0.4 && hp_perc < 0.7) {
            return "§e";
        } else if (hp_perc >= 0.1 && hp_perc < 0.4) {
            return "§c";
        } else if (hp_perc > 0 && hp_perc < 0.1) {
            return "§4";
        } else if (hp_perc == 0.0) {
            return "§7";
        }
        return null;
    }
}