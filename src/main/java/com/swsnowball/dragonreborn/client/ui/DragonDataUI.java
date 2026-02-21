package com.swsnowball.dragonreborn.client.ui;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import com.swsnowball.dragonreborn.config.DragonRebornConfig;

import static com.swsnowball.dragonreborn.util.DragonDataUtil.getHealthDescription;
import static com.swsnowball.dragonreborn.util.DragonDataUtil.getHealthTextColorCode;

public class DragonDataUI {
    private static final Minecraft MC = Minecraft.getInstance();

    // UI尺寸和位置
    private static final int UI_WIDTH = 180;
    private static final int UI_HEIGHT = 110; // 原为90
    private static final int PADDING = 10;
    private static final int TITLE_HEIGHT = 20;

    // 动画相关
    private static float slideProgress = 0.0f; // 0.0(完全隐藏) ~ 1.0(完全显示)
    private static float slideSpeed = 0.5f;
    private static boolean shouldShow = false;
    private static long lastUpdateTime = 0;

    // 当前显示的龙数据
    private static EntityDragonBase currentDragon = null;
    private static DragonExtendedData currentData = null;
    private static String dragonName = "";
    static boolean dragon_death = false;
    static float dragon_closeness = 0;
    static float dragon_health = 20.0f;
    static float dragon_maxHealth = 20.0f;

    public static void update(EntityDragonBase dragon, DragonExtendedData data) {
        long currentTime = System.currentTimeMillis();

        if (dragon != null && data != null) {
            currentDragon = dragon;
            currentData = data;
            dragonName = dragon.getName().getString();
            dragon_death = dragon.isModelDead();
            dragon_closeness = data.getCloseness();
            dragon_health = dragon.getHealth();
            dragon_maxHealth = dragon.getMaxHealth();
            shouldShow = true;
        } else {
            shouldShow = false;
        }

        // 更新动画
        float delta = (currentTime - lastUpdateTime) / 1000.0f * 60.0f; // 基于60fps的delta
        delta = Mth.clamp(delta, 0.01f, 0.8f); // 限制delta范围

        if (shouldShow) {
            slideProgress = Mth.lerp(slideSpeed * delta, slideProgress, 1.0f);
        } else {
            slideProgress = Mth.lerp(slideSpeed * delta, slideProgress, 0.0f);
        }

        slideProgress = Mth.clamp(slideProgress, 0.0f, 1.0f);
        lastUpdateTime = currentTime;
    }

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        if (slideProgress <= 0.001f || currentDragon == null || currentData == null) {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();
        Font font = MC.font;
        int screenWidth = MC.getWindow().getGuiScaledWidth();

        // 计算位置（使用缓动函数实现平滑滑动）
        float easedProgress = easeOutCubic(slideProgress);
        int xPos = (int)(-UI_WIDTH + (UI_WIDTH * easedProgress)) + DragonRebornConfig.UI_POSITION_X.get();
        int yPos = 50 + DragonRebornConfig.UI_POSITION_Y.get();

        // 保存当前渲染状态
        poseStack.pushPose();

        // 绘制半透明背景
        int backgroundColor = 0xAA000000; // 黑色半透明
        guiGraphics.fill(xPos, yPos, xPos + UI_WIDTH, yPos + UI_HEIGHT, backgroundColor);

        // 绘制边框
        int borderColor = 0xFF4A90E2; // 龙蓝色
        guiGraphics.fill(xPos, yPos, xPos + UI_WIDTH, yPos + 1, borderColor); // 上边框
        guiGraphics.fill(xPos, yPos + UI_HEIGHT - 1, xPos + UI_WIDTH, yPos + UI_HEIGHT, borderColor); // 下边框
        guiGraphics.fill(xPos, yPos, xPos + 1, yPos + UI_HEIGHT, borderColor); // 左边框

        // 绘制标题 - 构建Component而不是字符串拼接
        int titleY = yPos + 5;
        Component title = Component.literal("")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
                .append(Component.literal(dragonName))
                .append(Component.literal(" ").withStyle(ChatFormatting.RESET))
                .append(getHealthDescriptionComponent(dragon_health, dragon_maxHealth));

        int lineY = titleY + 17;
        int lineHeight = 15;

// 亲密度
        renderDataBar(guiGraphics, font,
                Component.translatable("dragon.data.closeness"),
                currentData.getCloseness(),
                currentData.getClosenessDescription(), // 假设这个返回字符串，保持不变
                xPos - 15, lineY, 0xFF4CAF50);
        lineY += lineHeight;

        if (!dragon_death) {
            // 心情
            renderDataBar(guiGraphics, font,
                    Component.translatable("dragon.data.moodweight"),
                    currentData.getMoodWeight(),
                    currentData.getMoodDescription(), // 假设返回字符串
                    xPos - 15, lineY, 0xFF2196F3);
            lineY += lineHeight;

            // 孤独值
            renderDataBar(guiGraphics, font,
                    Component.translatable("dragon.data.loneliness"),
                    currentData.getLoneliness(),
                    getLonelinessDescriptionComponent(currentData.getLoneliness()), // 修改为返回Component的方法
                    xPos - 15, lineY, 0xFF9C27B0);
            lineY += lineHeight;

            // 孤独状态
            renderStatusIndicator(guiGraphics, font,
                    Component.translatable("dragon.data.islonely"),
                    currentData.getLonelyStatus(),
                    xPos + PADDING, lineY);
            lineY += lineHeight;

            // 主人靠近状态
            renderStatusIndicator(guiGraphics, font,
                    Component.translatable("dragon.data.isownernear"),
                    currentData.getPlayerIsNear(),
                    xPos + PADDING + 90, lineY - lineHeight);

            if (dragon_closeness >= 0.2) {
                // 互动请求冷却
                renderNumberIndicator(guiGraphics, font,
                        Component.translatable("dragon.data.IRC"),
                        "s", currentData.getIRC() / 20,
                        xPos + PADDING, lineY + 10, 0xFF2196F3);
            } else {
                // 不够亲密提示 - 使用带占位符的单个键
                Component hint = Component.translatable("dragon.dataUI.noIRHint", dragonName)
                        .withStyle(ChatFormatting.RED);
                guiGraphics.drawString(font, hint, xPos + PADDING, lineY + 10, 0xFFFFFF);
            }

            poseStack.popPose();
        } else {
            Component deadHint = Component.translatable("dragon.dataUI.dragon_death_hint")
                    .withStyle(ChatFormatting.RED);
            guiGraphics.drawString(font, deadHint, xPos + 5, lineY, 0xFFFFFF);
        }

    }

    private static void renderDataBar(GuiGraphics guiGraphics, Font font, Component label,
                                      float value, Component description,
                                      int x, int y, int color) {
        PoseStack poseStack = guiGraphics.pose();

        // 绘制标签 - 直接使用Component，并添加冒号作为文字组件
        Component labelWithColon = Component.literal("").append(label).append(":");
        guiGraphics.drawString(font, labelWithColon, x + 15, y, 0xFFFFFF);

        // 其余代码保持不变...
        // 绘制背景条、前景条、边框、数值和描述（描述保持字符串）
        int barWidth = 100;
        int barHeight = 8;
        int barX = x + 50;
        int barY = y + 2;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x66000000);
        int fillWidth = (int)(barWidth * value);
        guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, color);
        guiGraphics.fill(barX, barY, barX + 1, barY + barHeight, 0xFFFFFFFF);
        guiGraphics.fill(barX + barWidth - 1, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        String valueText = String.format("%.0f%%", value * 100);
        guiGraphics.drawString(font, valueText, barX + barWidth + 5, y, 0xFFFFFF);

        poseStack.pushPose();
        poseStack.scale(0.8f, 0.8f, 1.0f);
        guiGraphics.drawString(font, description,
                (int)((barX + barWidth + 25) / 0.8f),
                (int)((y) / 0.8f),
                0xAAAAAA);
        poseStack.popPose();
    }

    private static void renderStatusIndicator(GuiGraphics guiGraphics, Font font,
                                              Component label, boolean status,
                                              int x, int y) {
        Component statusText = status
                ? Component.translatable("basic.logic.yes").withStyle(ChatFormatting.GREEN)
                : Component.translatable("basic.logic.no").withStyle(ChatFormatting.RED);
        Component fullText = Component.literal("").append(label).append(": ").append(statusText);
        guiGraphics.drawString(font, fullText, x, y, 0xFFFFFF);

        int dotColor = status ? 0xFF00FF00 : 0xFFFF0000;
        int dotSize = 4;
        guiGraphics.fill(x - 8, y + 4, x - 8 + dotSize, y + 4 + dotSize, dotColor);
    }

    private static void renderNumberIndicator(GuiGraphics guiGraphics, Font font,
                                              Component label, String unit, float value,
                                              int x, int y, int color) {
        Component text = Component.literal("")
                .append(label)
                .append(": " + value + " " + unit);
        guiGraphics.drawString(font, text, x, y, color);
    }

    private static Component getLonelinessDescriptionComponent(float loneliness) {
        if (loneliness == 0.0f) return Component.translatable("dragon.data.loneliness.rate.great");
        if (loneliness < 0.2f) return Component.translatable("dragon.data.loneliness.rate.slight");
        if (loneliness < 0.6f) return Component.translatable("dragon.data.loneliness.rate.medium");
        return Component.translatable("dragon.data.loneliness.rate.severe");
    }

    private static void drawCenteredString(GuiGraphics guiGraphics, Font font, Component text,
                                           int x, int y, int color) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color);
    }

    // 缓动函数：平滑结束
    private static float easeOutCubic(float t) {
        return (float)(1 - Math.pow(1 - t, 3));
    }

    private static Component getHealthDescriptionComponent(float current, float max_value) {
        float hp_perc = current / max_value;
        ChatFormatting color;
        Component desc;
        if (hp_perc == 1) {
            color = ChatFormatting.DARK_GREEN;
            desc = Component.translatable("dragon.attributes.health.no_hurt");
        } else if (hp_perc >= 0.8) {
            color = ChatFormatting.GREEN;
            desc = Component.translatable("dragon.attributes.health.healthy");
        } else if (hp_perc >= 0.7) {
            color = ChatFormatting.DARK_AQUA;
            desc = Component.translatable("dragon.attributes.health.little_wound");
        } else if (hp_perc >= 0.4) {
            color = ChatFormatting.YELLOW;
            desc = Component.translatable("dragon.attributes.health.hurt");
        } else if (hp_perc >= 0.1) {
            color = ChatFormatting.RED;
            desc = Component.translatable("dragon.attributes.health.serious");
        } else if (hp_perc > 0) {
            color = ChatFormatting.DARK_RED;
            desc = Component.translatable("dragon.attributes.health.dying");
        } else {
            color = ChatFormatting.GRAY;
            desc = Component.translatable("dragon.attributes.health.dead");
        }
        return Component.literal("").append(((MutableComponent) desc).withStyle(color)).append(")");
    }
}