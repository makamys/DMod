package makamys.dmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.dmod.EntityFox;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {
	
	DamageSource lastDamageSource;
	
	@Inject(method = "onDeath", at = @At("HEAD"))
	public void preOnDeath(DamageSource src, CallbackInfo ci) {
		lastDamageSource = src;
	}
	
	@ModifyVariable(method = "onDeath", at = @At("STORE"), name = "i", ordinal = 0)
	public int lootingModifier(int value) {
		Entity entity = lastDamageSource.getEntity();
		if(entity instanceof EntityFox) {
			value = ((EntityFox)entity).getLootingLevel();
		}
		return value;
	}
	
}
