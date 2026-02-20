package com.swsnowball.dragonreborn.client.ui;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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

        // 绘制标题
        int titleY = yPos + 5;
        drawCenteredString(guiGraphics, font, "§6§l" + (dragonName +
                        getHealthTextColorCode(dragon_health, dragon_maxHealth) +
                        "(" +
                        getHealthDescription(dragon_health, dragon_maxHealth)) + ")",
                xPos + UI_WIDTH / 2, titleY, 0xFFFFFF);

        // 绘制数据行
        int lineY = titleY + 17;
        int lineHeight = 15;

        // 亲密度（带颜色条）
        renderDataBar(guiGraphics, font, "亲密度", currentData.getCloseness(),
                currentData.getClosenessDescription(),
                xPos - 15, lineY, 0xFF4CAF50); // 绿色
        lineY += lineHeight;

        if (!dragon_death) {
            // 心情（带颜色条）
            renderDataBar(guiGraphics, font, "心情", currentData.getMoodWeight(),
                    currentData.getMoodDescription(),
                    xPos - 15, lineY, 0xFF2196F3); // 蓝色
            lineY += lineHeight;

            // 孤独值（带颜色条）
            renderDataBar(guiGraphics, font, "孤独值", currentData.getLoneliness(),
                    getLonelinessDescription(currentData.getLoneliness()),
                    xPos - 15, lineY, 0xFF9C27B0); // 紫色
            lineY += lineHeight;

            // 状态标志
            renderStatusIndicator(guiGraphics, font, "孤独状态", currentData.getLonelyStatus(),
                    xPos + PADDING, lineY);
            lineY += lineHeight;

            renderStatusIndicator(guiGraphics, font, "主人靠近", currentData.getPlayerIsNear(),
                    xPos + PADDING + 90, lineY - lineHeight);

            if (dragon_closeness >= 0.2) { // 只有亲密度达到0.2才会渲染互动请求时间
                renderNumberIndicator(guiGraphics, font, "互动请求冷却", "s",currentData.getIRC()/20,
                        xPos + PADDING,lineY + 10, 0xFF2196F3);
            } else {
                guiGraphics.drawString(font, ("§c" + "你和" + dragonName + "还不够亲密，它还不会找你摸..."), xPos+PADDING , lineY + 10, 0xFFFFFF);
            }


            // 恢复渲染状态
            poseStack.popPose();
        } else {
            guiGraphics.drawString(font, ("§c" + "这条龙已经死亡。"), xPos+5 , lineY, 0xFFFFFF);
        }

    }

    private static void renderDataBar(GuiGraphics guiGraphics, Font font, String label,
                                      float value, String description,
                                      int x, int y, int color) {
        PoseStack poseStack = guiGraphics.pose();

        // 绘制标签
        guiGraphics.drawString(font, label + ":", x+15, y, 0xFFFFFF);

        // 绘制背景条
        int barWidth = 100;
        int barHeight = 8;
        int barX = x + 50;
        int barY = y + 2;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x66000000);

        // 绘制前景条（根据值）
        int fillWidth = (int)(barWidth * value);
        guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, color);

        // 绘制边框
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 1, 0xFFFFFFFF);
        guiGraphics.fill(barX, barY + barHeight - 1, barX + barWidth, barY + barHeight, 0xFFFFFFFF);
        guiGraphics.fill(barX, barY, barX + 1, barY + barHeight, 0xFFFFFFFF);
        guiGraphics.fill(barX + barWidth - 1, barY, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        // 绘制数值和描述
        String valueText = String.format("%.0f%%", value * 100);
        guiGraphics.drawString(font, valueText, barX + barWidth + 5, y, 0xFFFFFF);

        // 描述（小字号）
        poseStack.pushPose();
        poseStack.scale(0.8f, 0.8f, 1.0f);
        guiGraphics.drawString(font, description,
                (int)((barX + barWidth + 25) / 0.8f),
                (int)((y) / 0.8f),
                0xAAAAAA);
        poseStack.popPose();
    }

    private static void renderStatusIndicator(GuiGraphics guiGraphics, Font font,
                                              String label, boolean status,
                                              int x, int y) {
        String statusText = status ? "§a是" : "§c否";
        guiGraphics.drawString(font, label + ": " + statusText, x, y, 0xFFFFFF);

        // 绘制状态点
        int dotColor = status ? 0xFF00FF00 : 0xFFFF0000;
        int dotSize = 4;
        guiGraphics.fill(x - 8, y + 4, x - 8 + dotSize, y + 4 + dotSize, dotColor);
    }

    private static void renderNumberIndicator(GuiGraphics guiGraphics, Font font,
                                              String label, String unit, float value,
                                              int x, int y, int i) {
        guiGraphics.drawString(font, label + ": " + value + " " + unit, x, y, 0xFFFFFF);
    }

    private static String getLonelinessDescription(float loneliness) {
        if (loneliness == 0.0f) return "良好";
        if (loneliness < 0.2f) return "轻微";
        if (loneliness < 0.6f) return "中等";
        return "严重";
    }

    private static void drawCenteredString(GuiGraphics guiGraphics, Font font, String text,
                                           int x, int y, int color) {
        guiGraphics.drawString(font, Component.literal(text),
                x - font.width(text) / 2, y, color);
    }

    // 缓动函数：平滑结束
    private static float easeOutCubic(float t) {
        return (float)(1 - Math.pow(1 - t, 3));
    }
}