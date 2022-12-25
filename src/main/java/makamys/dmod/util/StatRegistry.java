package makamys.dmod.util;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;

// TODO support crafting and depletion too
public class StatRegistry {
    
    public static StatRegistry instance = new StatRegistry();
    
    private static List<RegistryInfo> items = new ArrayList<>();
    
    private boolean applied = false;
    
    public static void registerItem(Item item) {
        if (item != null) {
            items.add(new RegistryInfo(item));
        }
    }
    
    private static void clearIDs() {
        for(RegistryInfo info : items) {
            StatList.itemStats.remove(info.stat);
            StatList.objectUseStats[info.id] = null;
            
            info.id = -1;
        }
    }
    
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        for(RegistryInfo info : items) {
            Item item = info.item;
            info.id = Item.getIdFromItem(item);
            if(info.stat == null) {
                info.stat = (new StatCrafting("stat.useItem." + item.getUnlocalizedName(), new ChatComponentTranslation("stat.useItem", new Object[] {(new ItemStack(item)).func_151000_E()}), item)).registerStat();
            }
            
            StatList.objectUseStats[info.id] = info.stat;

            if (!(item instanceof ItemBlock)) {
                StatList.itemStats.add(info.stat);
            }
        }
        applied = true;
    }
    
    @EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        if(applied) {
            clearIDs();
            applied = false;
        }
    }
    
    private static class RegistryInfo {
        public Item item;
        public int id = -1;
        public StatBase stat;
        
        public RegistryInfo(Item item) {
            this.item = item;
        }
    }
    
}
