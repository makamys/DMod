package makamys.dmod.future.entity.item;

import net.minecraft.entity.item.EntityItem;

public class EntityItemFuture {
	
	public static boolean cannotPickUp(EntityItem dis) {
      return dis.delayBeforeCanPickup > 0;
	}
	
}
