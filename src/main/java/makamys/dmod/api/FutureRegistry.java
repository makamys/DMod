package makamys.dmod.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class FutureRegistry {
	
	public static FutureRegistry instance = new FutureRegistry();
	
	private List<Predicate<ItemStack>> foxBreedingItemPredicates = new ArrayList<>();
	
	public void registerFoxBreedingItem(Predicate<ItemStack> predicate) {
		foxBreedingItemPredicates.add(predicate);
	}
	
	public boolean isFoxBreedingItem(ItemStack is) {
		return foxBreedingItemPredicates.isEmpty() ? 
				is.getItem() == Items.wheat :
					foxBreedingItemPredicates.stream().anyMatch(pred -> pred.test(is));
	}
	
}
