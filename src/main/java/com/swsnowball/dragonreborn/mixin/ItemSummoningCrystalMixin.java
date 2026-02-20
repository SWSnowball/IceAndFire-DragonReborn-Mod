package com.swsnowball.dragonreborn.mixin;

import com.github.alexthe666.iceandfire.item.ItemSummoningCrystal;
import com.swsnowball.dragonreborn.util.DragonNBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

import static com.swsnowball.dragonreborn.util.DragonNBTUtil.getCrystalPosition;

@Mixin(ItemSummoningCrystal.class)
public class ItemSummoningCrystalMixin {
    @Inject(
            method = "appendHoverText",
            at = @At("TAIL")
    )
    private void dragonreborn$appendDragonPos(ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn, CallbackInfo ci) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag(); // 获取根 tag

            // 检查是否存在 "Dragon" 子标签（类型 10 表示 CompoundTag）
            if (tag.contains("Dragon", 10)) {
                CompoundTag dragonTag = tag.getCompound("Dragon");
                String dragonName = dragonTag.getString("CustomName");
                if (dragonName == "") {
                    return;
                }
                tooltip.add(Component.nullToEmpty(dragonName + "的当前位置："));
                List<Integer> DPos = getCrystalPosition(stack);
                int Dx = DPos.get(0);
                int Dy = DPos.get(1);
                int Dz = DPos.get(2);
                tooltip.add(Component.nullToEmpty("§aX: " + Dx));
                tooltip.add(Component.nullToEmpty("§cY: " + Dy));
                tooltip.add(Component.nullToEmpty("§3Z: " + Dz));
            }
        }
    }
}