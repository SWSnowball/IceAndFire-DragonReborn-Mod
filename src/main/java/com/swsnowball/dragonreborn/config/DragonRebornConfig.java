package com.swsnowball.dragonreborn.config;

import net.minecraftforge.common.ForgeConfigSpec;

// config/DragonRebornConfig.java
public class DragonRebornConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.BooleanValue ALLOW_TEXT_SHOWING;
    public static ForgeConfigSpec.BooleanValue ALLOW_DRAGON_EXTENDED_DATA_UPDATE;

    public static ForgeConfigSpec.BooleanValue SHOW_DRAGON_UI;
    public static ForgeConfigSpec.IntValue UI_POSITION_X;
    public static ForgeConfigSpec.IntValue UI_POSITION_Y;
    public static ForgeConfigSpec.DoubleValue UI_OPACITY;

    public static ForgeConfigSpec.DoubleValue HEAD_SWINGING_SPEED;
    public static ForgeConfigSpec.DoubleValue HEAD_SWINGING_RANGE;
    public static ForgeConfigSpec.DoubleValue HEAD_DOWNFALL_MULTIPLE;

    public static ForgeConfigSpec.DoubleValue NECK_PARTS_DELAY;
    public static ForgeConfigSpec.DoubleValue NECK_SWINGING_SPEED;
    public static ForgeConfigSpec.DoubleValue NECK_SWINGING_RANGE;

    public static ForgeConfigSpec.DoubleValue TAIL_SPEED;
    public static ForgeConfigSpec.DoubleValue TAIL_PARTS_DELAY;
    public static ForgeConfigSpec.DoubleValue TAIL_SWINGING_RANGE_ADDITION;

    public static ForgeConfigSpec.IntValue HEAD_YAW_DOWNFALL;

    static {
        // 一般开关配置
        BUILDER.push("Common Setups");

        ALLOW_TEXT_SHOWING = BUILDER
                .comment("Allow text showing")
                .define("allowTextShowing", true);

        ALLOW_DRAGON_EXTENDED_DATA_UPDATE = BUILDER
                .comment("Allow Dragon Extended Data to update")
                .define("allowDragonExtendedDataUpdate", true);

        BUILDER.pop();

        // 龙数据UI设置
        BUILDER.push("Dragon Data UI Options");

        SHOW_DRAGON_UI = BUILDER
                .comment("Show dragon data UI")
                .define("showDragonUI", true);

        UI_POSITION_X = BUILDER
                .comment("UI horizontal position offset (Positive number for right movement)")
                .defineInRange("uiOffsetX", 0, -100, 100);

        UI_POSITION_Y = BUILDER
                .comment("UI vertical position offset（Positive number for down movement）")
                .defineInRange("uiOffsetY", 50, 0, 200);

        UI_OPACITY = BUILDER
                .comment("UI background alpha (0.0-1.0)")
                .defineInRange("uiOpacity", 0.8, 0.1, 1.0);

        BUILDER.pop();

        // 龙抚摸动画配置
        BUILDER.push("Dragon Petting Animation Options");

        HEAD_SWINGING_SPEED = BUILDER
                .comment("Head Swinging Speed (The ω value in the sine wave, make the speed ω times faster)")
                .defineInRange("headSwingingSpeed", 0.8, 0.0, 10.0);

        HEAD_SWINGING_RANGE = BUILDER
                .comment("Head Swinging Range (The A value in the sine wave, make the range A times wider)")
                .defineInRange("headSwingingSpeed", 0.08, 0.0, 10.0);

        HEAD_DOWNFALL_MULTIPLE = BUILDER
                .comment("Head Downfall Multiple")
                .defineInRange("headDownfallMultiple", 0.1, 0.0, 10.0);

        NECK_PARTS_DELAY = BUILDER
                .comment("Delay for per neck parts (The neck root rotates less)")
                .defineInRange("neckPartsDelay", 0.15, 0.0, 10.0);

        NECK_SWINGING_SPEED = BUILDER
                .comment("Neck Swinging Speed (The ω value in the sine wave, make the speed ω times faster)")
                        .defineInRange("neckSwingingSpeed", 2.0, 0.0, 10.0);

        NECK_SWINGING_RANGE = BUILDER
                .comment("Neck swinging range (The A value in the sine wave, make the range A times wider)")
                        .defineInRange("neckSwingingRange", 0.12, 0.0, 10.0);

        TAIL_SPEED = BUILDER
                .comment("Tail Swinging Speed (The ω value in the sine wave, make the speed ω times faster)")
                        .defineInRange("tailSwingingSpeed", 2.0, 1.5, 100.0);

        TAIL_PARTS_DELAY = BUILDER
                .comment("TAIL PARTS DELAY (The φ value in the sine wave, the tail root rotates less)")
                        .defineInRange("tailPartsDelay", 0.25, 0.0, 10.0);

        TAIL_SWINGING_RANGE_ADDITION = BUILDER
                .comment("Tail Swinging Range Addition (directly add this value to the range value)")
                        .defineInRange("tailSwingingRangeAddition", 0.0, 0.0, 100.0);

        BUILDER.pop();
        BUILDER.push("Look To Player Animation Setups");

        HEAD_YAW_DOWNFALL = BUILDER
                .comment("Head Yaw Downfall: A degree value to make dragon's head yaw a little down")
                        .defineInRange("headYawDownfall", 10, 0, 360);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}