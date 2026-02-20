package com.swsnowball.dragonreborn.item;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.isDragonProtected;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.unprotectDragon;

public class ProtectorDefuserItem extends Item {
    private static final Random RANDOM = new Random();
    public ProtectorDefuserItem(Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        // 只在服务端执行逻辑
        if (!level.isClientSide() && player != null) {
            EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 5.0);
            if (dragon != null) {
                if (dragon.getOwner() != player) {
                    dragon.setDeathStage(0); // 保险起见把龙的DeathStage设为0
                    player.hurt(player.damageSources().magic(), 10.0F);
                    player.displayClientMessage(
                            Component.literal("§c你没有权力剥夺这条龙的护符！"),
                            true
                    );
                    return InteractionResult.FAIL;
                } else {
                    if (isDragonProtected(dragon)) {
                        unprotectDragon(dragon);
                        ResourceLocation itemId = new ResourceLocation("dragonreborn:dragon_body_protector");
                        Item item = ForgeRegistries.ITEMS.getValue(itemId);

                        if (item != null) {
                            ItemStack protector_itemStack = new ItemStack(item, 1);
                            // 计算生成位置（玩家眼睛高度）
                            double x = player.getX();
                            double y = player.getY() + player.getEyeHeight();
                            double z = player.getZ();

                            // 创建一个物品实体
                            ItemEntity itemEntity = new ItemEntity(
                                    level,  // 当前世界
                                    x, y, z,        // 生成位置
                                    protector_itemStack       // 物品堆叠
                            );

                            // 设置一些属性
                            itemEntity.setPickUpDelay(0);  // 立即可以拾取
                            // 添加到世界
                            player.level().addFreshEntity(itemEntity);
                        }

                        player.displayClientMessage(
                                Component.literal("§c" + "已去除" + dragon.getName().getString() + "的护符保护。"),
                                true
                        );
                        spawnParticles((ServerLevel) level, dragon);
                        level.playSound(null, dragon.blockPosition(),
                                SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0f, 0.5f);
                        return InteractionResult.SUCCESS;
                    } else {
                        player.displayClientMessage(
                                Component.literal("§a" + dragon.getName().getString() + "的躯体还未受到保护。"),
                                true
                        );
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    private void spawnParticles(ServerLevel level, EntityDragonBase dragon) {
        for (int j = 0; j < 40; j++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 2.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 2.0;
            double offsetY = RANDOM.nextDouble() * 1.5;

            double x = dragon.getX() + offsetX;
            double z = dragon.getZ() + offsetZ;
            double y = dragon.getY() + offsetY;

            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, y, z,
                    1,
                    0, 0, 0,
                    0.05
            );
        }
    }
}