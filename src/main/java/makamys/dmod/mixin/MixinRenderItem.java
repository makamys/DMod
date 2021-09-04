package makamys.dmod.mixin;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.dmod.future.item.ItemFuture;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
    
    ItemStack lastStack;
    
    @Inject(method = "renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"))
    private void preRenderItemOverlayIntoGUI(FontRenderer fontRenderer, TextureManager textureManager, ItemStack stack, int x, int y, String string, CallbackInfo ci) {
        lastStack = stack;
    }
    
    @ModifyVariable(method = "renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "STORE"), name = "l")
    private int getBarColor(int old) {
        if(lastStack.getItem() instanceof ItemFuture) {
            ItemFuture item = (ItemFuture)lastStack.getItem();
            if(item.getItemBarHasColor(lastStack)) {
                return item.getItemBarColor(lastStack);
            }
        }
        return old;
    }
    
}
