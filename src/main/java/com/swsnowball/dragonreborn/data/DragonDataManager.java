// com.swsnowball.dragonreborn.data.DragonDataManager.java
package com.swsnowball.dragonreborn.data;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DragonDataManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DATA_KEY = "DragonRebornData";

    // 不使用WeakHashMap，避免数据丢失
    private static final Map<UUID, DragonExtendedData> cache = new HashMap<>();

    // 获取或创建龙的数据
    public static DragonExtendedData getOrCreateData(EntityDragonBase dragon) {
        UUID dragonId = dragon.getUUID();

        // 先从缓存中查找
        if (cache.containsKey(dragonId)) {
            return cache.get(dragonId);
        }

        // 从龙的持久化数据中读取
        CompoundTag persistentData = dragon.getPersistentData();
        DragonExtendedData data;

        if (persistentData.contains(DATA_KEY)) {
            // 如果已经有数据，反序列化
            data = DragonExtendedData.deserialize(persistentData.getCompound(DATA_KEY));
            saveData(dragon, data);
        } else {
            // 如果没有数据，创建新数据
            data = new DragonExtendedData();
            data.setDragonName(dragon.getName().getString());
            // 保存到NBT
            saveData(dragon, data);
        }

        // 放入缓存
        cache.put(dragonId, data);
        return data;
    }

    // 保存数据到龙的NBT
    public static void saveData(EntityDragonBase dragon, DragonExtendedData data) {
        dragon.getPersistentData().put(DATA_KEY, data.serialize());
        cache.put(dragon.getUUID(), data);
    }
}