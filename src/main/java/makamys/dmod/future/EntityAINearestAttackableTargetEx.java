package makamys.dmod.future;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

public class EntityAINearestAttackableTargetEx extends EntityAINearestAttackableTarget {
	private IEntitySelector extraSelector;
	
	public EntityAINearestAttackableTargetEx(EntityCreature creature, Class targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, IEntitySelector extraSelector) {
		super(creature, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, null);
		this.extraSelector = extraSelector;
	}
	
	@Override
	protected boolean isSuitableTarget(EntityLivingBase p_75296_1_, boolean p_75296_2_) {
		return super.isSuitableTarget(p_75296_1_, p_75296_2_) && extraSelector.isEntityApplicable(p_75296_1_);
	}
}
