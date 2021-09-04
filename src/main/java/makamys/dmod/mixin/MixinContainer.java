package makamys.dmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import makamys.dmod.inventory.DSlotClickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

@Mixin(Container.class)
public class MixinContainer {
    
    @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
    private void preSlotClick(int p1, int p2, int p3, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if(DSlotClickHandler.onSlotClick((Container)(Object)this, p1, p2, p3, player)) {
            cir.setReturnValue(null);
        }
    }
}
