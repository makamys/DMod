package makamys.dmod.future.entity.passive;

import makamys.dmod.future.entity.EntityLivingFuture;
import makamys.dmod.future.entity.EntityLivingFutured;
import makamys.dmod.util.EggHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class EntityAnimalFuture extends EntityAnimal implements EntityLivingFutured {

	public EntityAnimalFuture(World p_i1681_1_) {
		super(p_i1681_1_);
	}
	
	@Override
	public boolean interact(EntityPlayer player) {
	      ItemStack itemStack = player.inventory.getCurrentItem();
	      if (this.isBreedingItem(itemStack)) {
	         int i = this.getGrowingAge();
	         if (!this.worldObj.isRemote && i == 0 && this.canEat()) {
            	this.eat(player, itemStack);
	            this.func_146082_f(player);
	            return true;
	         }
	         /*
	         if (this.isBaby()) {
	            this.eat(player, itemStack);
	            this.growUp((int)((float)(-i / 20) * 0.1F), true);
	            return ActionResult.success(this.world.isClient);
	         }

	         if (this.world.isClient) {
	            return ActionResult.CONSUME;
	         }*/
	      }
		return super.interact(player);
	}
	
	public void eat(EntityPlayer player, ItemStack itemStack) {
		if (!player.capabilities.isCreativeMode) {
			--itemStack.stackSize;

			if (itemStack.stackSize <= 0) {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack) null);
			}
		}
	}
	
	public boolean canEat() {
		return !this.isInLove();
	}
	
	@Override
	public String getEatSound(ItemStack stack) {
		return "SoundEvents.ENTITY_GENERIC_EAT"; // TODO
	}
	
   public int getLookPitchSpeed() {
	      return 40;
	   }

	   public int getBodyYawSpeed() {
	      return 75;
	   }

	   public int getLookYawSpeed() {
	      return 10;
	   }
	   
	   @Override
	   public boolean attackEntityAsMob(Entity target)
	    {
	        float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
	        int i = 0;

	        if (target instanceof EntityLivingBase)
	        {
	            f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)target);
	            i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)target);
	        }

	        int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0)
            {
                target.setFire(j * 4);
            }
	        
	        boolean flag = target.attackEntityFrom(DamageSource.causeMobDamage(this), f);

	        if (flag)
	        {
	            if (i > 0)
	            {
	                target.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
	                this.motionX *= 0.6D;
	                this.motionZ *= 0.6D;
	            }

	            this.dealDamage(this, target);
	            this.onAttacking(target);
	        }

	        return flag;
	    }
	   
	   public void dealDamage(EntityLivingBase attacker, Entity target) {
           if (target instanceof EntityLivingBase)
           {
               EnchantmentHelper.func_151384_a((EntityLivingBase)target, attacker);
           }

           EnchantmentHelper.func_151385_b(attacker, target);
	   }
	   
	   public void onAttacking(Entity target) {
		   this.setLastAttacker(target instanceof EntityLivingBase ? (EntityLivingBase)target : null);
	   }
	   
	   public float computeFallDistance(float fallDistance) {
		   return fallDistance;
	   }
	   
	   public ItemStack eatFood(World world, ItemStack stack) {
		   return EntityLivingFuture.eatFoodBody(this, world, stack);
	   }
	   
		@Override
		public ItemStack getPickedResult(MovingObjectPosition target) {
			int eggID = EggHelper.getIDForClass(getClass());
			return eggID != -1 ? new ItemStack(Items.spawn_egg, 1, eggID) : super.getPickedResult(target);
		}
}
