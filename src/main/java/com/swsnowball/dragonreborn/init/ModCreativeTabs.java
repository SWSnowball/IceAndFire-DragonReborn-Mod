package com.swsnowball.dragonreborn.init;

import com.swsnowball.dragonreborn.DragonRebornMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// 这个类负责创建创造模式标签页
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonRebornMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> DRAGON_REBORN_TAB =
            CREATIVE_MODE_TABS.register("dragon_reborn_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.dragonreborn"))  // 标签页标题
                            .icon(() -> new ItemStack(ModItems.DRAGON_ADRENALINE.get()))  // 显示图标
                            .displayItems((parameters, output) -> {
                                // 在这里添加所有要显示在标签页中的物品
                                output.accept(ModItems.DRAGON_ADRENALINE.get());
                                output.accept(ModItems.DRAGON_BODY_PROTECTOR.get());
                                output.accept(ModItems.FASTER_DRAGON_ADRENALINE.get());
                                output.accept(ModItems.PROTECTOR_DEFUSER.get());
                                output.accept(ModItems.DRAGON_SLEEP_STICK.get());
                                output.accept(ModItems.DRAGON_DIZZINESS_SNOWBALL.get());
                                // 后续添加更多物品
                                // output.accept(ModItems.ANOTHER_ITEM.get());
                            })
                            .build()
            );
}