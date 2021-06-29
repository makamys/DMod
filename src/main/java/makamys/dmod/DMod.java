package makamys.dmod;

import net.minecraft.entity.EntityList;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
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
        
        EntityRegistry.registerModEntity(TestEntity.class, "test", 0, instance, 64, 1, true);
        
        int eggID = 0;
        while(EntityList.getStringFromID(eggID) != null) {
        	eggID++;
        }
        
        EntityList.IDtoClassMapping.put(eggID, TestEntity.class);
		EntityList.entityEggs.put(eggID, new EntityEggInfo(eggID, 0xFF8000, 0));
    }
}
