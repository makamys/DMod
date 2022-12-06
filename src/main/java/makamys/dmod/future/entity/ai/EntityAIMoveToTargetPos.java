package makamys.dmod.future.entity.ai;

import makamys.dmod.constants.AIMutex;
import makamys.dmod.future.util.BlockPos;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAIMoveToTargetPos extends EntityAIBase {
   protected final EntityCreature mob;
   public final double speed;
   protected int cooldown;
   protected int tryingTime;
   private int safeWaitingTime;
   protected BlockPos targetPos;
   private boolean reached;
   private final int range;
   private final int maxYDifference;
   protected int lowestY;

   public EntityAIMoveToTargetPos(EntityCreature mob, double speed, int range) {
      this(mob, speed, range, 1);
   }

   public EntityAIMoveToTargetPos(EntityCreature mob, double speed, int range, int maxYDifference) {
      this.targetPos = BlockPos.ORIGIN;
      this.mob = mob;
      this.speed = speed;
      this.range = range;
      this.lowestY = 0;
      this.maxYDifference = maxYDifference;
      this.setMutexBits(AIMutex.MOVE | AIMutex.JUMP);
   }

   @Override
   public boolean shouldExecute() {
      if (this.cooldown > 0) {
         --this.cooldown;
         return false;
      } else {
         this.cooldown = this.getInterval(this.mob);
         return this.findTargetPos();
      }
   }

   protected int getInterval(EntityCreature mob) {
      return 200 + mob.getRNG().nextInt(200);
   }

   @Override
   public boolean continueExecuting() {
      return this.tryingTime >= -this.safeWaitingTime && this.tryingTime <= 1200 && this.isTargetPos(this.mob.worldObj, this.targetPos);
   }

   @Override
   public void startExecuting() {
      this.startMovingToTarget();
      this.tryingTime = 0;
      this.safeWaitingTime = this.mob.getRNG().nextInt(this.mob.getRNG().nextInt(1200) + 1200) + 1200;
   }

   protected void startMovingToTarget() {
      this.mob.getNavigator().tryMoveToXYZ((double)((float)this.targetPos.getX()) + 0.5D, (double)(this.targetPos.getY() + 1), (double)((float)this.targetPos.getZ()) + 0.5D, this.speed);
   }

   public double getDesiredSquaredDistanceToTarget() {
      return 1.0D;
   }

   protected BlockPos getTargetPos() {
      return this.targetPos.up();
   }

   @Override
   public void updateTask() {
      BlockPos blockPos = this.getTargetPos();
      if (this.mob.getDistanceSq(blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= Math.pow(this.getDesiredSquaredDistanceToTarget(), 2)) {
         this.reached = false;
         ++this.tryingTime;
         if (this.shouldResetPath()) {
            this.mob.getNavigator().tryMoveToXYZ((double)((float)blockPos.getX()) + 0.5D, (double)blockPos.getY(), (double)((float)blockPos.getZ()) + 0.5D, this.speed);
         }
      } else {
         this.reached = true;
         --this.tryingTime;
      }

   }

   public boolean shouldResetPath() {
      return this.tryingTime % 40 == 0;
   }

   protected boolean hasReached() {
      return this.reached;
   }

   protected boolean findTargetPos() {
      int i = this.range;
      int j = this.maxYDifference;
      int mobX = MathHelper.floor_double(mob.posX);
      int mobY = MathHelper.floor_double(mob.posY);
      int mobZ = MathHelper.floor_double(mob.posZ);
      int bx, by, bz;

      for(int k = this.lowestY; k <= j; k = k > 0 ? -k : 1 - k) {
         for(int l = 0; l < i; ++l) {
            for(int m = 0; m <= l; m = m > 0 ? -m : 1 - m) {
               for(int n = m < l && m > -l ? l : 0; n <= l; n = n > 0 ? -n : 1 - n) {
                  bx = mobX + m;
                  by = mobY + k - 1;
                  bz = mobZ + n;
                  if (this.mob.isWithinHomeDistance(bx, by, bz) && this.isTargetPos(this.mob.worldObj, bx, by, bz)) {
                     this.targetPos = new BlockPos(bx, by, bz);
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean isTargetPos(World world, BlockPos pos) {
       return this.isTargetPos(world, pos.getX(), pos.getY(), pos.getZ());
   }
   
   protected abstract boolean isTargetPos(World world, int bx, int by, int bz);
}
