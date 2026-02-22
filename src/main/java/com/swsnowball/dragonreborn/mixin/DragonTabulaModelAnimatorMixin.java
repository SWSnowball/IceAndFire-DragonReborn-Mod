package com.swsnowball.dragonreborn.mixin;

import com.github.alexthe666.iceandfire.client.model.animator.DragonTabulaModelAnimator;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.swsnowball.dragonreborn.client.DragonAnimationManager;
import com.swsnowball.dragonreborn.client.SimplePettingAnimation;
import com.swsnowball.dragonreborn.client.animation.DragonLookController;
import com.swsnowball.dragonreborn.client.animation.IDragonAnimation;
import com.swsnowball.dragonreborn.client.animation.LookAnimationManager;
import com.swsnowball.dragonreborn.client.animation.PettingAnimationApplier;
import com.swsnowball.dragonreborn.event.DragonLookToPlayerAnimationHandler;
import com.swsnowball.dragonreborn.util.DragonNBTUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.swsnowball.dragonreborn.event.DragonLookToPlayerAnimationHandler.CONTROLLERS;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.getDragonDizziness;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.getHasReset;

@Mixin(DragonTabulaModelAnimator.class)
public abstract class DragonTabulaModelAnimatorMixin<T extends EntityDragonBase> {

    @Inject(
            method = "setRotationAngles(Lcom/github/alexthe666/citadel/client/model/TabulaModel;Lcom/github/alexthe666/iceandfire/entity/EntityDragonBase;FFFFFF)V",
            at = @At("TAIL"),
            remap = false
    )
    private void dragonreborn$injectCustomAnimations(
            TabulaModel model,
            EntityDragonBase dragon,
            float limbSwing, float limbSwingAmount, float ageInTicks,
            float rotationYaw, float rotationPitch, float scale,
            CallbackInfo ci
    ) {
        // 下面注释的为旧实现备份
        /*
        // 检查抚摸动画
        SimplePettingAnimation.AnimationData animData = SimplePettingAnimation.getAnimationData(dragon.getId());
        if (animData != null && animData.isPlaying) {
            PettingAnimationApplier.applyPettingAnimation(model, animData, ageInTicks);
        } else {
            if (getDragonDizziness(dragon) <= 0) {
                DragonLookController controller = CONTROLLERS.computeIfAbsent(
                        dragon.getUUID(),
                        k -> new DragonLookController(dragon)
                );
                if (LookAnimationManager.shouldLookAtPlayer(dragon)) {
                    // 应用动画到模型
                    controller.applyToModel(model);
                    DragonNBTUtil.setHasReset(dragon, false);
                }
            } else {
                IDragonAnimation anim = DragonAnimationManager.getAnimation(dragon.getId());
                if (anim != null && !anim.isFinished()) {
                    anim.apply(model, dragon, ageInTicks, 0); // partialTicks 可从上下文中获取，这里简化为0
                    return;
                }
            }
        }

         */
        // 1. 检查管理器中的动画（如眩晕）
        IDragonAnimation anim = DragonAnimationManager.getAnimation(dragon.getId());
        if (anim != null && !anim.isFinished()) {
            anim.apply(model, dragon, ageInTicks, 0); // partialTicks 可从上下文中获取，这里简化为0
            return;
        }

        // 2. 检查原有的抚摸动画
        SimplePettingAnimation.AnimationData petAnim = SimplePettingAnimation.getAnimationData(dragon.getId());
        if (petAnim != null && petAnim.isPlaying) {
            PettingAnimationApplier.applyPettingAnimation(model, petAnim, ageInTicks);
            return;
        }

        // 3. 看向动画等原有逻辑
        DragonLookController controller = CONTROLLERS.computeIfAbsent(
                dragon.getUUID(),
                k -> new DragonLookController(dragon)
        );
        if (LookAnimationManager.shouldLookAtPlayer(dragon)) {
            controller.applyToModel(model);
            DragonNBTUtil.setHasReset(dragon, false);
        }
    }
}