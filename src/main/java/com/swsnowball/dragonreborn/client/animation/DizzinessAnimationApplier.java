package com.swsnowball.dragonreborn.client.animation;

import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class DizzinessAnimationApplier implements IDragonAnimation {
    private int remainingTicks;      // 剩余刻数（由数据包更新）
    private final int maxTicks;      // 初始最大刻数
    private float intensity;         // 当前强度 0~1
    private float time;              // 累积时间，用于周期性运动
    private static final Random RANDOM = new Random();

    public DizzinessAnimationApplier(int durationTicks) {
        this.maxTicks = durationTicks;
        this.remainingTicks = durationTicks;
        this.intensity = 1.0f;
        this.time = RANDOM.nextFloat() * 100f; // 随机初始相位
    }

    /**
     * 由网络包调用，更新剩余刻数
     */
    public void updateRemaining(int ticks) {
        this.remainingTicks = ticks;
        this.intensity = Math.min(1.0f, (float) ticks / maxTicks);
    }

    @Override
    public void update(int entityId, float deltaTime) {
        // 每帧增加时间，用于动画循环
        time += deltaTime * 20; // 20 tick/s 对应游戏刻速度
        // 注意：剩余刻数由外部数据包更新，这里不自动减少
    }

    @Override
    public void apply(TabulaModel model, EntityDragonBase dragon, float ageInTicks, float partialTicks) {
        if (intensity <= 0.01f) return;

        Map<String, AdvancedModelBox> cubes = model.getCubes();

        // 1. 头部摇晃（左右+上下）
        applyHeadAnimation(cubes, intensity, time);

        // 2. 脖子跟随头部
        applyNeckAnimation(cubes, intensity, time);

        // 3. 尾巴缓慢摆动
        applyTailAnimation(cubes, intensity, time);

        // 4. 翅膀下垂并微微颤动（参考 WINGBLAST 的翅膀姿态）
        applyWingAnimation(cubes, intensity, time);

        // 5. 身体下沉
        applyBodyAnimation(cubes, intensity);
    }

    private void applyHeadAnimation(Map<String, AdvancedModelBox> cubes, float intensity, float time) {
        AdvancedModelBox head = cubes.get("Head");
        if (head == null) return;

        // 左右摇晃（幅度随强度减弱）
        float headYaw = (float) Math.sin(time * 0.15) * 0.2f * intensity;
        head.rotateAngleY += headYaw;

        // 上下摇晃
        float headPitch = (float) Math.cos(time * 0.2) * 0.15f * intensity;
        head.rotateAngleX += headPitch;

        // 头部下垂
        head.rotateAngleX += 0.45f;

        // 下颚微微张开
        AdvancedModelBox jaw = cubes.get("Jaw");
        if (jaw != null) {
            jaw.rotateAngleX += 0.24f * intensity;
        }
    }

    private void applyNeckAnimation(Map<String, AdvancedModelBox> cubes, float intensity, float time) {
        for (int i = 1; i <= 4; i++) {
            AdvancedModelBox neck = cubes.get("Neck" + i);
            if (neck == null) continue;

            float delay = i * 0.2f;
            // 脖子随头部轻微摆动，幅度递减
            float neckYaw = (float) Math.sin(time * 0.3 - delay) * 0.1f * intensity * (1 - i * 0.1f);
            neck.rotateAngleY += neckYaw;
            // 脖子下沉
            neck.rotateAngleX -= 0.05f * intensity * (1 - i * 0.1f);
        }
    }

    private void applyTailAnimation(Map<String, AdvancedModelBox> cubes, float intensity, float time) {
        for (int i = 1; i <= 8; i++) {
            AdvancedModelBox tail = cubes.get("Tail" + i);
            if (tail == null) continue;

            float delay = i * 0.25f;
            // 左右摆动，幅度小
            float swing = (float) Math.sin(time * 0.2 + delay) * 0.1f * intensity;
            tail.rotateAngleY += swing;
            // 轻微上下
            float upDown = (float) Math.cos(time * 0.2 + delay) * 0.05f * intensity;
            tail.rotateAngleZ += upDown;
        }
    }

    private void applyWingAnimation(Map<String, AdvancedModelBox> cubes, float intensity, float time) {
        // 参考 Ice and Fire 中 WINGBLAST 的翅膀姿态：
        // 在 WINGBLAST 动画中，翅膀会向外展开再收回。眩晕时我们希望翅膀无力地下垂并轻微颤抖。
        AdvancedModelBox wingL1 = cubes.get("WingL1");
        AdvancedModelBox wingR1 = cubes.get("WingR1");
        if (wingL1 == null || wingR1 == null) return;

        // 基础下垂角度（假设正常休息时翅膀角度为0，下垂为负值）
        float baseDrop = -0.3f * intensity; // 向下旋转

        // 颤抖：正弦波动
        float tremor = (float) Math.sin(time * 8) * 0.05f * intensity;

        // 左右翅膀不对称颤抖（增加自然感）
        wingL1.rotateAngleZ += baseDrop + tremor;
        wingR1.rotateAngleZ += baseDrop - tremor;

        // 如果有 Wing2 骨骼（前臂），也做相应调整
        AdvancedModelBox wingL2 = cubes.get("WingL2");
        AdvancedModelBox wingR2 = cubes.get("WingR2");
        if (wingL2 != null) {
            wingL2.rotateAngleZ += baseDrop * 0.5f + tremor * 0.5f;
        }
        if (wingR2 != null) {
            wingR2.rotateAngleZ += baseDrop * 0.5f - tremor * 0.5f;
        }

        // 趾头轻微弯曲（可选）
        /*
        AdvancedModelBox fingerL1 = cubes.get("FingerL1");
        AdvancedModelBox fingerR1 = cubes.get("FingerR1");
        if (fingerL1 != null) {
            fingerL1.rotateAngleX += 0.1f * intensity;
        }
        if (fingerR1 != null) {
            fingerR1.rotateAngleX += 0.1f * intensity;
        }

         */
    }

    private void applyBodyAnimation(Map<String, AdvancedModelBox> cubes, float intensity) {
        AdvancedModelBox bodyUpper = cubes.get("BodyUpper");
        if (bodyUpper != null) {
            bodyUpper.rotateAngleX -= 0.05f * intensity; // 身体下沉
        }
        // 四肢放松
        AdvancedModelBox legFrontL = cubes.get("LegFrontL");
        AdvancedModelBox legFrontR = cubes.get("LegFrontR");
        if (legFrontL != null) {
            legFrontL.rotateAngleX -= 0.03f * intensity;
        }
        if (legFrontR != null) {
            legFrontR.rotateAngleX -= 0.03f * intensity;
        }
    }

    @Override
    public boolean isFinished() {
        return remainingTicks <= 0;
    }

    @Override
    public String getType() {
        return "dizziness";
    }
}