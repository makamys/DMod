package makamys.dmod.future;

import makamys.dmod.constants.AIMutex;
import net.minecraft.entity.ai.EntityAIBase;

public abstract class DiveJumpingGoal extends EntityAIBase {
	public DiveJumpingGoal() {
		setMutexBits(AIMutex.MOVE | AIMutex.JUMP);
	}
}