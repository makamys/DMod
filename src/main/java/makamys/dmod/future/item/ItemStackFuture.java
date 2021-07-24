package makamys.dmod.future.item;

import java.util.Optional;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemStackFuture {
	
	public static ItemStack finishUsing(ItemStack dis, World world, EntityLiving user) {
		return ItemFuture.finishUsing(dis.getItem(), dis, world, user);
	}
	
	public static void decrement(ItemStack dis, int count) {
		// TODO is this correct?
		increment(dis, -count);
	}
	
	public static void increment(ItemStack dis, int count) {
		dis.stackSize += count;
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
	
	public static boolean canCombine(ItemStack a, ItemStack b) {
		return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage() && ItemStack.areItemStackTagsEqual(a, b);
	}
	
	public static ItemStack oldify(ItemStack stack) {
		if(stack.stackSize == 0) {
			return null;
		}
		return stack;
	}
	
	public static NBTTagCompound getOrCreateNbt(ItemStack dis) {
		if(!dis.hasTagCompound()) {
			dis.stackTagCompound = new NBTTagCompound();
		}
		return dis.getTagCompound();
	}
}
