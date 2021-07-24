package makamys.dmod.client.tooltip;

import java.util.List;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.IContainerTooltipHandler;
import makamys.dmod.item.ItemBundle;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class DTooltipHandler implements IContainerTooltipHandler {
	
	@Override
	public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
		return currenttip;
	}

	@Override
	public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip) {
		return currenttip;
	}

	@Override
	public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey,
			List<String> currenttip) {
		if(itemstack != null && itemstack.getItem() instanceof ItemBundle) {
			currenttip.add(1, GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(new BundleTooltipHandler(itemstack)));
		}
		return currenttip;
	}

}
