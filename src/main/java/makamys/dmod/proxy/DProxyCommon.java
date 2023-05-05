package makamys.dmod.proxy;

import static makamys.dmod.DModConstants.LOGGER;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import makamys.dmod.ConfigDMod;
import makamys.dmod.DMod;
import makamys.dmod.compat.Compat;
import makamys.dmod.entity.EntityFox;
import makamys.dmod.future.entity.passive.EntityAnimalFuture;
import makamys.dmod.util.DUtil;
import makamys.dmod.util.EggHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class DProxyCommon {
    
    public Cache<EntityItem, EntityPlayer> itemDropperMap = CacheBuilder.newBuilder().maximumSize(1000).build();
    
    public void init() {
        Compat.init();
        
        if(ConfigDMod.enableFox) {
            EntityRegistry.registerModEntity(EntityFox.class, "fox", 0, DMod.instance, 64, 1, true);
            
            List<BiomeGenBase> foxBiomes = DUtil.getBiomesMatchingTag(BiomeDictionary.Type.CONIFEROUS);
            LOGGER.debug("Fox spawn biomes: " + String.join(", ", foxBiomes.stream().map(b -> b.biomeName + " (" + b.getClass().getName() + ")").collect(Collectors.toList())));
            EntityRegistry.addSpawn(EntityFox.class, 8, 2, 4, EnumCreatureType.creature, foxBiomes.toArray(new BiomeGenBase[] {}));
            EggHelper.addEgg(EntityFox.class, 14005919, 13396256);
        }
    }
    
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if(event.entity instanceof EntityAnimalFuture) {
            event.distance = ((EntityAnimalFuture)event.entity).computeFallDistance(event.distance);
        }
    }
    
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if(event.source.getEntity() instanceof EntityFox) {
            EntityFox fox = (EntityFox)event.source.getEntity();
            int looting = fox.getLootingLevel();
            EntityLivingBase victim = event.entityLiving;
            if(victim instanceof EntityChicken) {
                int extraChicken = victim.getRNG().nextInt(1 + looting);
                for(EntityItem entityItem : event.drops) {
                    Item item = entityItem.getEntityItem().getItem();
                    if(item == Items.cooked_chicken || item == Items.chicken) {
                        entityItem.getEntityItem().stackSize += extraChicken;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onItemTossEvent(ItemTossEvent event) {
        itemDropperMap.put(event.entityItem, event.player);
    }
    
}
