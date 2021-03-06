package makamys.dmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import makamys.dmod.proxy.DProxyCommon;
import makamys.dmod.util.StatRegistry;
import makamys.mclib.core.MCLib;
import makamys.mclib.core.MCLibModules;
import makamys.mclib.sloppydeploader.SloppyDepLoaderAPI;
import makamys.mclib.sloppydeploader.SloppyDependency;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = DMod.MODID, version = DMod.VERSION)
public class DMod
{
    public static final String MODID = "dmod";
    public static final String VERSION = "@VERSION@";
    
    @Instance(MODID)
    public static DMod instance;
    
    @SidedProxy(clientSide = "makamys.dmod.proxy.DProxyClient", serverSide = "makamys.dmod.proxy.DProxyCommon")
    public static DProxyCommon proxy;
    
    public static final Logger LOGGER = LogManager.getLogger("dmod");
    
    static {
        MCLib.init();
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DModItems.preInit();
        MCLibModules.updateCheckAPI.submitModTask(MODID, "@UPDATE_URL@");
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        instance = this;
        MinecraftForge.EVENT_BUS.register(proxy);
        
        proxy.init();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ConfigDMod.reload();
        DModItems.postInit();
    }
    
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        StatRegistry.instance.onServerStarting(event);
    }
    
    @EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        StatRegistry.instance.onServerStopped(event);
    }
}
