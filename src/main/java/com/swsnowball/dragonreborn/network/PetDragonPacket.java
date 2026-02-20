// com.swsnowball.dragonreborn.network.PetDragonPacket.java
package com.swsnowball.dragonreborn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PetDragonPacket {
    private final int dragonId;

    public PetDragonPacket(int dragonId) {
        this.dragonId = dragonId;
    }

    public PetDragonPacket(FriendlyByteBuf buf) {
        this.dragonId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(dragonId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(dragonId);
                // 这里调用抚摸处理逻辑
                // 会在事件处理器中处理
                com.swsnowball.dragonreborn.event.DragonInteractionHandler.handlePettingPacket(player, entity);
            }
        });
        context.get().setPacketHandled(true);
    }
}