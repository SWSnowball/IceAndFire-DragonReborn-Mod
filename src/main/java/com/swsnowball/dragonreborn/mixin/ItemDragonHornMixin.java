package com.swsnowball.dragonreborn.mixin;

import com.github.alexthe666.iceandfire.item.ItemDragonHorn;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

import static com.swsnowball.dragonreborn.util.DragonDataUtil.*;

@Mixin(ItemDragonHorn.class)
public class ItemDragonHornMixin {

    @Inject(
            method = "appendHoverText",
            at = @At("TAIL"),
            remap = true
    )
    private void dragonreborn$appendMoreInfo(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        if (!stack.hasTag()) {
            return; // 让原版显示"空号角"
        }

        CompoundTag tag = stack.getTag();
        if (!tag.contains("EntityTag")) {
            return;
        }

        CompoundTag entityTag = tag.getCompound("EntityTag");

        // 读取生命值
        float health = entityTag.getFloat("Health");
        float maxHealth = 20.0f; // 默认值

        // 从属性列表中读取最大生命值
        if (entityTag.contains("Attributes")) {
            ListTag attributes = entityTag.getList("Attributes", 10); // 10对应CompoundTag类型
            for (int i = 0; i < attributes.size(); i++) {
                CompoundTag attribute = attributes.getCompound(i);
                if (attribute.getString("Name").equals("minecraft:generic.max_health")) {
                    maxHealth = (float) attribute.getDouble("Base");
                    break;
                }
            }
        }

        // 修复：正确构建组件，不要将 Component 与 String 拼接
        MutableComponent healthText = null;
        if (getHealthDescription(health, maxHealth) != null) {
            healthText = Component.literal("生命: ")
                    .append(Component.literal(String.format("%.1f", health))
                            .withStyle(getHealthTextColor(health, maxHealth)))
                    .append(Component.literal(" / "))
                    .append(Component.literal(String.format("%.1f", maxHealth)))
                    .append(Component.literal(" ("))
                    .append(Component.literal(getHealthDescription(health, maxHealth)))
                    .append(Component.literal(")"));
        }

        tooltip.add(healthText);

        // 读取驯养数据
        float closeness = 0.0f;
        if (entityTag.contains("ForgeData")) {
            CompoundTag forgeData = entityTag.getCompound("ForgeData");
            if (forgeData.contains("DragonRebornData")) {
                CompoundTag dragonData = forgeData.getCompound("DragonRebornData");
                closeness = dragonData.getFloat("closeness");
            }
        }

        // 修复亲密度显示
        MutableComponent closenessText = Component.literal("亲密度: ")
                .append(Component.literal(String.format("%.1f%%", closeness * 100))
                        .withStyle(getHealthTextColor(closeness, 1)));

        tooltip.add(closenessText);

        // 读取死亡状态
        boolean isDead = entityTag.getBoolean("ModelDead");
        if (isDead) {
            int deathStage = entityTag.getInt("DeathStage");
            tooltip.add(Component.literal("搜刮阶段: " + deathStage + " 次"));
        }
    }
}