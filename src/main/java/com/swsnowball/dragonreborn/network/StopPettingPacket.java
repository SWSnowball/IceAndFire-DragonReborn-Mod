package com.swsnowball.dragonreborn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StopPettingPacket {
    // 唯一的数据：要停止抚摸的龙ID
    private final int dragonId;

    // 构造和编解码，和PetDragonPacket一模一样
    public StopPettingPacket(int dragonId) {
        this.dragonId = dragonId;
    }
    public StopPettingPacket(FriendlyByteBuf buf) {
        this.dragonId = buf.readInt(); // 读一个整数
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.dragonId); // 写一个整数
    }

    // 处理逻辑：交给服务端的事件处理器
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(this.dragonId);
                // 像PetDragonPacket一样，转交给“停止抚摸”的处理器
                com.swsnowball.dragonreborn.event.DragonInteractionHandler.handleStopPettingPacket(player, entity);
            }
        });
        context.get().setPacketHandled(true);
    }
}