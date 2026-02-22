package com.swsnowball.dragonreborn.client;

import com.swsnowball.dragonreborn.client.animation.IDragonAnimation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DragonAnimationManager {
    private static final Map<Integer, IDragonAnimation> ACTIVE_ANIMATIONS = new HashMap<>();
    private static long lastUpdateTime = System.currentTimeMillis();

    public static void startAnimation(int entityId, IDragonAnimation animation) {
        ACTIVE_ANIMATIONS.put(entityId, animation);
    }

    public static void stopAnimation(int entityId) {
        ACTIVE_ANIMATIONS.remove(entityId);
    }

    public static IDragonAnimation getAnimation(int entityId) {
        return ACTIVE_ANIMATIONS.get(entityId);
    }

    public static void updateAll() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;
        if (deltaTime > 0.1f) deltaTime = 0.1f; // 防卡顿

        Iterator<Map.Entry<Integer, IDragonAnimation>> iterator = ACTIVE_ANIMATIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, IDragonAnimation> entry = iterator.next();
            IDragonAnimation anim = entry.getValue();
            anim.update(entry.getKey(), deltaTime);
            if (anim.isFinished()) {
                iterator.remove();
            }
        }
    }
}