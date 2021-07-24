package makamys.dmod.proxy;

import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import makamys.dmod.client.render.ModelFox;
import makamys.dmod.client.render.RenderFox;
import makamys.dmod.client.tooltip.DTooltipHandler;
import makamys.dmod.entity.EntityFox;
import makamys.dmod.future.item.ItemFuture;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class DProxyClient extends DProxyCommon {
	
	@Override
	public void init() {
		super.init();
		RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, new RenderFox(new ModelFox(), 0.4F));
		
		// TODO don't crash if chicken is not present
		if(Loader.isModLoaded("CodeChickenCore")) {
			GuiContainerManager.addTooltipHandler(new DTooltipHandler());
		}
	}
	
	@SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
    	if(event.itemStack.getItem() instanceof ItemFuture) {
    		((ItemFuture)event.itemStack.getItem()).appendTooltip(event.itemStack, event.entity.worldObj, event.toolTip);
    	}
    }
	
}
