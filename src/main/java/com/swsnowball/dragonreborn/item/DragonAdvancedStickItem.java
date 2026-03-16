package com.swsnowball.dragonreborn.item;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.swsnowball.dragonreborn.sounds.RandomInteractionSound.getRandomSound;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.getAllowDragonToSleep;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.setAllowDragonToSleep;

public class DragonAdvancedStickItem extends Item {
    public DragonAdvancedStickItem(Properties properties) {
        super(properties);
    }

    public static void setDragonSleepBehavior(EntityDragonBase dragon, Player player, Level level) {
        if (dragon != null && !dragon.isModelDead()) {
            if (getAllowDragonToSleep(dragon)) {
                setAllowDragonToSleep(dragon, false);
                player.displayClientMessage(
                        Component.literal("§6" + dragon.getName().getString() + " §a现在会陪着你一起保持清醒。"),
                        true
                );
                level.playSound(null, dragon.blockPosition(),
                        getRandomSound(dragon), SoundSource.PLAYERS, 1.0f, 0.5f);
            } else {
                setAllowDragonToSleep(dragon, true);
                player.displayClientMessage(
                        Component.literal("§6" + dragon.getName().getString() + " §a现在会自己睡觉。"),
                        true
                );
                level.playSound(null, dragon.blockPosition(),
                        getRandomSound(dragon), SoundSource.PLAYERS, 1.0f, 0.5f);
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (!level.isClientSide() && player != null) {
            // 获取龙实体
            EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 5.0);
            if (!dragon.isTame()) return InteractionResult.FAIL;
            if (dragon != null && !dragon.isModelDead()) {
                setDragonSleepBehavior(dragon, player, level);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    // 添加说明文字
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component.translatable("item.dragonreborn.dragon_advanced_stick.tooltip")).withStyle(ChatFormatting.GRAY));
    }
}