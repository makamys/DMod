package makamys.dmod;

import cpw.mods.fml.common.registry.GameRegistry;
import makamys.dmod.item.IConfigurable;
import makamys.dmod.item.ItemBundle;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

// Class structure insprired by Et Futurum

public class DModItems {
	
	public static final Item bundle = initItem(new ItemBundle());
	
	public static void preInit() {
    	
	}
	
	public static void postInit() {
		registerRecipes();
	}
	
	private static void registerRecipes() {
		for(Item bundleCraftingItem : ConfigDMod.bundleCraftingItems) {
			GameRegistry.addShapedRecipe(new ItemStack(bundle), new Object[] {"SLS", "L L", "LLL", 'L', bundleCraftingItem, 'S', Items.string});
		}
	}
	
	private static Item initItem(Item item) {
		if(!(item instanceof IConfigurable) || ((IConfigurable)item).isEnabled()) {
			String name = item.getUnlocalizedName();
	    	int firstDot = name.lastIndexOf('.');
	    	GameRegistry.registerItem(item, name.substring(firstDot + 1));
		}
    	return item;
    }
	
}
