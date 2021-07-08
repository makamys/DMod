package makamys.dmod;

import net.minecraft.entity.EntityLivingBase;

public interface ITameable {
	
	public EntityLivingBase getPetOwner();
	
	public default boolean isPetSitting() {
		return false;
	}
	
}
