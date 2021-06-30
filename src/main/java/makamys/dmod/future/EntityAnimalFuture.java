package makamys.dmod.future;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class EntityAnimalFuture extends EntityAnimal implements EntityLivingFutured {

	public EntityAnimalFuture(World p_i1681_1_) {
		super(p_i1681_1_);
	}
	
	@Override
	public boolean interact(EntityPlayer player) {
	      ItemStack itemStack = player.inventory.getCurrentItem();
	      AnimalEntityFutured disF = (AnimalEntityFutured)this;
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
}
