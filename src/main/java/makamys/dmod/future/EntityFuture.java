package makamys.dmod.future;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class EntityFuture {

	public static Vec3 getRotationVector(Entity dis) {
	      return getRotationVector(dis.rotationPitch, dis.rotationYaw);
	   }
	
	protected static final Vec3 getRotationVector(float pitch, float yaw) {
	      float f = pitch * 0.017453292F;
	      float g = -yaw * 0.017453292F;
	      float h = MathHelper.cos(g);
	      float i = MathHelper.sin(g);
	      float j = MathHelper.cos(f);
	      float k = MathHelper.sin(f);
	      return Vec3.createVectorHelper((double)(i * j), (double)(-k), (double)(h * j));
	   }
}
