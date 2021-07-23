package makamys.dmod.proxy;

import cpw.mods.fml.client.registry.RenderingRegistry;
import makamys.dmod.client.render.ModelFox;
import makamys.dmod.client.render.RenderFox;
import makamys.dmod.entity.EntityFox;

public class DProxyClient extends DProxyCommon {
	
	@Override
	public void init() {
		super.init();
		RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, new RenderFox(new ModelFox(), 0.4F));
	}
	
}
