package makamys.dmod.compat;

import codechicken.nei.guihook.GuiContainerManager;
import makamys.dmod.client.tooltip.DTooltipHandler;

public class NEICompat {
	
	public static void init() {
		GuiContainerManager.addTooltipHandler(new DTooltipHandler());
	}
	
}
