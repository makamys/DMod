package makamys.dmod.future.entity.passive;

import java.util.concurrent.Callable;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class AnimalEntityEmulator {
    
    /*public static boolean interact(EntityAnimal dis, EntityPlayer player, Callable<Boolean> superImpl) {
          ItemStack itemStack = player.inventory.getCurrentItem();
          AnimalEntityFutured disF = (AnimalEntityFutured)dis;
          if (dis.isBreedingItem(itemStack)) {
             int i = dis.getGrowingAge();
             if (!dis.worldObj.isRemote && i == 0 && disF.canEat(dis)) {
                if(disF.eat(dis, player, itemStack)) {
                    eat(dis, player, itemStack);
                }
                dis.func_146082_f(player);
                return true;
             }
             /*
             if (this.isBaby()) {
                this.eat(player, itemStack);
                this.growUp((int)((float)(-i / 20) * 0.1F), true);
                return ActionResult.success(this.world.isClient);
             }

             if (this.world.isClient) {
                return ActionResult.CONSUME;
             }*/
      /*    }
          
          try {
            return superImpl.call();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
       }
    
    public static void eat(EntityAnimal dis, EntityPlayer player, ItemStack itemStack) {
        if (!player.capabilities.isCreativeMode) {
            --itemStack.stackSize;

            if (itemStack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack) null);
            }
        }
    }*/
}
