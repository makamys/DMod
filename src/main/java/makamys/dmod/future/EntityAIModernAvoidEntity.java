package makamys.dmod.future;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;
import scala.NotImplementedError;

public class EntityAIModernAvoidEntity extends EntityAIBase {
    public static final IEntitySelector alive = new IEntitySelector()
    {
        private static final String __OBFID = "CL_00001575";
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
        private static final String __OBFID = "CL_00001575";
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
	   protected final Class classToFleeFrom;
	   protected final Predicate extraInclusionSelector;
	   protected final Predicate<EntityLiving> inclusionSelector;
	   private final TargetPredicate withinRangePredicate;

	   public EntityAIModernAvoidEntity(EntityCreature mob, Class fleeFromType, float distance, double slowSpeed, double fastSpeed) {
	      this(mob, fleeFromType, (livingEntity) -> {
		         return true;
		      }, distance, slowSpeed, fastSpeed, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
	   }

	   public EntityAIModernAvoidEntity(EntityCreature mob, Class fleeFromType, Predicate extraInclusionSelector, float distance, double slowSpeed, double fastSpeed, Predicate inclusionSelector) {
	      this.mob = mob;
	      this.classToFleeFrom = fleeFromType;
	      this.extraInclusionSelector = extraInclusionSelector;
	      this.fleeDistance = distance;
	      this.slowSpeed = slowSpeed;
	      this.fastSpeed = fastSpeed;
	      this.inclusionSelector = inclusionSelector;
	      this.fleeingEntityNavigation = mob.getNavigator();
	      this.setMutexBits(1); //this.setControls(EnumSet.of(Goal.Control.MOVE));
	      this.withinRangePredicate = (new TargetPredicate()).setBaseMaxDistance((double)distance).setPredicate(inclusionSelector.and(extraInclusionSelector));
	   }

	   public EntityAIModernAvoidEntity(EntityCreature fleeingEntity, Class classToFleeFrom, float fleeDistance, double fleeSlowSpeed, double fleeFastSpeed, Predicate<EntityLiving> inclusionSelector) {
	      this(fleeingEntity, classToFleeFrom, (livingEntity) -> {
	         return true;
	      }, fleeDistance, fleeSlowSpeed, fleeFastSpeed, inclusionSelector);
	   }

	   public boolean shouldExecute() {
		   this.targetEntity = EntityViewEmulator.getClosestEntityIncludingUngeneratedChunks(this.mob.worldObj,
				   this.classToFleeFrom,
					this.withinRangePredicate, this.mob, this.mob.posX, this.mob.posY, this.mob.posZ,
					this.mob.getBoundingBox().expand((double) this.fleeDistance, 3.0D, (double) this.fleeDistance));
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
	   /*
	   public boolean shouldExecute2() {
		if (this.classToFleeFrom == EntityPlayer.class) {
			if (this.mob instanceof EntityTameable && ((EntityTameable) this.mob).isTamed()) {
				return false;
			}

			this.targetEntity = this.mob.worldObj.getClosestPlayerToEntity(this.mob, (double) this.fleeDistance);

			if (this.targetEntity == null) {
				return false;
			}
		} else {
			List list = this.mob.worldObj.selectEntitiesWithinAABB(this.classToFleeFrom,
					this.mob.boundingBox.expand((double) this.fleeDistance, 3.0D, (double) this.fleeDistance),
					this.field_98218_a);

			if (list.isEmpty()) {
				return false;
			}

			this.targetEntity = (Entity) list.get(0);
		}

		Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.mob, 16, 7,
				Vec3.createVectorHelper(this.targetEntity.posX, this.targetEntity.posY, this.targetEntity.posZ));

		if (vec3 == null) {
			return false;
		} else if (this.targetEntity.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord) < this.targetEntity
				.getDistanceSqToEntity(this.mob)) {
			return false;
		} else {
			this.fleePath = this.fleeingEntityNavigation.getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
			return this.fleePath == null ? false : this.fleePath.isDestinationSame(vec3);
		}
	}*/
/*
	   public boolean shouldContinue() {
	      return !this.fleeingEntityNavigation.noPath();
	   }

	   public void start() {
	      this.fleeingEntityNavigation.setPath(this.fleePath, this.slowSpeed);
	   }

	   public void stop() {
	      this.targetEntity = null;
	   }

	   public void updateTask() {
	      if (this.mob.getDistanceSqToEntity(this.targetEntity) < 49.0D) {
	         this.mob.getNavigator().setSpeed(this.fastSpeed);
	      } else {
	         this.mob.getNavigator().setSpeed(this.slowSpeed);
	      }

	   }*/
	}