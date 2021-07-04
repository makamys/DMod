package makamys.dmod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor
    public ThreadLocal<Boolean> getCaptureDrops();
    
    @Accessor
    public ThreadLocal<List<ItemStack>> getCapturedDrops();
}