package makamys.dmod.client.tooltip;


import java.awt.Dimension;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.gui.GuiDraw.ITooltipLineHandler;
import net.minecraft.item.ItemStack;

public class BundleTooltipHandler implements ITooltipLineHandler {

	private ItemStack itemStack;
	
	public BundleTooltipHandler(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	@Override
	public Dimension getSize() {
		return new Dimension(16, 16);
	}

	@Override
	public void draw(int x, int y) {
		GuiDraw.drawRect(x, y, 16, 16, 0xFF00FF00);
	}

}
