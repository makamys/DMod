package makamys.dmod.future.item;

import makamys.dmod.future.entity.EntityLivingFuture;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemFuture {
	
	public static ItemStack finishUsing(Item dis, ItemStack stack, World world, EntityLiving user) {
		return dis instanceof ItemFood ? EntityLivingFuture.eatFood(user, world, stack) : stack;
	}
	
	public static boolean canBeNested(Item dis) {
		return true; // TODO don't allow shulker box, configurable blacklist?
	}
	
}
