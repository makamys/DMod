package makamys.dmod.future;

import makamys.dmod.constants.AIMutex;
import net.minecraft.entity.ai.EntityAIBase;

public abstract class EntityAIDiveJump extends EntityAIBase {
   public EntityAIDiveJump() {
      this.setMutexBits(AIMutex.MOVE | AIMutex.JUMP);
   }
}

