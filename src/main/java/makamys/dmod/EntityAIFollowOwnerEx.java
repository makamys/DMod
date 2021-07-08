package makamys.dmod;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwnerEx extends EntityAIBase
{
    private EntityLiving thePet;
    private ITameable thePetI;
    private EntityLivingBase theOwner;
    World theWorld;
    private double field_75336_f;
    private PathNavigate petPathfinder;
    private int field_75343_h;
    float maxDist;
    float minDist;
    private boolean field_75344_i;
    private double minTeleportDistSq;

    public EntityAIFollowOwnerEx(EntityLiving p_i1625_1_, double p_i1625_2_, float p_i1625_4_, float p_i1625_5_, double minTeleportDistSq)
    {
    	if(!(p_i1625_1_ instanceof ITameable)) {
    		throw new IllegalArgumentException("The pet entity must implement ITameable");
    	}
        this.thePet = p_i1625_1_;
        this.thePetI = (ITameable)p_i1625_1_;
        this.theWorld = p_i1625_1_.worldObj;
        this.field_75336_f = p_i1625_2_;
        this.petPathfinder = p_i1625_1_.getNavigator();
        this.minDist = p_i1625_4_;
        this.maxDist = p_i1625_5_;
        this.minTeleportDistSq = minTeleportDistSq;
        this.setMutexBits(3);
    }
    
    public EntityAIFollowOwnerEx(EntityLiving p_i1625_1_, double p_i1625_2_, float p_i1625_4_, float p_i1625_5_)
    {
    	this(p_i1625_1_, p_i1625_2_, p_i1625_4_, p_i1625_5_, Double.POSITIVE_INFINITY);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = this.thePetI.getPetOwner();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (this.thePetI.isPetSitting())
        {
            return false;
        }
        else if (this.thePet.getDistanceSqToEntity(entitylivingbase) < Math.pow(this.minDist * (this.thePet.getEntitySenses().canSee(entitylivingbase) ? 1.0 : 0.5), 2))
        {
            return false;
        }
        else
        {
            this.theOwner = entitylivingbase;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.petPathfinder.noPath() && (this.thePet.getDistanceSqToEntity(this.theOwner) > Math.pow(this.maxDist * (this.thePet.getEntitySenses().canSee(this.theOwner) ? 1.0 : 0.5), 2)) && !this.thePetI.isPetSitting();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.field_75343_h = 0;
        this.field_75344_i = this.thePet.getNavigator().getAvoidsWater();
        this.thePet.getNavigator().setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.theOwner = null;
        this.petPathfinder.clearPathEntity();
        this.thePet.getNavigator().setAvoidsWater(this.field_75344_i);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());

        if (!this.thePetI.isPetSitting())
        {
            if (--this.field_75343_h <= 0)
            {
                this.field_75343_h = 10;

                if (!this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.field_75336_f))
                {
                    if (!this.thePet.getLeashed())
                    {
                        if (this.thePet.getDistanceSqToEntity(this.theOwner) >= minTeleportDistSq)
                        {
                            int i = MathHelper.floor_double(this.theOwner.posX) - 2;
                            int j = MathHelper.floor_double(this.theOwner.posZ) - 2;
                            int k = MathHelper.floor_double(this.theOwner.boundingBox.minY);

                            for (int l = 0; l <= 4; ++l)
                            {
                                for (int i1 = 0; i1 <= 4; ++i1)
                                {
                                    if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(this.theWorld, i + l, k - 1, j + i1) && !this.theWorld.getBlock(i + l, k, j + i1).isNormalCube() && !this.theWorld.getBlock(i + l, k + 1, j + i1).isNormalCube())
                                    {
                                        this.thePet.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.thePet.rotationYaw, this.thePet.rotationPitch);
                                        this.petPathfinder.clearPathEntity();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}