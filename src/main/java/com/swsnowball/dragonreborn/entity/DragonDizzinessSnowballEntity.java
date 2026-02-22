package com.swsnowball.dragonreborn.entity;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import com.swsnowball.dragonreborn.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import static com.swsnowball.dragonreborn.sounds.RandomInteractionSound.getRandomSound;
import static com.swsnowball.dragonreborn.util.DragonNBTUtil.setDragonDizziness;

public class DragonDizzinessSnowballEntity extends ThrowableItemProjectile {
    public DragonDizzinessSnowballEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DragonDizzinessSnowballEntity(Level level, LivingEntity shooter) {
        super(ModEntities.DRAGON_DIZZINESS_SNOWBALL.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Player owner = (Player) this.getOwner();

        if (this.isRemoved()) {
            return;
        }

        // 服务端处理集中逻辑（核心）
        if (!this.level().isClientSide) {
            if (owner != null) {
                owner.displayClientMessage(
                        Component.translatable("entity.snowball.not_hit").withStyle(ChatFormatting.GRAY),
                        true
                );
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Player owner = (Player) this.getOwner();
        Entity entity = result.getEntity();

        if (this.isRemoved()) {
            return;
        }

        if (!this.level().isClientSide) {
            if (owner != null) {
                if (entity instanceof EntityDragonBase dragon) {
                    if (dragon.isOwnedBy(owner)) {
                        owner.displayClientMessage(
                                Component.translatable("entity.snowball.hit_owner_dragon", dragon.getName().getString()).withStyle(ChatFormatting.RED),
                                true
                        );
                        this.level().playSound(null, this.blockPosition(),
                                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
                        dragon.hurt(dragon.damageSources().thrown(this, this.getOwner()), 0.0F);
                        setDragonDizziness(dragon, 200);
                    } else {
                        owner.displayClientMessage(Component.translatable("entity.snowball.hit_enemy_dragon", dragon.getName().getString()), true);
                        this.level().playSound(null, this.blockPosition(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 0.5f);
                        dragon.hurt(dragon.damageSources().thrown(this, this.getOwner()), 0.0F);
                        setDragonDizziness(dragon, 200);
                    }
                } else if (entity instanceof EntityDragonPart part) {
                    Entity parent = part.getParent();
                    if (parent instanceof EntityDragonBase dragon) {
                        if (dragon.isOwnedBy(owner)) {
                            owner.displayClientMessage(
                                    Component.translatable("entity.snowball.hit_owner_dragon", dragon.getName().getString()).withStyle(ChatFormatting.RED),
                                    true
                            );
                            this.level().playSound(null, this.blockPosition(),
                                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
                            dragon.hurt(dragon.damageSources().thrown(this, this.getOwner()), 0.0F);
                            setDragonDizziness(dragon, 200);
                        } else {
                            owner.displayClientMessage(Component.translatable("entity.snowball.hit_enemy_dragon", dragon.getName().getString()), true);
                            this.level().playSound(null, this.blockPosition(),
                                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 0.5f);
                            dragon.hurt(dragon.damageSources().thrown(this, this.getOwner()), 0.0F);
                            setDragonDizziness(dragon, 200);
                        }
                    }
                } else {
                    owner.displayClientMessage(
                            Component.translatable("entity.snowball.hit_owner_dragon", entity.getName().getString()).withStyle(ChatFormatting.RED),
                            true
                    );
                    this.level().playSound(null, this.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 0.5f);
                    entity.hurt(entity.damageSources().thrown(this, this.getOwner()), 0.0F);
                    setDragonDizziness(entity, 200);
                    owner.displayClientMessage(
                            Component.translatable("entity.snowball.hit_other_entity", entity.getName().getString()).withStyle(ChatFormatting.GREEN),
                            true
                    );
                }
            }
            this.discard();
        }
    }
}
