package makamys.dmod;

import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public interface IBerryBushHandler {
	
	public boolean isBerryBush(World world, int x, int y, int z);
	public int getBerryBushAge(World world, int x, int y, int z);
	public int getMetaForNewAge(World world, int x, int y, int z, int newAge);
	public Item getSweetBerryItem();
	public boolean isBerryBushDamageSource(DamageSource source);
	
}
