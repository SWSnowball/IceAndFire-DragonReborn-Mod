package com.swsnowball.dragonreborn.mixin;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.iceandfire.item.ItemDragonHorn;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.alexthe666.iceandfire.api.FoodUtils;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.swsnowball.dragonreborn.data.DragonDataManager;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import com.swsnowball.dragonreborn.client.SimplePettingAnimation;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.*;
import static com.swsnowball.dragonreborn.util.MathUtil.round;

@Mixin(EntityDragonBase.class)
public abstract class EntityDragonBaseMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(
            method = "interactAt",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void dragonreborn$protectDragonLoot(Player player, Vec3 vec, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this; // TODO：这里是为了把这个Mixin对象转化为原方法中已经获得的this，也就是龙的对象在实际执行时自己转换为EntityDragonBase类型
        // TODO：下面所有涉及到在这里获取dragon对象为this的代码都是可以到达的，是IDEA误判
        ItemStack stack = player.getItemInHand(hand);

        // 判断是否为搜刮行为
        boolean tryingToLoot = (stack.isEmpty() && IafConfig.dragonDropSkull) ||
                (stack.getItem() == Items.GLASS_BOTTLE && IafConfig.dragonDropBlood);

        if (dragon.isModelDead() && tryingToLoot) {

            if (isDragonProtected(dragon)) {
                // 检查玩家是否是龙的主人
                if (dragon.isTame() && dragon.getOwnerUUID() != null) {
                    boolean isOwner = dragon.getOwnerUUID().equals(player.getUUID());

                    if (!isOwner) {
                        // 非主人玩家，执行保护
                        if (!dragon.level().isClientSide) {
                            player.displayClientMessage(
                                    Component.translatable("dragon.interaction.loot.fail").withStyle(ChatFormatting.RED),
                                    true
                            );
                            dragon.setDeathStage(0); // 保险起见把龙的DeathStage设为0
                            player.hurt(player.damageSources().magic(), 10.0F);
                        }
                        cir.setReturnValue(InteractionResult.FAIL);
                        return;
                    }
                    if (isOwner) {
                        player.displayClientMessage(
                                Component.translatable("dragon.interaction.loot.owner_fail").withStyle(ChatFormatting.RED),
                                true
                        );
                        cir.setReturnValue(InteractionResult.FAIL);
                        return;
                    }
                }
            }
        }
    }

    @Inject(
            method = "isBlinking",
            at = @At("HEAD"),
            cancellable = true,
            remap = false  // 原版Mod的方法可能没有remap
    )
    private void onIsBlinking(CallbackInfoReturnable<Boolean> cir) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this;

        // 检查是否有抚摸动画在播放
        if (SimplePettingAnimation.hasAnimation(dragon.getId())) {
            // 强制龙处于眨眼状态（闭眼）
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(
            method = "isTimeToWake()Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void dragonreborn$modifyTimeToWake(CallbackInfoReturnable<Boolean> cir) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this;

        // 如果不允许睡眠，总是返回 true（该醒来了）
        if (!getAllowDragonToSleep(dragon)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, remap = false)
    private void dragonreborn$checkHornInteraction(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this;
        ItemStack stack = player.getItemInHand(hand);

        if (dragon.isModelDead() && stack.is(IafItemRegistry.DRAGON_HORN.get()) && dragon.isOwnedBy(player)) {
            // 直接调用号角物品的存储方法
            ItemDragonHorn horn = (ItemDragonHorn) stack.getItem();
            InteractionResult result = horn.interactLivingEntity(stack, player, dragon, hand);
            if (result.consumesAction()) {
                // 存储成功，取消原方法以防止其他逻辑干扰
                cir.setReturnValue(result);
                cir.cancel();
            }
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void dragonreborn$FeedingAffectionToExtendedData(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        EntityDragonBase dragon = (EntityDragonBase) (Object) this;
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        int dragonType = getDragonType(dragon);
        int itemFoodAmount = FoodUtils.getFoodPoints(stack, true, dragonType == 1) / 10;
        if (itemFoodAmount > 0 && (dragon.getHunger() < 100 || dragon.getHealth() < dragon.getMaxHealth())) {
            dragon.setAnimation(EntityDragonBase.ANIMATION_BITE); // 补全原版右键喂食没有动画的问题
            DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
            float closeness_addition = (float) (itemFoodAmount * 0.0003);
            float moodWeight_addition = (float) (itemFoodAmount * 0.005);
            if (dragon.getHealth() < dragon.getMaxHealth()) {
                data.setCloseness(data.getCloseness() + closeness_addition / 5);
                data.setMoodWeight(data.getMoodWeight() + moodWeight_addition / 5);
            } else {
                data.setCloseness(data.getCloseness() + closeness_addition);
                data.setMoodWeight(data.getMoodWeight() + moodWeight_addition);
            }
            dragon.playSound(SoundEvents.GENERIC_EAT, 1.0f, 1.0f);
            Component message = Component.literal("§a（")
                    .append(Component.translatable("dragon.data.closeness"))
                    .append(" +" + round(closeness_addition * 100, 2) + "% | ")
                    .append(Component.translatable("dragon.data.moodweight"))
                    .append(" +" + (round(moodWeight_addition, 2) * 100) + "%）");
            player.displayClientMessage(message, true);
        }
    }
}
