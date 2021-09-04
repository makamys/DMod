package makamys.dmod.future.predicate.entity;

import java.util.function.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class EntityPredicates {
    public static final Predicate<EntityLivingBase> EXCEPT_CREATIVE_OR_SPECTATOR = (entity) -> {
          return !(entity instanceof EntityPlayer) || /*!entity.isSpectator() && */!((EntityPlayer)entity).capabilities.isCreativeMode;
       };
}
