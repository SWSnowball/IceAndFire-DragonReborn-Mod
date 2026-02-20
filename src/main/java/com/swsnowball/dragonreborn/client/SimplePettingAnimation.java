package com.swsnowball.dragonreborn.client;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
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

    // 动画类型
    public static final String ANIM_PETTING = "petting";

    // 动画状态
    public static class AnimationData {
        public final float current_dragon_mood;
        public long startTime;
        public final float duration;
        public final String type;
        public float progress = 0f;
        public boolean isPlaying = true;
        public boolean reversed_anim = false;
        public float elapsed = 0;

        public AnimationData(String type, float duration, float current_dragon_mood) {
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
            this.type = type;
            this.current_dragon_mood = current_dragon_mood;
        }

        public void update() {
            // 计算从动画开始到现在经过的时间（秒）
            this.elapsed = (System.currentTimeMillis() - this.startTime) / 1000f;

            if (!reversed_anim) {
                // 阶段一：正向播放 (0.0 -> 1.0)
                this.progress = Math.min(1.0f, this.elapsed / this.duration);

                // 如果播到头了，触发反转
                if (this.progress >= 1.0f) {
                    this.reversed_anim = true;
                    // 重置计时器，为反向播放重新计时
                    this.startTime = System.currentTimeMillis();
                    this.elapsed = 0f;
                }
            } else {
                // 阶段二：反向播放 (1.0 -> 0.0)
                // elapsed 现在是反向阶段开始后经过的时间
                // 公式：1.0 - (经过的时间 / 总时长) = 从1.0开始递减
                this.progress = Math.max(0.0f, 1.0f - (this.elapsed / this.duration));
            }
        }

        public float getProgress() {
            return progress;
        }

        public boolean getPlayingStatus() {
            return isPlaying;
        }
    }

    // 存储所有正在播放的动画
    public static final Map<Integer, AnimationData> animations = new HashMap<>();

    // 开始一个动画
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
        animations.put(entityId, new AnimationData(type, duration, dragon_mood));
    }

    // 更新所有动画（每帧调用）
    public static void updateAllAnimations() {
        Iterator<Map.Entry<Integer, AnimationData>> iterator = animations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, AnimationData> entry = iterator.next();
            AnimationData data = entry.getValue();
            data.update();

            if (!data.isPlaying) {
                iterator.remove();
                LOGGER.debug("移除完成的动画: 实体ID={}", entry.getKey());
            }
        }
    }

    // 获取实体的动画数据
    public static AnimationData getAnimationData(int entityId) {
        return animations.get(entityId);
    }

    // 停止动画
    public static void stopAnimation(int entityId) {
        animations.remove(entityId);
    }

    // 检查是否有动画在播放
    public static boolean hasAnimation(int entityId) {
        return animations.containsKey(entityId);
    }

    // 获取动画缓动值（0-1，用于平滑）
    public static float getEasedProgress(AnimationData data) {
        if (data == null) return 0f;
        float t = data.progress;
        // 缓动函数：先快后慢
        return (float) (1 - Math.pow(1 - t, 3));
    }
}