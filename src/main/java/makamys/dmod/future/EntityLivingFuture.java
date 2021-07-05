package makamys.dmod.future;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityLivingFuture {
	public static ItemStack eatFood(EntityLivingBase dis, World world, ItemStack stack) {
		if(dis instanceof EntityAnimalFuture) {
			return ((EntityAnimalFuture)dis).eatFood(world, stack);
		} else {
			return eatFoodBody(dis, world, stack);
		}
	}
	
	public static ItemStack eatFoodBody(EntityLivingBase dis, World world, ItemStack stack) {
		if (stack.getItem() instanceof ItemFood) {
			world.playSound(dis.posX, dis.posY, dis.posZ, ((EntityLivingFutured)dis).getEatSound(stack), 1.0F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F, false);
			EntityLivingFuture.applyFoodEffects(dis, stack, world, dis);
			if (!(dis instanceof EntityPlayer) || !((EntityPlayer) dis).capabilities.isCreativeMode) {
				ItemStackFuture.decrement(stack, 1);
			}
		}
		return stack;
	}
	
	public static void applyFoodEffects(EntityLivingBase dis, ItemStack stack, World world, EntityLivingBase targetEntity) {
		Item item = stack.getItem();
		if (item instanceof ItemFood) {
			ItemFood food = (ItemFood)item;
	        if (!world.isRemote && food.potionId > 0 && world.rand.nextFloat() < food.potionEffectProbability)
	        {
	            dis.addPotionEffect(new PotionEffect(food.potionId, food.potionDuration * 20, food.potionAmplifier));
	        }
		}

	}
}
