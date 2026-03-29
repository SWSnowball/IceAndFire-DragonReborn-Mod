package com.swsnowball.dragonreborn.data;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class DragonDataKeys {
    public static final EntityDataAccessor<Float> CLOSENESS =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> MOOD_WEIGHT =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LONELINESS =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> LONELY =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> PLAYER_IS_NEAR =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> CLOSENESS_BONUS =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> INTERACTION_REQUEST_COOLDOWN =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> WAITING_FOR_PLAYER =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> WAITING_TIME =
            SynchedEntityData.defineId(EntityDragonBase.class, EntityDataSerializers.INT);
}