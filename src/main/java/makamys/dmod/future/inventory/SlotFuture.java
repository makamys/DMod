package makamys.dmod.future.inventory;

import java.util.Optional;

import makamys.dmod.future.item.ItemStackFuture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotFuture {
    
    public static ItemStack insertStack(Slot dis, ItemStack stack) {
        return insertStack(dis, stack, stack.stackSize);
    }

    /*** can return an item stack of size 0 that should be converted to null */
    public static ItemStack insertStack(Slot dis, ItemStack stack, int count) {
        if (stack != null && dis.isItemValid(stack)) {
            ItemStack itemStack = dis.getStack();
            int slotItemCount = itemStack != null ? itemStack.stackSize : 0;
            int i = Math.min(Math.min(count, stack.stackSize), getMaxItemCount(dis, stack) - slotItemCount);
            if (itemStack == null) {
                dis.putStack(stack.splitStack(i));
            } else if (ItemStackFuture.canCombine(itemStack, stack)) {
                ItemStackFuture.decrement(stack, i);
                ItemStackFuture.increment(itemStack, i);
                dis.putStack(itemStack);
            }

            return ItemStackFuture.oldify(stack);
        } else {
            return ItemStackFuture.oldify(stack);
        }
    }

    public static int getMaxItemCount(Slot dis, ItemStack stack) {
        return Math.min(dis.getSlotStackLimit(), stack.getMaxStackSize());
    }
    
    public static ItemStack takeStackRange(Slot dis, int min, int max, EntityPlayer player) {
        ItemStack stack = tryTakeStackRange(dis, min, max, player);
        if(stack != null) {
            dis.onPickupFromSlot(player, stack);
        }
        return stack;
    }
    
    public static ItemStack tryTakeStackRange(Slot dis, int min, int max, EntityPlayer player) {
        if (!dis.canTakeStack(player)) {
            return null;
        } else if (!canTakePartial(dis, player) && max < dis.getStack().stackSize) {
            return null;
        } else {
            min = Math.min(min, max);
            ItemStack itemStack = dis.decrStackSize(min);
            if (itemStack == null) {
                return null;
            } else {
                if (dis.getStack() == null) {
                    dis.putStack(null);
                }

                return itemStack;
            }
        }
    }
    
    public static boolean canTakePartial(Slot dis, EntityPlayer player) {
        return dis.canTakeStack(player) && dis.isItemValid(dis.getStack());
    }
    
}
