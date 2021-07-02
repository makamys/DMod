package makamys.dmod.future;

import java.util.function.Predicate;

import makamys.dmod.constants.AIMutex;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

public class EntityAIModernAvoidEntity extends EntityAIBase {
    public static final IEntitySelector alive = new IEntitySelector()
    {
        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isEntityApplicable(Entity p_82704_1_)
        {
            return p_82704_1_.isEntityAlive();
        }
    };
    
    public final IEntitySelector field_98218_a = new IEntitySelector()
    {
        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isEntityApplicable(Entity p_82704_1_)
        {
            return p_82704_1_.isEntityAlive() && EntityAIModernAvoidEntity.this.mob.getEntitySenses().canSee(p_82704_1_);
        }
    };
	
	   protected final EntityCreature mob;
	   private final double slowSpeed;
	   private final double fastSpeed;
	   protected Entity targetEntity;
	   protected final float fleeDistance;
	   protected PathEntity fleePath;
	   protected final PathNavigate fleeingEntityNavigation;
	   protected final Class<?> classToFleeFrom;
	   protected final Predicate<EntityLivingBase> extraInclusionSelector;
	   protected final Predicate<EntityLivingBase> inclusionSelector;
	   private final TargetPredicate withinRangePredicate;

	   public EntityAIModernAvoidEntity(EntityCreature mob, Class<?> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
	      this(mob, fleeFromType, (livingEntity) -> {
		         return true;
		      }, distance, slowSpeed, fastSpeed, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
	   }

	   public EntityAIModernAvoidEntity(EntityCreature mob, Class<?> fleeFromType, Predicate<EntityLivingBase> extraInclusionSelector, float distance, double slowSpeed, double fastSpeed, Predicate<EntityLivingBase> inclusionSelector) {
	      this.mob = mob;
	      this.classToFleeFrom = fleeFromType;
	      this.extraInclusionSelector = extraInclusionSelector;
	      this.fleeDistance = distance;
	      this.slowSpeed = slowSpeed;
	      this.fastSpeed = fastSpeed;
	      this.inclusionSelector = inclusionSelector;
	      this.fleeingEntityNavigation = mob.getNavigator();
	      this.setMutexBits(AIMutex.MOVE);
	      this.withinRangePredicate = (new TargetPredicate()).setBaseMaxDistance((double)distance).setPredicate(inclusionSelector.and(extraInclusionSelector));
	   }

	   public EntityAIModernAvoidEntity(EntityCreature fleeingEntity, Class<?> classToFleeFrom, float fleeDistance, double fleeSlowSpeed, double fleeFastSpeed, Predicate<EntityLivingBase> inclusionSelector) {
	      this(fleeingEntity, classToFleeFrom, (livingEntity) -> {
	         return true;
	      }, fleeDistance, fleeSlowSpeed, fleeFastSpeed, inclusionSelector);
	   }

	   @Override
	   public boolean shouldExecute() {
		   this.targetEntity = EntityViewEmulator.getClosestEntityIncludingUngeneratedChunks(this.mob.worldObj,
				   this.classToFleeFrom,
					this.withinRangePredicate, this.mob, this.mob.posX, this.mob.posY, this.mob.posZ,
					this.mob.boundingBox.expand((double) this.fleeDistance, 3.0D, (double) this.fleeDistance));
			if (this.targetEntity == null) {
				return false;
			} else {
				Vec3 vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.mob, 16, 7,
						Vec3.createVectorHelper(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ));
				if (vec3d == null) {
					return false;
				} else if (this.targetEntity.getDistanceSq(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord) < this.targetEntity
						.getDistanceSqToEntity(this.mob)) {
					return false;
				} else {
					this.fleePath = this.fleeingEntityNavigation.getPathToXYZ(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord);
					return this.fleePath != null;
				}
			}
	   }

	   @Override
	   public boolean continueExecuting() {
	      return !this.fleeingEntityNavigation.noPath();
	   }

	   @Override
	   public void startExecuting() {
	      this.fleeingEntityNavigation.setPath(this.fleePath, this.slowSpeed);
	   }

	   @Override
	   public void resetTask() {
	      this.targetEntity = null;
	   }

	   @Override
	   public void updateTask() {
	      if (this.mob.getDistanceSqToEntity(this.targetEntity) < 49.0D) {
	         this.mob.getNavigator().setSpeed(this.fastSpeed);
	      } else {
	         this.mob.getNavigator().setSpeed(this.slowSpeed);
	      }

	   }
	}