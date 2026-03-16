package com.swsnowball.dragonreborn.item;

import com.swsnowball.dragonreborn.entity.DragonDizzinessSnowballEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DragonDizzinessSnowballItem extends Item{
    public DragonDizzinessSnowballItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5f,
                0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide) {
            DragonDizzinessSnowballEntity snowball = new DragonDizzinessSnowballEntity(level, player);
            snowball.setItem(itemstack);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(snowball);
        }

        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        if (itemstack != null) {
            player.getCooldowns().addCooldown(itemstack.getItem(), 100);
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // 让物品有附魔光效
        return true;
    }

    // 添加说明文字
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add((Component.translatable("item.dragonreborn.dragon_dizziness_snowball.tooltip")).withStyle(ChatFormatting.GRAY));
    }
}
