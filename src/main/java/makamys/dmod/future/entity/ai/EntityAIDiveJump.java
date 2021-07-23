package makamys.dmod.future.entity.ai;

import makamys.dmod.constants.AIMutex;
import net.minecraft.entity.ai.EntityAIBase;

public abstract class EntityAIDiveJump extends EntityAIBase {
   public EntityAIDiveJump() {
      this.setMutexBits(AIMutex.MOVE | AIMutex.JUMP);
   }
}

