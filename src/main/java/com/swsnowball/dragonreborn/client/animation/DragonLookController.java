package com.swsnowball.dragonreborn.client.animation;

import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.util.DragonNBTUtil;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static com.swsnowball.dragonreborn.config.DragonRebornConfig.HEAD_YAW_DOWNFALL;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.setHasReset;

public class DragonLookController {

    public EntityDragonBase dragon;
    private float headYaw;
    private float headPitch;
    private float neckYaw;
    private float neckPitch;
    public static final Logger LOGGER = LogManager.getLogger();

    public DragonLookController(EntityDragonBase dragon) {
        this.dragon = dragon;
    }

    /**
     * 更新朝向动画
     */
    public void update() {
        LookToPlayerAnimation.LookData lookData = LookToPlayerAnimation.calculateLookAtOwner(dragon);

        if (!lookData.hasOwner) {
            // 平滑返回默认位置
            headYaw = Mth.lerp(0.05F, headYaw, 0);  // 从 0.1F 改为 0.05F
            headPitch = Mth.lerp(0.05F, headPitch, 0);
            neckYaw = Mth.lerp(0.05F, neckYaw, 0);
            neckPitch = Mth.lerp(0.05F, neckPitch, 0);
            //LOGGER.info("平滑返回默认位置");
            return;
        }

        // 应用平滑过渡
        float lerpSpeed = 0.2F;  // 从 0.2F 改为 0.1F，让过渡更平滑

        headYaw = Mth.lerp(lerpSpeed, headYaw, lookData.headYaw);
        headPitch = Mth.lerp(lerpSpeed, headPitch, lookData.headPitch);

        // 颈部旋转比头部小一些
        neckYaw = Mth.lerp(lerpSpeed, neckYaw, lookData.headYaw * 0.7F);
        neckPitch = Mth.lerp(lerpSpeed, neckPitch, lookData.headPitch * 0.5F);

        // 如果需要，旋转整个实体
        if (LookToPlayerAnimation.shouldRotateEntity(lookData.headYaw, lookData.headPitch)) {
            // 逐步旋转整个龙实体
            float targetYaw = lookData.yaw;
            float currentYaw = dragon.getYRot();

            // 平滑旋转
            float newYaw = Mth.rotLerp(0.15F, currentYaw, targetYaw);
            dragon.setYRot(newYaw);
            dragon.yRotO = newYaw;
        }
        //LOGGER.info("完成一次update");
    }

    /**
     * 应用到模型上
     */
    public void applyToModel(TabulaModel model) {
        Map<String, AdvancedModelBox> cubes = model.getCubes();
        if (cubes == null || cubes.isEmpty()) return;

        AdvancedModelBox Head = cubes.get("Head");

        // 应用头部旋转
        Head.rotateAngleX = headPitch * Mth.DEG_TO_RAD;
        Head.rotateAngleY = ((headYaw - HEAD_YAW_DOWNFALL.get()) * Mth.DEG_TO_RAD);

        // 应用颈部旋转（如果有多个颈部段）
        AdvancedModelBox Neck1 = cubes.get("Neck1");
        if (Neck1 != null) {
            Neck1.rotateAngleX = neckPitch * Mth.DEG_TO_RAD;
            Neck1.rotateAngleY = neckYaw * Mth.DEG_TO_RAD;
        }

        // 如果有多个颈部段，可以逐段减少旋转
        for (int i = 1; i < 4; i++) {
            AdvancedModelBox Neck = cubes.get("Neck" + i);
            if (Neck != null) {
                Neck.rotateAngleX = neckPitch * (0.7F - i * 0.2F) * Mth.DEG_TO_RAD;
                Neck.rotateAngleY = neckYaw * (0.7F - i * 0.2F) * Mth.DEG_TO_RAD;
            }
        }
    }

    public void resetToDefault() {
        // 平滑返回到默认位置
        headYaw = Mth.lerp(0.1F, headYaw, 0);
        headPitch = Mth.lerp(0.1F, headPitch, 0);
        neckYaw = Mth.lerp(0.1F, neckYaw, 0);
        neckPitch = Mth.lerp(0.1F, neckPitch, 0);
        // 达到缓动函数接近目标值时停止
        if (headYaw < 0.01 && headPitch < 0.01 && neckYaw < 0.01 && neckPitch < 0.01) {
            setHasReset(dragon, true);
        }
    }

    public float getHeadYaw() { return headYaw; }
    public float getHeadPitch() { return headPitch; }
    public float getNeckYaw() { return neckYaw; }
    public float getNeckPitch() { return neckPitch; }
}