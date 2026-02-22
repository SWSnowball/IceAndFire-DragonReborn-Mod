// DragonRebornMod.java - 修改后的初始化部分
package com.swsnowball.dragonreborn;

import com.swsnowball.dragonreborn.config.DragonRebornConfig;
import com.swsnowball.dragonreborn.event.DragonLookToPlayerAnimationHandler;
import com.swsnowball.dragonreborn.event.OwnerHurtDragonListener;
import com.swsnowball.dragonreborn.init.ModEntities;
import com.swsnowball.dragonreborn.init.ModItems;
import com.swsnowball.dragonreborn.init.ModCreativeTabs;
import com.swsnowball.dragonreborn.network.*;
import com.swsnowball.dragonreborn.text.TextManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import com.swsnowball.dragonreborn.client.ui.DragonDataOverlay;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(DragonRebornMod.MOD_ID)
public class DragonRebornMod {
    public static final String MOD_ID = "dragonreborn";
    public static final Logger LOGGER = LogManager.getLogger();

    // 网络通道
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public DragonRebornMod() {
        // 获取Mod事件总线
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册事件监听器
        modEventBus.addListener(this::setup);

        // 注册物品、创造标签等
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        ModEntities.ENTITY_TYPES.register(modEventBus);

        // 注册Forge事件总线
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new DragonLookToPlayerAnimationHandler());

        MinecraftForge.EVENT_BUS.register(new OwnerHurtDragonListener());

        // 监听Mod生命周期的构造事件
        modEventBus.addListener(this::onCommonSetup);

        LOGGER.info("SWSnb_DragonReborn mod initialized!");

        // 注册配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DragonRebornConfig.SPEC, "dragonreborn-common.toml");

        // 在模组构造时初始化文字系统
        LOGGER.info("初始化文字系统...");
        TextManager.init();

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterOverlays);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Dragon Reborn mod common setup running...");
    }

    // 添加commonSetup方法来注册网络包
    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 在这里注册网络包
            int packetId = 0;
            NETWORK.registerMessage(packetId++, PetDragonPacket.class,
                    PetDragonPacket::encode, PetDragonPacket::new, PetDragonPacket::handle);

            // 注册StartAnimationPacket
            NETWORK.registerMessage(packetId++, StartAnimationPacket.class,
                    StartAnimationPacket::encode, StartAnimationPacket::new, StartAnimationPacket::handle);
            NETWORK.registerMessage(packetId++, StopPettingPacket.class,
                    StopPettingPacket::encode, StopPettingPacket::new, StopPettingPacket::handle);
            NETWORK.registerMessage(packetId++, SyncAnimationStopPacket.class,
                    SyncAnimationStopPacket::encode, SyncAnimationStopPacket::new, SyncAnimationStopPacket::handle);
            NETWORK.registerMessage(packetId++, SyncDizzinessPacket.class,
                    SyncDizzinessPacket::encode, SyncDizzinessPacket::new, SyncDizzinessPacket::handle);
            LOGGER.info("网络包注册完成，共注册了{}个包。", packetId);
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("龙之重生客户端初始化完成");
    }

    private void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("dragon_data", DragonDataOverlay.OVERLAY);
        LOGGER.info("注册龙数据UI覆盖层");
    }
}