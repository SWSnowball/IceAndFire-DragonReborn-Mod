package com.swsnowball.dragonreborn.client;

import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.client.animation.IDragonAnimation;
import com.swsnowball.dragonreborn.client.animation.PettingAnimationApplier;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.swsnowball.dragonreborn.data.DragonDataManager.getOrCreateData;

@OnlyIn(Dist.CLIENT)
public class SimplePettingAnimation {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String ANIM_PETTING = "petting";

    // 动画数据类，实现 IDragonAnimation 接口
    public static class AnimationData implements IDragonAnimation {
        public final float current_dragon_mood;
        private long startTime;              // 保留，但不再用于计算
        public final float duration;
        public final String type;
        public float progress = 0f;
        public boolean isPlaying = true;
        public boolean reversed_anim = false;
        public float elapsed = 0;             // 已播放时间（秒）

        public AnimationData(String type, float duration, float current_dragon_mood) {
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
            this.type = type;
            this.current_dragon_mood = current_dragon_mood;
        }

        // 内部更新逻辑（基于 deltaTime）
        private void internalUpdate(float deltaTime) {
            this.elapsed += deltaTime;

            if (!reversed_anim) {
                this.progress = Math.min(1.0f, this.elapsed / this.duration);
                if (this.progress >= 1.0f) {
                    this.reversed_anim = true;
                    this.elapsed = 0f;
                }
            } else {
                this.progress = Math.max(0.0f, 1.0f - (this.elapsed / this.duration));
            }
        }

        // IDragonAnimation 方法实现
        @Override
        public void update(int entityId, float deltaTime) {
            internalUpdate(deltaTime);
        }

        @Override
        public void apply(TabulaModel model, EntityDragonBase dragon, float ageInTicks, float partialTicks) {
            PettingAnimationApplier.applyPettingAnimation(model, this, ageInTicks);
        }

        @Override
        public boolean isFinished() {
            return reversed_anim && progress <= 0f;
        }

        @Override
        public String getType() {
            return type;
        }

        // 保留原有方法
        public float getProgress() { return progress; }
        public boolean getPlayingStatus() { return isPlaying; }
    }

    // 存储所有动画（使用接口类型）
    private static final Map<Integer, IDragonAnimation> animations = new HashMap<>();
    private static long lastUpdateTime = System.currentTimeMillis();

    // 开始动画
    public static void startAnimation(int entityId, String type, float duration) {
        LOGGER.info("开始动画: 实体ID={}, 类型={}, 时长={}s", entityId, type, duration);
        float dragon_mood = 0;
        if (Minecraft.getInstance().level != null) {
            Entity dragon = Minecraft.getInstance().level.getEntity(entityId);
            if (dragon instanceof EntityDragonBase) {
                DragonExtendedData data = getOrCreateData((EntityDragonBase) dragon);
                dragon_mood = data.getMoodWeight();
            }
        }
        AnimationData animData = new AnimationData(type, duration, dragon_mood);
        animations.put(entityId, animData);
    }

    // 更新所有动画（每帧调用）
    public static void updateAllAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        if (deltaTime > 0.1f) deltaTime = 0.1f; // 防止卡顿时跳跃过大

        Iterator<Map.Entry<Integer, IDragonAnimation>> iterator = animations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, IDragonAnimation> entry = iterator.next();
            IDragonAnimation anim = entry.getValue();
            anim.update(entry.getKey(), deltaTime);
        }
    }

    // 获取动画数据（返回 AnimationData，供外部使用）
    public static AnimationData getAnimationData(int entityId) {
        IDragonAnimation anim = animations.get(entityId);
        if (anim instanceof AnimationData) {
            return (AnimationData) anim;
        }
        return null;
    }

    public static void stopAnimation(int entityId) {
        animations.remove(entityId);
    }

    public static boolean hasAnimation(int entityId) {
        return animations.containsKey(entityId);
    }

    public static float getEasedProgress(AnimationData data) {
        if (data == null) return 0f;
        float t = data.progress;
        return (float) (1 - Math.pow(1 - t, 3));
    }
}