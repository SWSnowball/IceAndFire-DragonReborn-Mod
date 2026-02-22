package com.swsnowball.dragonreborn.network;

import com.swsnowball.dragonreborn.client.animation.DizzinessAnimationApplier;
import com.swsnowball.dragonreborn.client.animation.IDragonAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDizzinessPacket {
    private final int entityId;
    private final int dizzyTime; // 剩余刻数

    public SyncDizzinessPacket(int entityId, int dizzyTime) {
        this.entityId = entityId;
        this.dizzyTime = dizzyTime;
    }

    public SyncDizzinessPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.dizzyTime = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(dizzyTime);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isClient()) {
                // 客户端处理
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    net.minecraft.world.entity.Entity entity = mc.level.getEntity(entityId);
                    if (entity instanceof com.github.alexthe666.iceandfire.entity.EntityDragonBase dragon) {
                        IDragonAnimation current = com.swsnowball.dragonreborn.client.DragonAnimationManager.getAnimation(entityId);
                        if (dizzyTime > 0) {
                            if (current instanceof DizzinessAnimationApplier) {
                                ((DizzinessAnimationApplier) current).updateRemaining(dizzyTime);
                            } else {
                                // 如果当前有其他动画，可以选择停止并开始眩晕
                                com.swsnowball.dragonreborn.client.DragonAnimationManager.startAnimation(entityId, new DizzinessAnimationApplier(dizzyTime));
                            }
                        } else {
                            // 眩晕结束，如果当前是眩晕动画则移除
                            if (current instanceof DizzinessAnimationApplier) {
                                com.swsnowball.dragonreborn.client.DragonAnimationManager.stopAnimation(entityId);
                            }
                        }
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}