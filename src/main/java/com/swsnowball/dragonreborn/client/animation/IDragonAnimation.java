package com.swsnowball.dragonreborn.client.animation;


import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;

public interface IDragonAnimation {
    /**
     * 每帧更新动画状态（例如计算进度、强度等）
     * @param entityId   龙实体ID
     * @param deltaTime  距离上一帧的时间（秒），可用于平滑动画
     */
    void update(int entityId, float deltaTime);

    /**
     * 将动画应用到模型上
     * @param model         TabulaModel
     * @param dragon        对应的龙实体
     * @param ageInTicks    年龄刻（用于周期性运动）
     * @param partialTicks  渲染帧部分 tick
     */
    void apply(TabulaModel model, EntityDragonBase dragon, float ageInTicks, float partialTicks);

    /**
     * 动画是否已结束（管理器会据此自动移除）
     */
    boolean isFinished();

    /**
     * 动画类型标识（可选，用于调试）
     */
    String getType();
}