package com.swsnowball.dragonreborn.network;
import com.swsnowball.dragonreborn.client.SimplePettingAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;
import java.util.function.Supplier;

public class SyncAnimationStopPacket {
    private final int entityId;

    public SyncAnimationStopPacket(int entityId) { this.entityId = entityId; }
    public SyncAnimationStopPacket(FriendlyByteBuf buf) { this.entityId = buf.readInt(); }
    public void encode(FriendlyByteBuf buf) { buf.writeInt(this.entityId); }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 确保在客户端执行
            if (context.get().getDirection().getReceptionSide().isClient()) {
                // 收到服务端指令，直接移除本地动画数据
                SimplePettingAnimation.stopAnimation(this.entityId);
            }
        });
        context.get().setPacketHandled(true);
    }
}