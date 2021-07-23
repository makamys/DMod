package makamys.dmod.future.item;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemStackFuture {
	
	public static ItemStack finishUsing(ItemStack dis, World world, EntityLiving user) {
		return ItemFuture.finishUsing(dis.getItem(), dis, world, user);
	}
	
	public static void decrement(ItemStack dis, int count) {
		// TODO is this correct?
		dis.stackSize -= count;
		if(dis.stackSize < 0) {
			dis.stackSize = 0;
		}
	}
	
	public static boolean isEmpty(ItemStack dis) {
		if (dis == null) {
			return true;
		} else if (dis.getItem() != null/* && dis.getItem() != Items.AIR*/) {
			return dis.stackSize <= 0;
		} else {
			return true;
		}
	}
}
