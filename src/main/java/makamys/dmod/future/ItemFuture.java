package makamys.dmod.future;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemFuture {
	
	public static ItemStack finishUsing(Item dis, ItemStack stack, World world, EntityLiving user) {
		return dis instanceof ItemFood ? EntityLivingFuture.eatFood(user, world, stack) : stack;
	}
	
}
