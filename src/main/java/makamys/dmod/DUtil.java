package makamys.dmod;

import java.util.UUID;

import net.minecraft.world.biome.BiomeGenBase;

public class DUtil {
	public static UUID UUIDorNullFromString(String str) {
		return str == null || str.isEmpty() ? null : UUID.fromString(str);
	}
	
	public static BiomeGenBase getMutation(BiomeGenBase base) {
		return BiomeGenBase.getBiomeGenArray()[base.biomeID + 128];
	}
}
