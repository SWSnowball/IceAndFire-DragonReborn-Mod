// com.swsnowball.dragonreborn.network.PetDragonPacket.java
package com.swsnowball.dragonreborn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PetDragonPacket {
    private final int dragonId;
    private final int dragonPartId;

    public PetDragonPacket(int dragonId, int dragonPartId) {
        this.dragonId = dragonId;
        this.dragonPartId = dragonPartId;
    }

    public PetDragonPacket(FriendlyByteBuf buf) {
        this.dragonId = buf.readInt();
        this.dragonPartId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(dragonId);
        buf.writeInt(dragonPartId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                Entity entity = null;
                if (dragonPartId != -1) {
                    entity = player.level().getEntity(dragonPartId); // 获取部位实体
                } else {
                    entity = player.level().getEntity(dragonId);     // 获取龙本体
                }
                if (entity != null) {
                    com.swsnowball.dragonreborn.event.DragonInteractionHandler.handlePettingPacket(player, entity);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}