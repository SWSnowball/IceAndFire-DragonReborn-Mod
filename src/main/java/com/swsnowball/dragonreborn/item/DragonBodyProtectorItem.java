package com.swsnowball.dragonreborn.item;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import com.swsnowball.dragonreborn.util.DragonNBTUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class DragonBodyProtectorItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    public DragonBodyProtectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (!level.isClientSide() && player != null) {
            // 使用统一检测方法获取龙实体
            EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 5.0);

            if (dragon != null) {
                // 检查龙是否已经被保护
                if (DragonNBTUtil.isDragonProtected(dragon)) {
                    player.displayClientMessage(
                            Component.literal("§5" + dragon.getName().getString() + " §a不需要更多的保护了。"),
                            true
                    );
                    return InteractionResult.FAIL;
                } else {
                    // 保护龙
                    DragonNBTUtil.protectDragon(dragon);

                    player.displayClientMessage(
                            Component.literal("§6" + dragon.getName().getString() + " §a的躯体已受到保护！"),
                            true
                    );

                    // 播放音效
                    level.playSound(null, dragon.blockPosition(),
                            SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);

                    // 生成粒子效果
                    if (level instanceof ServerLevel serverLevel) {
                        spawnParticles(serverLevel, dragon);
                    }

                    // 消耗物品
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                }
            } else {
                player.displayClientMessage(
                        Component.literal("§c这个物品只能给龙使用..."),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    private void spawnParticles(ServerLevel level, EntityDragonBase dragon) {
        for (int j = 0; j < 20; j++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 2.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 2.0;
            double offsetY = RANDOM.nextDouble() * 1.5;

            double x = dragon.getX() + offsetX;
            double z = dragon.getZ() + offsetZ;
            double y = dragon.getY() + offsetY;

            level.sendParticles(
                    ParticleTypes.GLOW,
                    x, y, z,
                    1,
                    0, 0, 0,
                    0.05
            );
        }
    }}