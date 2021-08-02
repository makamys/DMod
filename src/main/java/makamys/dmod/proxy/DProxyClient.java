package makamys.dmod.proxy;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import makamys.dmod.ConfigDMod;
import makamys.dmod.DMod;
import makamys.dmod.client.render.ModelFox;
import makamys.dmod.client.render.RenderFox;
import makamys.dmod.compat.NEICompat;
import makamys.dmod.entity.EntityFox;
import makamys.dmod.future.item.ItemFuture;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class DProxyClient extends DProxyCommon {
	
	@Override
	public void init() {
		super.init();
		
		if(ConfigDMod.enableFox) {
			RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, new RenderFox(new ModelFox(), 0.4F));
		}
		
		// TODO don't crash if chicken is not present
		if(Loader.isModLoaded("NotEnoughItems")) {
			NEICompat.init();
		} else {
			DMod.LOGGER.warn("NotEnoughItems was not found. Some optional features will not work.");
		}
	}
	
	@SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
    	if(event.itemStack.getItem() instanceof ItemFuture) {
    		((ItemFuture)event.itemStack.getItem()).appendTooltip(event.itemStack, event.entity.worldObj, event.toolTip);
    	}
    }
	
}
