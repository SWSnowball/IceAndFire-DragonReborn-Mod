package com.swsnowball.dragonreborn.data;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.ItemSummoningCrystal;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.swsnowball.dragonreborn.sounds.RandomInteractionSound.getRandomSound;
import static com.swsnowball.dragonreborn.text.TextShowing.showText;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.*;

@Mod.EventBusSubscriber(modid = "dragonreborn")
public class MoodManager {

    private static final int UPDATE_INTERVAL = 20; // 每20tick更新一次（1秒）
    static int oldCommand = 0;


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ServerLevel level = event.getServer().overworld();
            long gameTime = level.getGameTime();

            // 每20tick执行一次
            if (gameTime % UPDATE_INTERVAL == 0) {
                level.getAllEntities().forEach(entity -> {
                    if (entity instanceof EntityDragonBase dragon) {
                        updateDragonMood(dragon, level);
                        updateDragonPos(dragon);
                    }
                });
            }
        }
    }

    private static void updateDragonMood(EntityDragonBase dragon, ServerLevel level) {
        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);

        // 1. 获取龙的主人
        Player owner = null;
        if (dragon.getOwner() != null) {
            owner = level.getPlayerByUUID(dragon.getOwner().getUUID());
        }
        if (!dragon.isModelDead()) {
            // 2. 计算孤独值
            if (owner == null) {
                // 主人不在线或不在维度中
                if (!data.getLonelyStatus()) {
                    data.setLonelyStatus(true);
                    // 注意：这里不要调用showText，因为owner是null
                }
                // 主人不在线，增加孤独值
                if (data.getLoneliness() < 0.2) {
                    data.addLoneliness(0.0001388f);
                } else if (data.getLoneliness() > 0.2 && data.getLoneliness() < 0.59) {
                    data.addLoneliness(0.0001388f);
                } else if (data.getLoneliness() >= 0.59) {
                    data.addLoneliness(0.0000712f);
                }

            } else {
                // 主人在线，计算距离
                double distance = dragon.distanceToSqr(owner);

                if (distance > 80 * 80 && dragon.getCommand() != 2) {
                    // 距离大于80格且不处于守护模式
                    if (!data.getLonelyStatus()) {
                        data.setLonelyStatus(true);
                        showText(level, dragon, data, owner, "player_leaving");
                    } else {
                        // 已经处于孤独状态
                    }
                    // 增加孤独值
                    if (data.getLoneliness() < 0.2) {
                        data.addLoneliness(0.0001388f);
                    } else if (data.getLoneliness() > 0.2 && data.getLoneliness() < 0.59) {
                        data.addLoneliness(0.0001388f);
                    } else if (data.getLoneliness() >= 0.59) {
                        data.addLoneliness(0.0000712f);
                    }

                } else if (distance < 80 * 80) {
                    if (data.getLonelyStatus() && dragon.getCommand() != 2) {
                        data.setLonelyStatus(false);
                        showText(level, dragon, data, owner, "player_get_back");
                        setDragonCommand(dragon,2);
                    }
                    // 距离小于80格
                    if (data.getPlayerIsNear() && distance > 10 * 10) {
                        data.setPlayerIsNear(false);
                        owner.displayClientMessage(
                                Component.literal("§3" + "你现在没有靠近" + data.getDragonName() + "了。"),
                                true);
                    }

                    if (distance < 10 * 10) {
                        data.addLoneliness(-0.0020833f);
                        data.addCloseness(0.000017361f);
                        if (!data.getPlayerIsNear()) {
                            data.setPlayerIsNear(true);
                            owner.displayClientMessage(
                                    Component.literal("§3" + "你现在靠近" + data.getDragonName() + "了。"),
                                    true
                            );
                        }
                    }
                    data.addLoneliness(-0.0003472f);
                    data.addCloseness(0.000006944f);
                    if (data.getLonelyStatus()) {
                        data.setLonelyStatus(false);
                        showText(level, dragon, data, owner, "player_get_back");
                        setDragonCommand(dragon,2);
                    }
                }
            }

            // 3. 更新积极情绪权重
            updateMoodWeight(data, dragon, owner, level);

            // 4. 处理互动请求冷却和请求 - 只在主人在线时才处理
            if (data.getIRC() > 0 && data.getCloseness() >= 0.2) {
                data.setIRC(data.getIRC() - 20); // 减少请求互动冷却
            } else {
                // 只在主人在线并且不等待玩家时才请求互动
                if (!data.getWaitingForPlayer() && owner != null) {
                    updateInteractionRequest(data, dragon, owner, level);
                }
            }
            if (data.getWaitingForPlayer() && data.getCloseness() >= 0.2) { // 更新龙的等待时间
                if (data.getWaitingTime() >= 60) { // 若玩家超过60s无反应则自行离开
                    data.setWaitingTime(data.getWaitingTime() - 60);
                    showText(level, dragon, data, owner, "interaction_ignored");
                    float moodChange = 0.0f;
                    if (data.getMoodWeight() < 0.3) {
                        moodChange = 0.2F;
                    } else if (data.getMoodWeight() > 0.3 && data.getMoodWeight() < 0.7) {
                        moodChange = 0.1F;
                    } else if (data.getMoodWeight() > 0.7) {
                        moodChange = 0.05F;
                    }
                    data.setMoodWeight(data.getMoodWeight() - moodChange);
                    owner.displayClientMessage(
                            Component.literal("§5" + "(积极情绪权重 -" + moodChange * 100 + "%)"),
                            true
                    );
                    level.playSound(null, dragon.blockPosition(),
                            getRandomSound(dragon), SoundSource.PLAYERS, 1.0f, 0.5f);
                    setNewCooldown(data, dragon);
                } else {
                    data.setWaitingTime(data.getWaitingTime() + 1);
                    if (dragon.isHovering()) {dragon.setHovering(false);}
                    if (dragon.isFlying()) {dragon.setFlying(false);}
                }
            }

            // 5. 保存数据
            DragonDataManager.saveData(dragon, data);
        }
    }

    private static void updateMoodWeight(DragonExtendedData data, EntityDragonBase dragon, Player owner, ServerLevel level) {
        float change = 0.0f;

        // 基础变化：随时间缓慢趋于平静(0.5)
        float targetMood = 0.5f;
        change += (targetMood - data.getMoodWeight()) * 0.001f;

        // 骑行/搭在肩上/盘旋守护玩家影响
        if (owner != null && (dragon.isRidingPlayer(owner) || dragon.isHovering() || dragon.hasPassenger(owner))) {
                change += 0.002f;
                data.addCloseness(0.000003472f);
        }

        // 主人陪伴影响
        if (owner != null && dragon.distanceToSqr(owner) < 10 * 10) {
                change += 0.005f;
            // 靠近时心情变好
        }

        // 受伤影响
        if (dragon.getHealth() < dragon.getMaxHealth() * 0.79f && dragon.getHealth() > dragon.getMaxHealth() * 0.70f) {
            change -= 0.002f; // 受伤时心情变差
        } else if (dragon.getHealth() > dragon.getMaxHealth() * 0.4f && dragon.getHealth() < dragon.getMaxHealth() * 0.70f) {
            change -= 0.005f;
        } else if (dragon.getHealth() < dragon.getMaxHealth() * 0.4f && dragon.getHealth() > dragon.getMaxHealth() * 0.1f) {
            change -= 0.01f;
        } else if (dragon.getHealth() < dragon.getMaxHealth() * 0.1f) {
            change -= 0.03f;
        }

        // 饥饿值影响
        if (dragon.getHunger() < 20) {
            change -= 0.002f;
        }

        // 工作影响
        if (dragon.isFuelingForge()) {
            change -= 0.0005f;
        }

        // 孤独值影响
        if (data.getLonelyStatus()) {
            change -= 0.00025;
        }

        data.addMoodWeight(change);
        data.setDragonName(dragon.getName().getString());
        DragonDataManager.saveData(dragon, data);
    }

    public static void updateInteractionRequest(DragonExtendedData data, EntityDragonBase dragon, Player player, ServerLevel level) {
        // 安全检查：确保玩家不为null
        if (player == null) {
            LOGGER.warn("尝试为龙 {} 请求互动，但玩家为null", dragon.getName().getString());
            return;
        }

        if (data.getIRC() <= 0) { // 冷却为0时请求互动
            oldCommand = dragon.getCommand();
            data.setWaitingForPlayer(true);
            showText(level, dragon, data, player, "interaction_request");
            dragon.setCommand(2);
            if (dragon.isHovering()) {dragon.setHovering(false);}
            if (dragon.isFlying()) {dragon.setFlying(false);}
        }
    }

    public static void setNewCooldown(DragonExtendedData data, EntityDragonBase dragon) {
        Random random = new Random();
        int newValue = random.nextInt(12000) + 6000;
        newValue = newValue - (newValue % 20);
        data.setIRC(newValue);
        data.setWaitingForPlayer(false);
        data.setCloseness_bonus(2);
        data.setWaitingForPlayer(false);
        dragon.setCommand(oldCommand);
    }

    // 在 MoodManager 中实现
    public static void updateDragonPos(EntityDragonBase dragon) {
        if (dragon.isBoundToCrystal()) {
            Player owner = (Player) dragon.getOwner();
            if (owner != null) {
                Inventory inv = owner.getInventory();
                for (ItemStack stack : inv.items) {
                    if (stack.getItem() instanceof ItemSummoningCrystal && ItemSummoningCrystal.hasDragon(stack)) {
                        // 将当前位置写入晶石 NBT
                        setCrystalPosition(stack, dragon.blockPosition());
                        break; // 假设只有一个绑定的晶石
                    }
                }
            }
        }
    }
}

/*
    public static void MoodAffection(DragonExtendedData data, EntityDragonBase dragon) {
        if (data.getMoodWeight() < 0.3) {

        }
    }
}*/
