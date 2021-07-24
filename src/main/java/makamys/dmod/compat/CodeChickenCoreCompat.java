package makamys.dmod.compat;

import codechicken.nei.guihook.GuiContainerManager;
import makamys.dmod.client.tooltip.DTooltipHandler;

public class CodeChickenCoreCompat {
	
	public static void init() {
		GuiContainerManager.addTooltipHandler(new DTooltipHandler());
	}
	
}
