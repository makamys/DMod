package makamys.dmod.future;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
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
	   public boolean attackEntityAsMob(Entity p_70652_1_)
	    {
	        float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
	        int i = 0;

	        if (p_70652_1_ instanceof EntityLivingBase)
	        {
	            f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)p_70652_1_);
	            i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)p_70652_1_);
	        }

	        boolean flag = p_70652_1_.attackEntityFrom(DamageSource.causeMobDamage(this), f);

	        if (flag)
	        {
	            if (i > 0)
	            {
	                p_70652_1_.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
	                this.motionX *= 0.6D;
	                this.motionZ *= 0.6D;
	            }

	            int j = EnchantmentHelper.getFireAspectModifier(this);

	            if (j > 0)
	            {
	                p_70652_1_.setFire(j * 4);
	            }

	            if (p_70652_1_ instanceof EntityLivingBase)
	            {
	                EnchantmentHelper.func_151384_a((EntityLivingBase)p_70652_1_, this);
	            }

	            EnchantmentHelper.func_151385_b(this, p_70652_1_);
	        }

	        return flag;
	    }
	   
	   public float computeFallDistance(float fallDistance) {
		   return fallDistance;
	   }
	   
	   public ItemStack eatFood(World world, ItemStack stack) {
		   return EntityLivingFuture.eatFoodBody(this, world, stack);
	   }
}
