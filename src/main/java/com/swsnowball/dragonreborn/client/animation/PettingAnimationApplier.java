package com.swsnowball.dragonreborn.client.animation;

import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.client.SimplePettingAnimation;
import com.swsnowball.dragonreborn.data.DragonDataManager;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

import static com.swsnowball.dragonreborn.config.DragonRebornConfig.*;

@OnlyIn(Dist.CLIENT)
public class PettingAnimationApplier {

    // 应用抚摸动画到模型TabulaModel
    public static void applyPettingAnimation(TabulaModel model, SimplePettingAnimation.AnimationData data, float ageInTicks) {
        if (model == null || data == null) return;

        try {
            // 直接使用TabulaModel的getCubes()方法
            Map<String, AdvancedModelBox> cubes = model.getCubes();
            if (cubes == null || cubes.isEmpty()) return;

            // 获取时间因子（用于周期性运动）
            float time = ageInTicks * 0.05f;
            float progress = data.progress;
            float easedProgress = SimplePettingAnimation.getEasedProgress(data);

            // ====== 1. 头部动画 ======
            applyHeadAnimation(cubes, time, progress, easedProgress, data.current_dragon_mood);

            // ====== 2. 脖子动画 ======
            applyNeckAnimation(cubes, time, progress, easedProgress);

            // ====== 3. 尾巴动画 ======
            applyTailAnimation(cubes, time, progress, easedProgress, data.current_dragon_mood);

            // ====== 4. 翅膀动画 ======
            applyWingAnimation(cubes, time, progress);

            // ====== 5. 身体动画 ======
            applyBodyAnimation(cubes, easedProgress);

        } catch (Exception e) {
            // 动画出错不应该影响游戏
            e.printStackTrace();
        }
    }

    // 头部动画：歪头、闭眼
    private static void applyHeadAnimation(Map<String, AdvancedModelBox> cubes, float time, float progress, float easedProgress, float dragon_mood) {
        AdvancedModelBox head = cubes.get("Head");

        if (head == null) return;

        // 1. 头部向一侧倾斜（周期性摆动）
        if (dragon_mood > 0.3) {
            float headTilt = (float) Math.sin(time * 2.0f) * 0.15f * (1.0f - easedProgress);
            head.rotateAngleZ += headTilt;
        }

        // 2. 头部微微下垂
        float headDown = (float) (easedProgress * HEAD_DOWNFALL_MULTIPLE.get());
        head.rotateAngleX -= headDown;

        // TODO:龙的闭眼效果会在DragonInteractionHandler.java展示
        // TODO:Alex通过实时更换了一整条龙的纹理来实现睁眼/闭眼效果，不是通过添加/移除这一小块龙的眼睛纹理

        // 3. 轻微左右转动（享受状）
        if (dragon_mood >= 0.7) {
            float headTurn = (float) ((float) Math.sin(time * HEAD_SWINGING_SPEED.get()) * HEAD_SWINGING_RANGE.get());
            head.rotateAngleY += headTurn;
        }

        // 4. 下颚张开（舒服的叹气）
        /*
        AdvancedModelBox jaw = cubes.get("Jaw");
        if (jaw != null) {
            float jawOpen = (float) Math.sin(time * 0.5f) * 0.4f + easedProgress * 0.03f;
            jaw.rotateAngleX += jawOpen;
        }
         */
    }

    // 脖子动画：跟随头部
    private static void applyNeckAnimation(Map<String, AdvancedModelBox> cubes, float time, float progress, float easedProgress) {
        for (int i = 1; i <= 4; i++) {
            AdvancedModelBox neck = cubes.get("Neck" + i);
            
            if (neck == null) continue;

            // 延迟递减（脖子根部动得少，前端动得多）
            float delay = (float) (i * NECK_PARTS_DELAY.get());
            float neckTilt = (float) ((float) Math.sin(time * NECK_SWINGING_SPEED.get() - delay) * NECK_SWINGING_RANGE.get() * (1.0f - easedProgress * 0.5f));
            neck.rotateAngleZ += neckTilt;

            // 脖子微微前伸
            float neckStretch = easedProgress * 0.05f * (1.0f - i * 0.2f);
            neck.rotateAngleX -= 3 * neckStretch;
        }
    }

    // 尾巴动画：高兴的摆动
    private static void applyTailAnimation(Map<String, AdvancedModelBox> cubes, float time, float progress, float easedProgress, float dragon_mood) {
        // 尾巴摆动速度
        float tailSpeed = (float) ((float) (TAIL_SPEED.get() + 0.0) - (2.0 - dragon_mood + 1));
        // 尾巴摆动幅度
        float tailAmount = (float) (dragon_mood * 0.6 + 0.03 + TAIL_SWINGING_RANGE_ADDITION.get());

        for (int i = 1; i <= 8; i++) {
            AdvancedModelBox tail = cubes.get("Tail" + i);
            if (tail == null) continue;

            // 每节尾巴有延迟
            float delay = (float) (i * (TAIL_PARTS_DELAY.get() + 0.0));

            // 1. 左右摆动（主要运动）
            float swing = (float) ((float) Math.sin(time * tailSpeed + delay) * (tailAmount - (dragon_mood * 0.6 - 0.6) + 0.1));
            tail.rotateAngleY += swing;

            // 2. 轻微上下摆动（形成∞形）
            if (dragon_mood > 0.3) {
                float upDown = (float) ((float) Math.cos(time * tailSpeed + delay) * (tailAmount - (dragon_mood * 0.6 - 0.6) + 0.1) * 0.3f);
                tail.rotateAngleZ += upDown;
            }

            // 3. 摆动幅度递减（根部小，末端大）
            float scale = 1.0f - (i * 0.1f);
            tail.rotateAngleY *= scale;
            if (tail.rotateAngleZ != 0) tail.rotateAngleZ *= scale;
        }
    }

    // 翅膀动画：轻微扇动
    private static void applyWingAnimation(Map<String, AdvancedModelBox> cubes, float time, float progress) {
        AdvancedModelBox wingL1 = cubes.get("WingL1");
        AdvancedModelBox wingR1 = cubes.get("WingR1");

        if (wingL1 != null && wingR1 != null) {
            // 轻微颤动（像舒服的战栗）
            float wingTremble = (float) Math.sin(time * 15.0f) * 0.02f * (1.0f - progress);
            wingL1.rotateAngleZ += wingTremble;
            wingR1.rotateAngleZ -= wingTremble;
        }
    }

    // 身体动画：放松下沉
    private static void applyBodyAnimation(Map<String, AdvancedModelBox> cubes, float easedProgress) {
        AdvancedModelBox bodyUpper = cubes.get("BodyUpper");
        if (bodyUpper != null) {
            // 身体微微下沉（放松状态）
            bodyUpper.rotateAngleX -= easedProgress * 0.03f;
        }

        // 四肢放松
        AdvancedModelBox legFrontL = cubes.get("LegFrontL");
        AdvancedModelBox legFrontR = cubes.get("LegFrontR");
        if (legFrontL != null && legFrontR != null) {
            legFrontL.rotateAngleX -= easedProgress * 0.02f;
            legFrontR.rotateAngleX -= easedProgress * 0.02f;
        }
    }
}