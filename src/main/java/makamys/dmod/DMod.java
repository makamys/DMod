package makamys.dmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import makamys.dmod.proxy.DProxyCommon;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = DMod.MODID, version = DMod.VERSION)
public class DMod
{
    public static final String MODID = "dmod";
    public static final String VERSION = "0.0";
    
    @Instance(MODID)
	public static DMod instance;
    
    @SidedProxy(clientSide = "makamys.dmod.proxy.DProxyClient", serverSide = "makamys.proxy.DProxyCommon")
    public static DProxyCommon proxy;
    
    public static final Logger LOGGER = LogManager.getLogger("dmod"); 

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	DModItems.init();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	instance = this;
    	MinecraftForge.EVENT_BUS.register(proxy);
        
    	proxy.init();
    }
}
