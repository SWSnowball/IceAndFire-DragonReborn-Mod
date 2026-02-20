package com.swsnowball.dragonreborn.item;

import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import com.swsnowball.dragonreborn.util.DragonNBTUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;

import java.util.Random;

import static com.swsnowball.dragonreborn.util.MathUtil.round;

// 龙族肾上腺素物品类
public class FasterDragonAdrenalineItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    public FasterDragonAdrenalineItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        // 只在服务端执行逻辑
        if (!level.isClientSide() && player != null) {
            // 使用统一检测方法获取龙实体
            EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 5.0);

            if (dragon != null) {
                // 检查龙是否死亡
                if (DragonNBTUtil.isDragonDead(dragon)) {
                    LOGGER.info("开始复活龙: " + dragon.getName().getString());

                    // 开始复活过程
                    startRevivalProcess(dragon, (ServerLevel) level, player);

                    // 消耗物品
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                } else {
                    // 龙还活着，给提示
                    player.displayClientMessage(
                            Component.literal("§c这玩意可不是兴奋剂..."),
                            true
                    );
                    return InteractionResult.FAIL;
                }
            } else {
                player.displayClientMessage(
                        Component.literal("§c这种药剂只能给龙使用..."),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    // 开始复活过程 - 使用Minecraft的tick系统而不是Thread.sleep
    private void startRevivalProcess(EntityDragonBase dragon, ServerLevel serverLevel, Player player) {
        player.displayClientMessage(
                Component.literal("§a开始复活 " + dragon.getName().getString() + "..."),
                true
        );

        serverLevel.playSound(null, dragon.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.5f);

        // 创建一个专门处理复活进度的类
        new Thread(() -> {
            try {
                int revivalTime = 80;
                for (int i = 1; i <= revivalTime; i++) {
                    Thread.sleep(50); // 50ms = 1 tick
                    final int tick = i;

                    // 将游戏逻辑提交到主线程执行
                    serverLevel.getServer().execute(() -> {
                        if (tick == revivalTime) {
                            completeRevival(dragon, serverLevel, player);
                        } else {
                            float progress = tick / revivalTime;
                            dragon.setHealth(20.0f * progress);
                            spawnParticles(serverLevel, dragon, progress);

                            double distance = dragon.distanceToSqr(player);

                            if (distance < 10 * 10) {
                                player.displayClientMessage(
                                        Component.literal("§e" + dragon.getName().getString() + "的复活剩余时间: " + ((revivalTime - tick) / 20) + " s"),
                                        true
                                );
                            }
                        }

                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 生成粒子效果 - 这个方法在服务端调用，会发送到所有客户端
    private void spawnParticles(ServerLevel level, EntityDragonBase dragon, float progress) {
        double radius_base = dragon.getAgeInDays() * 0.1;
        double radius = radius_base + progress * 3.0; // 半径
        double height = (int)(radius_base * 0.5) + 0.3; // 高度

        // 生成环绕龙身体的粒子环
        int particleCount = 8 + (int)radius_base + (int)(progress * 4); // 粒子数量随年龄大小调正量

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount + progress * 2 * Math.PI;

            double x = dragon.getX() + Math.cos(angle) * radius;
            double z = dragon.getZ() + Math.sin(angle) * radius;
            double y = dragon.getY() + height;

            // 根据进度选择粒子类型：前期用火焰，后期用附魔
            if (progress < 0.5f) {
                level.sendParticles(
                        ParticleTypes.FLAME,
                        x, y, z,
                        1, // 数量
                        0, 0, 0, // 偏移量
                        0.05 // 速度
                );
            } else {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x, y, z,
                        1, // 数量
                        0, 0.2, 0, // 轻微向上偏移
                        0.05
                );
            }

            // 添加一些随机粒子增强效果
            if (RANDOM.nextFloat() < 0.3f) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;

                level.sendParticles(
                        ParticleTypes.HEART,
                        dragon.getX() + offsetX,
                        dragon.getY() + 1.0 + RANDOM.nextDouble(),
                        dragon.getZ() + offsetZ,
                        1,
                        0, 0, 0,
                        0.02
                );
            }
        }
    }

    // 完成复活
    private void completeRevival(EntityDragonBase dragon, ServerLevel serverLevel, Player player) {
        // 设置满生命值
        dragon.setHealth(20.0f);

        // 清除死亡标签
        DragonNBTUtil.reviveDragon(dragon);

        // 创建效果实例
        // 参数：效果类型，持续时间（刻），放大器（0为I级，1为II级，2为III级），环境效果，显示粒子
        MobEffectInstance effect = new MobEffectInstance(
                MobEffects.WEAKNESS,  // 虚弱效果
                9600,             // 持续时间：9600 Ticks
                2,                    // 放大器：2表示III级（0=I,1=II,2=III）
                false,                // 是否为环境效果
                true,                 // 是否显示粒子
                true                  // 是否在HUD显示图标
        );

        // 应用效果到实体
        dragon.addEffect(effect);

        dragon.setCommand(2); // 完成复活后主动寻找主人

        // 播放完成音效
        serverLevel.playSound(null, dragon.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.0f);

        // 生成大量庆祝粒子
        for (int i = 0; i < 50; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 6.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 6.0;

            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    dragon.getX() + offsetX,
                    dragon.getY() + 1.0 + RANDOM.nextDouble() * 4,
                    dragon.getZ() + offsetZ,
                    1,
                    0, 0.1, 0,
                    0.05
            );
        }

        // 发送完成消息
        player.displayClientMessage(
                Component.literal("§6" + dragon.getName().getString() + " §a已成功复活！"),
                true
        );
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // 让物品有附魔光效
        return true;
    }
}