package makamys.dmod.future.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


public interface IItemFuture {
	
	default boolean onStackClicked(ItemStack stack, Slot slot, int button, EntityPlayer player) {
		return false;
	}

	default boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, int button, EntityPlayer player) {
		return false;
	}
	
	default void appendTooltip(ItemStack stack, World world, List<String> tooltip) {}

}
