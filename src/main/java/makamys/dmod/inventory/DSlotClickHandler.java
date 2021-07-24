package makamys.dmod.inventory;

import makamys.dmod.future.item.ItemFuture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class DSlotClickHandler {

	// See https://wiki.vg/Protocol#Click_Window
	public static boolean onSlotClick(Container container, int slotNumber, int button, int modifier, EntityPlayer slotClickPlayer) {
		boolean eventconsumed = false;
		if(modifier == 0 || modifier == 5) {
			if(modifier == 5) {
				switch(button) {
				case 1:
					button = 0;
					break;
				case 5:
					button = 1;
					break;
				case 9:
					button = 2;
					break;
				default:
					button = -1;
				}
			}
			if(button >= 0 && button <= 2) {
				Slot slot = container.getSlot(slotNumber);
				EntityPlayer player = slotClickPlayer;
				ItemStack cursor = player.inventory.getItemStack();
				
				ItemStack stack = slot.getStack();
				if(cursor != null && cursor.getItem() instanceof ItemFuture) {
					eventconsumed |= ((ItemFuture)cursor.getItem()).onStackClicked(cursor, slot, button, player);
				} else if(stack != null && stack.getItem() instanceof ItemFuture) {
					eventconsumed |= ((ItemFuture)stack.getItem()).onClicked(stack, cursor, slot, button, player);
				}
				if(eventconsumed) {
					slot.onSlotChanged();
				}
			}
		}
		return eventconsumed;
	}

}
