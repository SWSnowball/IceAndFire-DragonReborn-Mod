package com.swsnowball.dragonreborn.event;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.client.SimplePettingAnimation;
import com.swsnowball.dragonreborn.client.animation.playeranim.PlayerAnimationHandler;
import com.swsnowball.dragonreborn.config.DragonRebornConfig;
import com.swsnowball.dragonreborn.data.DragonDataManager;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import com.swsnowball.dragonreborn.network.StartAnimationPacket;
import com.swsnowball.dragonreborn.network.SyncAnimationStopPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

import static com.github.alexthe666.iceandfire.entity.EntityDragonBase.*;
import static com.swsnowball.dragonreborn.data.MoodManager.setNewCooldown;
import static com.swsnowball.dragonreborn.sounds.RandomInteractionSound.getRandomSound;
import static com.swsnowball.dragonreborn.text.TextShowing.showText;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = "dragonreborn")
public class DragonInteractionHandler {
    // 处理网络包
    public static void handlePettingPacket(ServerPlayer player, Entity target) {
        if (target instanceof EntityDragonBase dragon) {
            // 检查龙是否被驯服并且主人是玩家
            if (!dragon.isTame() || dragon.getOwner() == null) {
                player.displayClientMessage(
                        Component.translatable("dragon.event.untamed_interaction").withStyle(ChatFormatting.RED),
                        false
                );
                dragon.roar();
                return;
            }

            if (!dragon.getOwner().getUUID().equals(player.getUUID())) {
                player.displayClientMessage(
                        Component.translatable("dragon.event.untamed_interaction").withStyle(ChatFormatting.RED),
                        false
                );
                dragon.roar();
                return;
            }

            // 执行抚摸逻辑
            DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
            handlePetting((ServerLevel) player.level(), dragon, player, data, null);
        } else if (target instanceof EntityDragonPart part) {
            EntityDragonBase dragon = (EntityDragonBase) part.getParent();
            // 检查龙是否被驯服并且主人是玩家
            if (!dragon.isTame() || dragon.getOwner() == null) {
                player.displayClientMessage(
                        Component.translatable("dragon.event.untamed_interaction").withStyle(ChatFormatting.RED),
                        false
                );
                dragon.roar();
                return;
            }

            if (!dragon.getOwner().getUUID().equals(player.getUUID())) {
                player.displayClientMessage(
                        Component.translatable("dragon.event.untamed_interaction").withStyle(ChatFormatting.RED),
                        false
                );
                dragon.roar();
                return;
            }

            // 执行抚摸逻辑
            DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
            handlePetting((ServerLevel) player.level(), dragon, player, data, part);
        }
    }


    private static void handlePetting(ServerLevel serverLevel, EntityDragonBase dragon,
                                      Player player, DragonExtendedData data,
                                      EntityDragonPart part) { // 核心抚摸功能算法

        // 基础检查：龙是否已死
        if (dragon.isModelDead()) {
            player.displayClientMessage(
                    Component.translatable("dragon.interaction.interact_with_dead_dragon", dragon.getName())
                            .withStyle(ChatFormatting.DARK_GREEN),
                    true);
            return;
        }

        if (data.getCloseness() < 0.2) { // 亲密度过低拒绝抚摸
            if (!DragonRebornConfig.ALLOW_TEXT_SHOWING.get()) {
                player.displayClientMessage(
                        Component.translatable("dragon.interaction.low_closeness_to_pet", dragon.getName())
                                .withStyle(ChatFormatting.GOLD),
                        false
                );
            }
            showText(serverLevel, dragon, data, player, "petting");
            sendParticles(dragon, serverLevel, data);
            dragon.roar();
            return;
        }

        int dragonId = dragon.getId();

        //基于服务端状态管理器做开关判断
        if (ContinuousPettingManager.isDragonBeingPetted(dragonId)) {
            // 更新服务端状态
            ContinuousPettingManager.stopPetting(dragonId);

            //更新玩家动画服务器状态
            PlayerAnimationHandler.disableAnimation(player);

            // 广播给所有客户端，让他们停止动画
            if (!dragon.isSleeping()) {
                SyncAnimationStopPacket stopPacket = new SyncAnimationStopPacket(dragonId);
                DragonRebornMod.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> dragon), stopPacket);
            }
            // 给玩家反馈
            player.displayClientMessage(
                    Component.translatable("dragon.interaction.stop_petting", dragon.getName().getString())
                            .withStyle(ChatFormatting.GRAY),
                    true);

        } else {
            //开始抚摸逻辑
            // 更新服务端状态
            ContinuousPettingManager.startPetting(dragonId);

            //更新玩家动画服务器状态
            if (part == null) {
                PlayerAnimationHandler.enableAnimation(player, dragon);
            } else {
                PlayerAnimationHandler.enableAnimation(player, part);
            }

            // 设置龙的指令状态
            dragon.setCommand(1);

            // 检查并处理“等待玩家”的状态
            if (data.getWaitingForPlayer()) {
                setNewCooldown(data, dragon);
                showText(serverLevel, dragon, data, player, "interaction_satisfied");
            }

            // 执行抚摸奖励
            float baseIncrease = 0.002f;
            float closenessBonus = baseIncrease * data.getCloseness_bonus();
            float baseMoodWeightIncrease = 0.1f;

            // 根据当前亲密度调整数值
            if (data.getCloseness() < 0.2) {
                baseIncrease = 0.001f;
                closenessBonus = baseIncrease * data.getCloseness_bonus();
                baseMoodWeightIncrease = 0.05f;
            } else if (data.getCloseness() > 0.2) {
                closenessBonus = baseIncrease * data.getCloseness_bonus();
            }

            // 增加亲密度和情绪权重
            data.addCloseness(baseIncrease + closenessBonus);
            data.addMoodWeight(baseMoodWeightIncrease);
            data.setCloseness_bonus(1);

            // 显示奖励提示
            Component message = Component.literal("§5（")
                    .append(Component.translatable("dragon.data.closeness"))
                    .append(" + " + ((baseIncrease + closenessBonus) * 100) + "% | ")
                    .append(Component.translatable("dragon.data.moodweight"))
                    .append(" + " + (baseMoodWeightIncrease * 100) + "%）");
            player.displayClientMessage(message, true);

            // 播放音效
            serverLevel.playSound(null, dragon.blockPosition(),
                    getRandomSound(dragon), SoundSource.PLAYERS, 1.0f, 0.5f);

            // 发送粒子效果（你原有的逻辑完全保留）
            sendParticles(dragon, serverLevel, data);

            // 显示抚摸文字提示
            showText(serverLevel, dragon, data, player, "petting");

            // 广播动画开始包给所有客户端
            if (!dragon.isSleeping()) {
                StartAnimationPacket packet = new StartAnimationPacket(
                        dragonId,
                        SimplePettingAnimation.ANIM_PETTING,
                        3.0f
                );
                DragonRebornMod.NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> dragon), packet);

                // 确保发送给玩家自己
                if (player instanceof ServerPlayer serverPlayer) {
                    DragonRebornMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
                }
            }
        }
    }

    public static void handleStopPettingPacket(ServerPlayer player, Entity target) {
        if (target instanceof EntityDragonBase dragon) {
            // 同样的权限检查
            if (!dragon.isTame() || dragon.getOwner() == null ||
                    !dragon.getOwner().getUUID().equals(player.getUUID())) {
                player.displayClientMessage(Component.translatable("dragon.event.untamed_interaction").withStyle(ChatFormatting.RED), false);
                return;
            }

            // 直接执行停止逻辑
            DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
            handlePetting((ServerLevel) player.level(), dragon, player, data, null);
        }
    }

    private static void sendParticles(EntityDragonBase dragon, ServerLevel serverLevel, DragonExtendedData data) {
        final Random RANDOM = new Random();
        for (int j = 0; j < 20; j++) {
            double offsetX = (RANDOM.nextDouble()) * 3.0;
            double offsetZ = (RANDOM.nextDouble()) * 3.0;
            double offsetY = RANDOM.nextDouble() * 1.5;

            double x = dragon.getX() + offsetX;
            double z = dragon.getZ() + offsetZ;
            double y = dragon.getY() + offsetY;
            if (data.getCloseness() > 0.7f) {
                serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        x, y, z,
                        1,
                        0, 0, 0,
                        0.05
                );
            } else if (data.getCloseness() > 0.2f && data.getCloseness() < 0.7f) {
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        x, y, z,
                        1,
                        0, 0, 0,
                        0.05
                );
            } else if (data.getCloseness() < 0.2f) {
                serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        x, y, z,
                        1,
                        0, 0, 0,
                        0.05
                );
            }
        }
    }
}