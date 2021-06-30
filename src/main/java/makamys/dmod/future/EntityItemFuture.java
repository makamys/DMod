package makamys.dmod.future;

import net.minecraft.entity.item.EntityItem;

public class EntityItemFuture {
	
	public static boolean cannotPickUp(EntityItem dis) {
      return dis.delayBeforeCanPickup > 0;
	}
	
}
