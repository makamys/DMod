package makamys.dmod.future.entity;

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
	
	public static int getHorizontalFacing(Entity entity) {
		return (MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) % 4;
	}
	
	public static int getMovementDirection(Entity entity) {
		// in vanilla 1.16.5 this is only implemented differently for boats and minecarts
		return getHorizontalFacing(entity);
	}
	
	public static Vec3 getVelocity(Entity dis) {
		return Vec3.createVectorHelper(dis.motionX, dis.motionY, dis.motionZ);
	}
	
	public static void setVelocity(Entity dis, Vec3 vel) {
		dis.motionX = vel.xCoord;
		dis.motionY = vel.yCoord;
		dis.motionZ = vel.zCoord;
	}
	
	public static double squaredHorizontalLength(Vec3 vector) {
		return vector.xCoord * vector.xCoord + vector.zCoord * vector.zCoord;
	}
}
