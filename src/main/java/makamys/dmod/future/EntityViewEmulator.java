package makamys.dmod.future;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityViewEmulator {
	
	   public static Entity getClosestEntityIncludingUngeneratedChunks(World world, Class entityClass, TargetPredicate targetPredicate, EntityLiving entity, double x, double y, double z, AxisAlignedBB box) {
	      return getClosestEntity(world, world.selectEntitiesWithinAABB(entityClass, box, EntityAIModernAvoidEntity.alive), targetPredicate, entity, x, y, z);
	   }

	   public static Entity getClosestEntity(World world, List entityList, TargetPredicate targetPredicate, EntityLiving entity, double x, double y, double z) {
	      double d = -1.0D;
	      Entity livingEntity = null;
	      Iterator var13 = entityList.iterator();

	      while(true) {
	         Entity livingEntity2;
	         double e;
	         do {
	            do {
	               if (!var13.hasNext()) {
	                  return livingEntity;
	               }

	               livingEntity2 = (Entity)var13.next();
	            } while(!(livingEntity2 instanceof EntityLiving && targetPredicate.test(entity, (EntityLiving)livingEntity2)));

	            e = livingEntity2.getDistanceSq(x, y, z);
	         } while(d != -1.0D && e >= d);

	         d = e;
	         livingEntity = livingEntity2;
	      }
	   }
}
