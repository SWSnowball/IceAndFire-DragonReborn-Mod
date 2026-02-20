package com.swsnowball.dragonreborn.text;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.config.DragonRebornConfig;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

public class TextShowing {
    public static void showText(ServerLevel serverLevel, EntityDragonBase dragon,
                                DragonExtendedData data, Player player, String event) {

        if (!DragonRebornConfig.ALLOW_TEXT_SHOWING.get()) {
            return;
        }

        // 安全检查：确保玩家不为null
        if (player == null) {
            LOGGER.warn("尝试为事件 {} 显示文字，但玩家为null", event);
            return;
        }

        Map<String, String> context = new HashMap<>();
        context.put("玩家名", player.getName().getString());
        context.put("当前时间", String.valueOf(serverLevel.getDayTime()));

        List<String> texts = TextManager.getTextForEvent(event, data, context, dragon);

        if (texts.isEmpty()) {
            LOGGER.warn("事件 {} 未获取到文字", event);
            return;
        }

        // 显示所有获取到的文字
        for (String text : texts) {
            player.displayClientMessage(
                    Component.literal("§6" + text),
                    false
            );
        }
    }
}