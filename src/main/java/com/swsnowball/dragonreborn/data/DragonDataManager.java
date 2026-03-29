package com.swsnowball.dragonreborn.data;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;

public class DragonDataManager {
    // 不再需要缓存，每次直接创建包装对象
    public static DragonExtendedData getOrCreateData(EntityDragonBase dragon) {
        return new DragonExtendedData(dragon);
    }
}