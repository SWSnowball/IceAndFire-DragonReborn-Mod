package com.swsnowball.dragonreborn.init;

import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// 这个类负责注册所有物品
public class ModItems {
    // 创建延迟注册器，参数1：注册类型，参数2：mod id
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DragonRebornMod.MOD_ID);

    // 注册物品
    public static final RegistryObject<Item> DRAGON_ADRENALINE = ITEMS.register(
            "dragon_adrenaline",  // 注册名称（小写，使用下划线分隔）
            () -> new DragonAdrenalineItem(
                    new Item.Properties()
                            .stacksTo(64)  // 堆叠数量
                            .rarity(Rarity.UNCOMMON)  // 稀有度
            )
    );

    public static final RegistryObject<Item> DRAGON_BODY_PROTECTOR = ITEMS.register(
            "dragon_body_protector",  // 注册名称（小写，使用下划线分隔）
            () -> new DragonBodyProtectorItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final RegistryObject<Item> FASTER_DRAGON_ADRENALINE = ITEMS.register(
            "faster_dragon_adrenaline",  // 注册名称（小写，使用下划线分隔）
            () -> new FasterDragonAdrenalineItem(
                    new Item.Properties()
                            .stacksTo(16)
                            .rarity(Rarity.EPIC)
            )
    );

    public static final RegistryObject<Item> PROTECTOR_DEFUSER = ITEMS.register(
            "protector_defuser",  // 注册名称（小写，使用下划线分隔）
            () -> new ProtectorDefuserItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
            )
    );

    public static final RegistryObject<Item> DRAGON_SLEEP_STICK = ITEMS.register(
            "dragon_sleep_stick",  // 注册名称（小写，使用下划线分隔）
            () -> new DragonSleepStickItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.COMMON)
            )
    );

    public static final RegistryObject<Item> DRAGON_DIZZINESS_SNOWBALL = ITEMS.register(
            "dragon_dizziness_snowball",  // 注册名称（小写，使用下划线分隔）
            () -> new DragonDizzinessSnowballItem(
                    new Item.Properties()
                            .stacksTo(16)
                            .rarity(Rarity.UNCOMMON)
            )
    );

    // 后续可以在这里添加更多物品
    // public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register(...)
}