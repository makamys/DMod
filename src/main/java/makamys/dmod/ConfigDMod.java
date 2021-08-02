package makamys.dmod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;

import makamys.dmod.entity.EntityFox;
import makamys.dmod.util.WeightedRandomItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

public class ConfigDMod {
	
	public static List<Item> foxBreedingItems;
	public static List<WeightedRandomItem<Item>> foxMouthItems;
	public static List<Class<Entity>> rabbitEntities;
	public static EntityFox.AbilityMode foxAbilityMode;
	
	public static boolean wolvesTargetFoxes;
	public static boolean lootingFoxFix;
	
	public static boolean enableBundle;
	public static List<Item> bundleCraftingItems;
	public static Set<Item> bundleItemBlacklist;
	public static boolean compactBundleGUI;
	public static boolean durabilityBarColor;
	
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
			DMod.LOGGER.debug("Couldn't resolve any of the items in " + propCat + "." + propName + ", falling back to defaults");
			items = Arrays.asList(defaults);
		}
		
		DMod.LOGGER.debug("Resolved " + propCat + "." + propName + " to " + items.stream().map(i -> i.getUnlocalizedName()).collect(Collectors.toList()));
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
			DMod.LOGGER.debug("Couldn't resolve any of the entity names in " + propCat + "." + propName + ", falling back to defaults");
			items = Arrays.asList(defaults);
		}
		
		DMod.LOGGER.debug("Resolved " + propCat + "." + propName + " to " + items.stream().map(e -> EntityList.classToStringMapping.get(e)).collect(Collectors.toList()));
		return items;
	}
	
	private static <E extends Enum> E getEnum(Configuration config, String propName, String propCat, E propDefault, String propComment) {
		Map enumMap = EnumUtils.getEnumMap(propDefault.getClass());
		String[] valuesStr = (String[])enumMap.keySet().toArray(new String[]{});
		return (E)enumMap.get(config.getString(propName, propCat, propDefault.toString(), propComment, valuesStr));
	}
	
	public static void reload(boolean resolve) {
		Configuration config = new Configuration(new File(Launch.minecraftHome, "config/dmod.cfg"));
        
        config.load();

        if(resolve) {
	        foxBreedingItems =
	        		resolveItemListOrDefault(config, "foxBreedingItems", "Fox", new String[]{"etfuturum:sweet_berries"}, "Falls back to wheat if none of the items can be resolved", Items.wheat);
	        rabbitEntities =
	        		resolveEntityClassListOrDefault(config, "rabbitEntities", "Fox", new String[]{"etfuturum.rabbit"}, "");
	        foxMouthItems = Arrays.stream(config.getStringList("foxMouthItems", "Fox", new String[] {"emerald=5", "egg=15", "etfuturum:rabbit_foot=10", "etfuturum:rabbit_hide=10", "wheat=20", "leather=20", "feather=20"}, "item=weight pairs deciding the relative likelyhood of foxes spawning with certain items. Entries containing items that can't be resolved will be ignored."))
	        		.map(str -> parseWeightedItemEntry(str)).filter(p -> p != null).collect(Collectors.toList());
	        bundleCraftingItems = 
	        		resolveItemListOrDefault(config, "bundleCraftingItems", "bundle", new String[]{"etfuturum:rabbit_hide"}, "Falls back to leather if none of the items can be resolved", Items.leather);
	        bundleItemBlacklist = 
	        		new HashSet<>(resolveItemListOrDefault(config, "bundleItemBlacklist", "bundle", new String[]{"etfuturum:rabbit_hide"}, "Items that should not be allowed in a bundle"));
        }
        
        wolvesTargetFoxes = config.getBoolean("wolvesTargetFoxes", "Mixins", true, "");
        lootingFoxFix = config.getBoolean("wolvesTargetFoxes", "Mixins", true, "Make looting enchants of fox weapons have an effect");
        durabilityBarColor = config.getBoolean("durabilityBarColor", "Mixins", true, "Change the durability bar color of certain items (bundles)");
        
        enableBundle = config.getBoolean("enableBundle", "bundle", true, "");
        compactBundleGUI = config.getBoolean("compactBundleGUI", "bundle", false, "Remove extra spacing between rows in the bundle tooltip.");
        // TODO tweak the level requirements of each individual ability
        foxAbilityMode = getEnum(config, "foxAbilityMode", "fox", EntityFox.AbilityMode.NORMAL, "NORMAL: Foxes unlock abilities as they level up\nUNLOCK_ALL: All abilities are unlocked from the start\nUNLOCK_NONE: No abilities will ever be unlocked\nNote: changing this won't affect the amount of exp foxes have, just whether the abilities will be enabled or not");
        
        if (config.hasChanged()) 
        {
            config.save();
        }
	}
	
	private static WeightedRandomItem<Item> parseWeightedItemEntry(String str) {
    	String[] halves = str.split("=");
    	if(halves.length == 2) {
    		Object itemObj = Item.itemRegistry.getObject(halves[0]);
			if(itemObj != null) {
				Item item = (Item)itemObj;
				try {
					int weight = Integer.parseInt(halves[1]);
					return new WeightedRandomItem<>(weight, item);
				} catch(NumberFormatException e) {
					DMod.LOGGER.warn("Invalid weight (must be an integer): " + halves[1]);
				}
			} else {
				DMod.LOGGER.warn("No item called " + halves[0]);
			}
    	} else {
    		DMod.LOGGER.warn("Incorrect pair: " + str);
    	}
		return null;
	}
	
}
