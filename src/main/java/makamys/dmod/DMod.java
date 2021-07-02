package makamys.dmod;

import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.EntityList;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import makamys.dmod.api.FutureRegistry;
import net.minecraft.entity.EntityList.EntityEggInfo;

@Mod(modid = DMod.MODID, version = DMod.VERSION)
public class DMod
{
    public static final String MODID = "dmod";
    public static final String VERSION = "0.0";
    
    @Instance(MODID)
	public static DMod instance;

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	instance = this;
        
        EntityRegistry.registerModEntity(EntityFox.class, "fox", 0, instance, 64, 1, true);
        RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, new RenderFox(new ModelFox(), 0.4F));
        
        int eggID = 0;
        while(EntityList.getStringFromID(eggID) != null) {
        	eggID++;
        }
        
        EntityList.IDtoClassMapping.put(eggID, EntityFox.class);
		EntityList.entityEggs.put(eggID, new EntityEggInfo(eggID, 0xFF8000, 0));
		
		registerCompatItems();
    }
    
    public void registerCompatItems() {
    	if(Loader.isModLoaded("etfuturum")) {
    		FutureRegistry.instance.registerFoxBreedingItem(is -> is.getItem() == ModItems.sweet_berries);
    	}
    }
}
