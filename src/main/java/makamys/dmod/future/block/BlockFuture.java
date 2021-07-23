package makamys.dmod.future.block;

import makamys.dmod.mixin.BlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockFuture {
	
	public static void dropStack(World world, int x, int y, int z, ItemStack stack, Block block) {
		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops") && !world.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			if (((BlockAccessor)block).getCaptureDrops().get())
	        {
				((BlockAccessor)block).getCapturedDrops().get().add(stack);
	            return;
	        }
	        float f = 0.7F;
	        double d0 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	        double d1 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	        double d2 = (double)(world.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
	        EntityItem entityitem = new EntityItem(world, (double)x + d0, (double)y + d1, (double)z + d2, stack);
	        entityitem.delayBeforeCanPickup = 10;
	        world.spawnEntityInWorld(entityitem);
        }
	}
	
}
