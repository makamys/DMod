package makamys.dmod.inventory;

import codechicken.nei.guihook.IContainerSlotClickHandler;
import makamys.dmod.future.item.IItemFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DSlotClickHandler implements IContainerSlotClickHandler {

	@Override
	public void beforeSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, int modifier) {}

	// See https://wiki.vg/Protocol#Click_Window for explanation of arguments
	@Override
	public boolean handleSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, int modifier,
			boolean eventconsumed) {
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
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				ItemStack cursor = player.inventory.getItemStack();
				
				ItemStack stack = slot.getStack();
				if(cursor != null && cursor.getItem() instanceof IItemFuture) {
					eventconsumed |= ((IItemFuture)cursor.getItem()).onStackClicked(stack, slot, button, player);
				} else if(stack != null && stack.getItem() instanceof IItemFuture) {
					eventconsumed |= ((IItemFuture)stack.getItem()).onClicked(stack, stack, slot, button, player);
				}
			}
		}
		
		return eventconsumed;
	}

	@Override
	public void afterSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, int modifier) {}

}
