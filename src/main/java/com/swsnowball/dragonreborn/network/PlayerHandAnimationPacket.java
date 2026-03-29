package com.swsnowball.dragonreborn.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.swsnowball.dragonreborn.client.animation.playeranim.PlayerAnimationHandler;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerHandAnimationPacket {
    private final UUID playerUUID;
    private final int targetEntityId;  // 龙或部位的 ID
    private final boolean start;       // true=开始, false=停止

    public PlayerHandAnimationPacket(UUID playerUUID, int targetEntityId, boolean start) {
        this.playerUUID = playerUUID;
        this.targetEntityId = targetEntityId;
        this.start = start;
    }

    public PlayerHandAnimationPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.targetEntityId = buf.readInt();
        this.start = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeInt(targetEntityId);
        buf.writeBoolean(start);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getDirection().getReceptionSide().isClient()) {
                // 在客户端执行
                Player player = Minecraft.getInstance().player;
                if (player != null && player.getUUID().equals(playerUUID)) {
                    if (start) {
                        Entity target = null;
                        if (Minecraft.getInstance().level != null) {
                            target = Minecraft.getInstance().level.getEntity(targetEntityId);
                        }
                        if (target != null) {
                            PlayerAnimationHandler.enableAnimationClient(player, target);
                        }
                    } else {
                        PlayerAnimationHandler.disableAnimationClient(player);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}