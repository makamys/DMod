package makamys.dmod.future.world;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import makamys.dmod.future.entity.ai.TargetPredicate;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityViewEmulator {
    
       public static Entity getClosestEntityIncludingUngeneratedChunks(World world, Class entityClass, TargetPredicate targetPredicate, EntityLiving entity, double x, double y, double z, AxisAlignedBB box) {
          return getClosestEntity(world, world.selectEntitiesWithinAABB(entityClass, box, IEntitySelector.selectAnything/*EntityAIModernAvoidEntity.alive*/), targetPredicate, entity, x, y, z);
          // XXX alive? why?
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
                } while(!(livingEntity2 instanceof EntityLivingBase && targetPredicate.test(entity, (EntityLivingBase)livingEntity2)));

                e = livingEntity2.getDistanceSq(x, y, z);
             } while(d != -1.0D && e >= d);

             d = e;
             livingEntity = livingEntity2;
          }
       }
       
       public static List getTargets(World world, Class entityClass, TargetPredicate targetPredicate, EntityLiving targetingEntity, AxisAlignedBB box) {
              List list = world.selectEntitiesWithinAABB(entityClass, box, null);
              List list2 = Lists.newArrayList();
              Iterator var7 = list.iterator();

              while(var7.hasNext()) {
                 Entity livingEntity = (Entity)var7.next();
                 if (livingEntity instanceof EntityLiving && targetPredicate.test(targetingEntity, (EntityLiving)livingEntity)) {
                    list2.add(livingEntity);
                 }
              }

              return list2;
       }
}
