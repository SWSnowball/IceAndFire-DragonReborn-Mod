package com.swsnowball.dragonreborn.event;

import com.swsnowball.dragonreborn.client.animation.DragonLookController;
import com.swsnowball.dragonreborn.client.animation.LookAnimationManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.getHasReset;

// 修改 @Mod.EventBusSubscriber 注解
@Mod.EventBusSubscriber(modid = "dragonreborn", bus = Mod.EventBusSubscriber.Bus.FORGE)
// 添加客户端限定（因为这是客户端渲染）
@OnlyIn(Dist.CLIENT)
public class DragonLookToPlayerAnimationHandler {

    public static final Map<UUID, DragonLookController> CONTROLLERS = new HashMap<>();
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * 每客户端Tick更新所有龙的朝向控制器
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        // 清理不再存在的实体
        CONTROLLERS.entrySet().removeIf(entry ->
                entry.getValue().dragon == null || !entry.getValue().dragon.isAlive()
        );

        // 更新所有控制器，但先检查是否应该看
        for (DragonLookController controller : CONTROLLERS.values()) {
            // 使用 LookAnimationManager 判断是否应该看
            boolean shouldLook = LookAnimationManager.shouldLookAtPlayer(controller.dragon);
            if (shouldLook) {
                controller.update();
                //LOGGER.info("更新龙" + controller.dragon.getName().getString() + "的看向动画");
            } else if (!shouldLook && !getHasReset(controller.dragon)) {
                // 如果不应该看，让控制器平滑复位
                controller.resetToDefault();
                //LOGGER.info("复位");
            //}
        }
    }
}
}