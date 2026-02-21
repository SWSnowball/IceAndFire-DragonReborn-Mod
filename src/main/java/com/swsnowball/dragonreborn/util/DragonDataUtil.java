package com.swsnowball.dragonreborn.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Random;

public class DragonDataUtil {
    public static String getHealthDescription(float current, float max_value) {
        float hp_perc = current / max_value;
        Component component;
        if (hp_perc == 1) {
            component = Component.translatable("dragon.attributes.health.no_hurt");
        } else if (hp_perc >= 0.8) {
            component = Component.translatable("dragon.attributes.health.healthy");
        } else if (hp_perc >= 0.7) {
            component = Component.translatable("dragon.attributes.health.little_wound");
        } else if (hp_perc >= 0.4) {
            component = Component.translatable("dragon.attributes.health.hurt");
        } else if (hp_perc >= 0.1) {
            component = Component.translatable("dragon.attributes.health.serious");
        } else if (hp_perc > 0) {
            component = Component.translatable("dragon.attributes.health.dying");
        } else {
            component = Component.translatable("dragon.attributes.health.dead");
        }
        return component.getString(); // 在客户端调用时才能正确翻译
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