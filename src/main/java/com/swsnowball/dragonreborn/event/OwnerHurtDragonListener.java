package com.swsnowball.dragonreborn.event;


import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.data.DragonDataManager;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.swsnowball.dragonreborn.util.MathUtil.round;

@Mod.EventBusSubscriber(modid = "dragonreborn", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OwnerHurtDragonListener {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof EntityDragonBase dragon) {
            DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            if (dragon.isTame()) {
                if (attacker instanceof Player player) {
                    if (dragon.isOwnedBy(player)) {
                        float getDamage = event.getAmount();
                        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
                        float moodWeightReduction = (float) ((getDamage / dragon.getMaxHealth()) * 0.05 * 5);
                        float closenessReduction = (getDamage / dragon.getMaxHealth()) * (1 - data.getMoodWeight());
                        data.setMoodWeight(data.getMoodWeight() - moodWeightReduction);
                        data.setCloseness(data.getCloseness() - closenessReduction);
                        Component message = Component.literal("")
                                .append(Component.translatable("dragon.event.hurt").withStyle(ChatFormatting.RED))
                                .append(Component.literal(dragon.getName().getString()).withStyle(ChatFormatting.RED))
                                .append(Component.literal("！§e（").withStyle(ChatFormatting.YELLOW))
                                .append(Component.translatable("dragon.data.moodweight"))
                                .append(" -" + round(moodWeightReduction * 100, 2) + "% | ")
                                .append(Component.translatable("dragon.data.closeness"))
                                .append(" -" + round(closenessReduction * 100, 2) + "%）");
                        player.displayClientMessage(message, true);
                        DragonDataManager.saveData(dragon, data);
                    }
                }
            }
        }
    }
}