package com.swsnowball.dragonreborn.sounds;

import com.github.alexthe666.iceandfire.entity.EntityFireDragon;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.EntityLightningDragon;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.sounds.SoundEvent;

//import java.util.Random;

public class RandomInteractionSound {
    //Random random = new Random();
    public static SoundEvent getRandomSound(EntityDragonBase dragon) {
        SoundEvent sound = null;
        if (dragon instanceof EntityFireDragon) {
            if (dragon.isTeen()) {
                sound = IafSoundRegistry.FIREDRAGON_TEEN_IDLE;
                return sound;
            } else if (dragon.isBaby()) {
                sound = IafSoundRegistry.FIREDRAGON_CHILD_IDLE;
                return sound;
            } else {
                sound = IafSoundRegistry.FIREDRAGON_ADULT_IDLE;
                return sound;
            }
        } else if (dragon instanceof EntityIceDragon) {
            if (dragon.isTeen()) {
                sound = IafSoundRegistry.ICEDRAGON_TEEN_IDLE;
                return sound;
            } else if (dragon.isBaby()) {
                sound = IafSoundRegistry.ICEDRAGON_CHILD_IDLE;
                return sound;
            } else {
                sound = IafSoundRegistry.ICEDRAGON_ADULT_IDLE;
                return sound;
            }
        } else if (dragon instanceof EntityLightningDragon) {
            if (dragon.isTeen()) {
                sound = IafSoundRegistry.LIGHTNINGDRAGON_TEEN_IDLE;
                return sound;
            } else if (dragon.isBaby()) {
                sound = IafSoundRegistry.LIGHTNINGDRAGON_CHILD_IDLE;
                return sound;
            } else {
                sound = IafSoundRegistry.LIGHTNINGDRAGON_ADULT_IDLE;
                return sound;
            }
        }
        return null;
    }
}