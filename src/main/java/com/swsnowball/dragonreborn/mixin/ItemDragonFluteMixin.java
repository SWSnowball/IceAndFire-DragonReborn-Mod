package com.swsnowball.dragonreborn.mixin;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.IDragonFlute;
import com.github.alexthe666.iceandfire.item.ItemDragonFlute;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.clearDragonAggroUtil;

@Mixin(ItemDragonFlute.class)
public class ItemDragonFluteMixin {
    // NBT键名
    private static final String FUNCTION_TAG = "Function";

    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true)
    private void dragonreborn$ModifiedFunction(Level worldIn, Player player, @NotNull InteractionHand hand,
                                               CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemStack = player.getItemInHand(hand);

        // 初始化NBT
        CompoundTag tag = itemStack.getOrCreateTag();
        if (!tag.contains(FUNCTION_TAG)) {
            tag.putInt(FUNCTION_TAG, 0); // 默认模式0：原版功能
        }

        int function = tag.getInt(FUNCTION_TAG);

        // 检查玩家是否潜行
        if (player.isShiftKeyDown()) {
            // 潜行右键切换功能模式
            int newFunction = (function == 0) ? 1 : 0;
            tag.putInt(FUNCTION_TAG, newFunction);

            // 发送反馈消息
            String modeName = (newFunction == 0) ? "降落模式" : "清仇模式";
            player.displayClientMessage(
                    Component.literal("§a龙歌长笛已切换到: §e" + modeName),
                    true
            );

            // 播放切换音效
            worldIn.playSound(player, player.blockPosition(),
                    SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.5F, 1.5F);

            // 设置冷却
            player.getCooldowns().addCooldown(itemStack.getItem(), 5);

            // 返回成功，取消原方法执行
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack));
            cir.cancel();
            return;
        }

        // 非潜行：根据模式执行功能
        if (function == 1) {
            // 模式1：清除仇恨
            clearDragonAggro(worldIn, player);

            // 设置冷却
            player.getCooldowns().addCooldown(itemStack.getItem(), 60);

            // 播放音效
            worldIn.playSound(player, player.blockPosition(),
                    IafSoundRegistry.DRAGONFLUTE, SoundSource.NEUTRAL, 1, 1.75F);

            // 返回成功，取消原方法执行
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack));
            cir.cancel();
            return;
        }

        // 模式0：让原版逻辑继续执行
        // 不取消，让原方法继续
    }
    /**
     * 模式1：清除龙的仇恨
     */
    private void clearDragonAggro(Level world, Player player) {
        // 获取范围内的实体（复制原版的搜索逻辑）
        float range = 16 * com.github.alexthe666.iceandfire.IafConfig.dragonFluteDistance;
        List<Entity> entities = world.getEntities(player,
                new AABB(player.getX(), player.getY(), player.getZ(),
                        player.getX() + 1.0D, player.getY() + 1.0D, player.getZ() + 1.0D)
                        .inflate(range, 256, range));

        // 按距离排序
        Collections.sort(entities, new ItemDragonFlute.Sorter(player));

        // 筛选出龙
        List<IDragonFlute> dragons = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof IDragonFlute dragon && entity instanceof LivingEntity livingDragon) {
                // 只处理驯服的、属于该玩家的龙
                if (livingDragon instanceof TamableAnimal tamable) {
                    if (tamable.isTame() && player.getUUID().equals(tamable.getOwnerUUID())) {
                        dragons.add(dragon);
                    }
                }
            }
        }

        // 对每只龙执行清仇逻辑
        for (IDragonFlute dragon : dragons) {
            if (dragon instanceof LivingEntity livingDragon) {
                // 清除攻击目标
                livingDragon.setLastHurtByMob(null);
                // 修改AttackDecision标签（设为0停止攻击）
                clearDragonAggroUtil((EntityDragonBase)dragon);
                TamableAnimal tamable = (TamableAnimal) livingDragon;// 转为TamableAnimal
                tamable.setTarget(null);
            }
        }
    }
}