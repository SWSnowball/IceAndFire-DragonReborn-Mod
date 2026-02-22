package com.swsnowball.dragonreborn.init;

import com.swsnowball.dragonreborn.entity.DragonDizzinessSnowballEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "dragonreborn");

    public static final RegistryObject<EntityType<DragonDizzinessSnowballEntity>> DRAGON_DIZZINESS_SNOWBALL =
            ENTITY_TYPES.register("dragon_dizziness_snowball",
                    () -> EntityType.Builder.<DragonDizzinessSnowballEntity>of(DragonDizzinessSnowballEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("dragon_dizziness_snowball"));
}
