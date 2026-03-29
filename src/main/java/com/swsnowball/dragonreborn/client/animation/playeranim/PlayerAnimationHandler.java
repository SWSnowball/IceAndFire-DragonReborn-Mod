package com.swsnowball.dragonreborn.client.animation.playeranim;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.network.PlayerHandAnimationPacket;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static com.swsnowball.dragonreborn.client.animation.LookToPlayerAnimation.calculateDragonEyeHeight;

@Mod.EventBusSubscriber(modid = "dragonreborn", value = Dist.CLIENT)
public class PlayerAnimationHandler {
    // 客户端专用静态数据
    private static final Set<UUID> ANIMATED_PLAYERS = new HashSet<>();
    private static final Map<UUID, Integer> PLAYER_TARGET_ID = new HashMap<>(); // 玩家UUID -> 目标实体ID
    private static final Map<UUID, Float> ANIMATION_PROGRESS = new HashMap<>();
    private static final float SPEED = 2.0f;
    private static final float RANGE = 30.0f;

    // 由网络包调用的客户端方法
    public static void enableAnimationClient(Player player, Entity target) {
        ANIMATED_PLAYERS.add(player.getUUID());
        PLAYER_TARGET_ID.put(player.getUUID(), target.getId());
        ANIMATION_PROGRESS.put(player.getUUID(), 0f);
    }

    public static void disableAnimationClient(Player player) {
        ANIMATED_PLAYERS.remove(player.getUUID());
        PLAYER_TARGET_ID.remove(player.getUUID());
        ANIMATION_PROGRESS.remove(player.getUUID());
    }

    // 服务端调用（实际通过发包间接影响客户端）
    public static void enableAnimation(Player player, Entity target) {
        // 仅服务端调用：发送网络包给该玩家
        if (!player.level().isClientSide) {
            DragonRebornMod.NETWORK.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerHandAnimationPacket(player.getUUID(), target.getId(), true)
            );
        }
    }

    public static void disableAnimation(Player player) {
        if (!player.level().isClientSide) {
            DragonRebornMod.NETWORK.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new PlayerHandAnimationPacket(player.getUUID(), -1, false)
            );
        }
    }

    public static boolean isAnimationEnabled(Player player) {
        return ANIMATED_PLAYERS.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (UUID player : ANIMATION_PROGRESS.keySet()) {
                ANIMATION_PROGRESS.compute(player, (k, current) -> current + SPEED);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!isAnimationEnabled(player)) return;

        Integer targetId = PLAYER_TARGET_ID.get(player.getUUID());
        if (targetId == null) return;
        Entity target = player.level().getEntity(targetId);
        if (target == null) return;

        // 获取玩家模型和右臂
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        ModelPart rightArm = model.rightArm;

        float ageInTicks = player.tickCount + event.getPartialTick();

        // 计算目标高度
        float targetY = (float) target.getY();
        if (target instanceof EntityDragonBase) {
            targetY += calculateDragonEyeHeight((EntityDragonBase) target);
        }

        // 计算手臂角度
        double dx = player.getX() - target.getX();
        double dy = player.getY() + player.getEyeHeight() - targetY;
        double dz = player.getZ() - target.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, horizontalDistance) * (180.0 / Math.PI)));

        float progress = ANIMATION_PROGRESS.getOrDefault(player.getUUID(), 0f);
        float rotate = (float) Math.sin(progress) * (RANGE + yaw);

        rightArm.xRot = pitch;
        rightArm.yRot = rotate;
    }

    private static double calculateDragonEyeHeight(EntityDragonBase dragon) {
        // 实现你的计算方法
        return dragon.getBbHeight() * 0.8; // 示例
    }
}