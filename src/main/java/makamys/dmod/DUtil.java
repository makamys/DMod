package makamys.dmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

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
	
	public static List<BiomeGenBase> getBiomesMatchingTag(BiomeDictionary.Type tag){
		BiomeGenBase[] biomes = BiomeGenBase.getBiomeGenArray();
		List<BiomeGenBase> list = new ArrayList<>();
		for(int i = 0; i < biomes.length; i++) {
			BiomeGenBase biome = biomes[i];
			if(biome != null && BiomeDictionary.isBiomeOfType(biome, tag)) {
				list.add(biome);
				if(i + 128 < biomes.length) {
					BiomeGenBase mutatedMaybe = biomes[i + 128];
					if(biome.isEqualTo(mutatedMaybe)) {
						list.add(mutatedMaybe);
					}
				}
			}
		}
		return list;
	}
}
