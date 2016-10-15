package mchorse.metamorph.api.attacks;

import mchorse.metamorph.api.IAttackAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

/**
 * Poison attack
 * 
 * This attack poisons the target. Used by cave spider morph in the main mod.
 */
public class PoisonAttack implements IAttackAbility
{
    @Override
    public void attack(Entity target, EntityPlayer player)
    {
        if (target instanceof EntityLivingBase)
        {
            ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.POISON, 200));
        }
    }
}