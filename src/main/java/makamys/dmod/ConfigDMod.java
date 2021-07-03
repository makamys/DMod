package makamys.dmod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class ConfigDMod {
	
	public static List<Item> foxBreedingItems;
	public static List<Class<Entity>> rabbitEntities;
	
	public static boolean wolvesTargetFoxes;
	
	private static List<Item> resolveItemListOrDefault(Configuration config, String propName, String propCat, String[] propDefault, String propComment, Item... defaults){
		String[] list = config.getStringList(propName, propCat, propDefault, propComment);
		List<Item> items = new ArrayList<>();
		for(String itemStr : list) {
			Object itemObj = Item.itemRegistry.getObject(itemStr);
			if(itemObj != null) {
				items.add((Item)itemObj);
			}
		}
		if(items.isEmpty() && list.length > 0) {
			System.out.println("Couldn't resolve any of the items in " + propCat + "." + propName + ", falling back to defaults");
			items = Arrays.asList(defaults);
		}
		
		System.out.println("Resolved " + propCat + "." + propName + " to " + items.stream().map(i -> i.getUnlocalizedName()).collect(Collectors.toList()));
		return items;
	}
	
	// nice copypasta
	private static List<Class<Entity>> resolveEntityClassListOrDefault(Configuration config, String propName, String propCat, String[] propDefault, String propComment, Class<Entity>... defaults){
		String[] list = config.getStringList(propName, propCat, propDefault, propComment);
		List<Class<Entity>> items = new ArrayList<>();
		for(String itemStr : list) {
			Object itemObj = EntityList.stringToClassMapping.get(itemStr);
			if(itemObj != null) {
				items.add((Class<Entity>)itemObj);
			}
		}
		if(items.isEmpty() && list.length > 0) {
			System.out.println("Couldn't resolve any of the entity names in " + propCat + "." + propName + ", falling back to defaults");
			items = Arrays.asList(defaults);
		}
		
		System.out.println("Resolved " + propCat + "." + propName + " to " + items.stream().map(e -> EntityList.classToStringMapping.get(e)).collect(Collectors.toList()));
		return items;
	}
	
	public static void reload(boolean resolve) {
		Configuration config = new Configuration(new File(Launch.minecraftHome, "config/dmod.cfg"));
        
        config.load();

        if(resolve) {
	        foxBreedingItems =
	        		resolveItemListOrDefault(config, "foxBreedingItems", "Fox", new String[]{"etfuturum:sweet_berries"}, "Falls back to wheat if none of the items can be resolved", Items.wheat);
	        rabbitEntities =
	        		resolveEntityClassListOrDefault(config, "rabbitEntities", "Fox", new String[]{"etfuturum.rabbit"}, "");
        }
        
        wolvesTargetFoxes = config.getBoolean("wolvesTargetFoxes", "Mixins", true, "");
    
        if (config.hasChanged()) 
        {
            config.save();
        }
	}
	
}
