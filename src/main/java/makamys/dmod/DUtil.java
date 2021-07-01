package makamys.dmod;

import java.util.UUID;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;

public class DUtil {
	public static UUID UUIDorNullFromString(String str) {
		return str == null || str.isEmpty() ? null : UUID.fromString(str);
	}
	
	public static BiomeGenBase getMutation(BiomeGenBase base) {
		return BiomeGenBase.getBiomeGenArray()[base.biomeID + 128];
	}
	
	public static String getItemStackParticleName(ItemStack p_71010_1_) {
        String s = "iconcrack_" + Item.getIdFromItem(p_71010_1_.getItem());

        if (p_71010_1_.getHasSubtypes())
        {
            s = s + "_" + p_71010_1_.getItemDamage();
        }
        return s;
	}
}
