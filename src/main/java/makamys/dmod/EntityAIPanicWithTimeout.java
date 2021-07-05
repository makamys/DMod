package makamys.dmod;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIPanic;

public class EntityAIPanicWithTimeout extends EntityAIPanic {

	protected EntityCreature theCreature;
	protected int startTime;
	protected int maxDuration;
	
	public EntityAIPanicWithTimeout(EntityCreature creature, double speed, int maxDuration) {
		super(creature, speed);
		this.theCreature = creature;
		this.maxDuration = maxDuration;
	}
	
	public EntityAIPanicWithTimeout(EntityCreature creature, double speed) {
		this(creature, speed, 7 * 20);
	}
	
	@Override
	public void startExecuting() {
		startTime = theCreature.ticksExisted; 
		super.startExecuting();
	}
	
	@Override
	public boolean continueExecuting() {
		if(isTimedOut()) {
			DMod.LOGGER.warn("Terminated runaway panic task of " + theCreature + " because it passed the timeout of " + maxDuration + " ticks");
		}
		return super.continueExecuting() && !isTimedOut();
	}
	
	private boolean isTimedOut() {
		return theCreature.ticksExisted >= startTime + maxDuration;
	}
	
	@Override
	public void resetTask() {
		if(isTimedOut()) {
			theCreature.getNavigator().clearPathEntity();
		}
		super.resetTask();
	}

}
