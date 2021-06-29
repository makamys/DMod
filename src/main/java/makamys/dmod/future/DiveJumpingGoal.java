package makamys.dmod.future;

import net.minecraft.entity.ai.EntityAIBase;

public abstract class DiveJumpingGoal extends EntityAIBase {
	public DiveJumpingGoal() {
		setMutexBits(1 | 4); // move, jump
	}
}