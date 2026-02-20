package com.swsnowball.dragonreborn.network;

import com.swsnowball.dragonreborn.client.SimplePettingAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

public class StartAnimationPacket {
    private final int entityId;
    private final String animType;
    private final float duration;

    public StartAnimationPacket(int entityId, String animType, float duration) {
        this.entityId = entityId;
        this.animType = animType;
        this.duration = duration;
    }

    public StartAnimationPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.animType = buf.readUtf(32);
        this.duration = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(animType, 32);
        buf.writeFloat(duration);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 确保在客户端执行
            if (context.get().getDirection().getReceptionSide().isClient()) {
                if (Minecraft.getInstance().level != null) {
                    Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                    if (entity != null) {
                        // 开始动画
                        SimplePettingAnimation
                                .startAnimation(entityId, animType, duration);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    // 用于调试的toString
    @Override
    public String toString() {
        return String.format("StartAnimationPacket{entityId=%d, animType='%s', duration=%.2f}",
                entityId, animType, duration);
    }
}