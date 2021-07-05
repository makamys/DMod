package makamys.dmod;

import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;
import java.util.stream.Collectors;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import makamys.dmod.future.EntityAnimalFuture;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.WorldEvent;

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
    	MinecraftForge.EVENT_BUS.register(instance);
        
        EntityRegistry.registerModEntity(EntityFox.class, "fox", 0, instance, 64, 1, true);
        RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, new RenderFox(new ModelFox(), 0.4F));
        
        List<BiomeGenBase> foxBiomes = DUtil.getBiomesMatchingTag(BiomeDictionary.Type.CONIFEROUS);
    	System.out.println("Fox spawn biomes: " + String.join(", ", foxBiomes.stream().map(b -> b.biomeName + " (" + b.getClass().getName() + ")").collect(Collectors.toList())));
        EntityRegistry.addSpawn(EntityFox.class, 8, 2, 4, EnumCreatureType.creature, foxBiomes.toArray(new BiomeGenBase[] {}));
        
        int eggID = 0;
        while(EntityList.getStringFromID(eggID) != null) {
        	eggID++;
        }
        
        EntityList.IDtoClassMapping.put(eggID, EntityFox.class);
		EntityList.entityEggs.put(eggID, new EntityEggInfo(eggID, 0xFF8000, 0));
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
    	ConfigDMod.reload(true);
    }
    
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
    	if(event.entity instanceof EntityAnimalFuture) {
    		event.distance = ((EntityAnimalFuture)event.entity).computeFallDistance(event.distance);
    	}
    }
}
