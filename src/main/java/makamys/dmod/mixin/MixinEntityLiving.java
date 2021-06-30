package makamys.dmod.mixin;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import makamys.dmod.future.EntityAnimalFuture;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends EntityLivingBase {
	
	@Shadow
	protected float[] equipmentDropChances;
	@Shadow
	private boolean persistenceRequired;
	
	public MixinEntityLiving(World p_i1594_1_) {
		super(p_i1594_1_);
	}

	@Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true)
	public void preOnLivingUpdate(CallbackInfo ci) {
		if(EntityAnimalFuture.class.isInstance(this.getClass())) {
			ci.cancel();
			EntityLiving dis = ((EntityLiving)(Object)this);
			
			super.onLivingUpdate();
			
			this.worldObj.theProfiler.startSection("looting");
			
			if (!this.worldObj.isRemote && dis.canPickUpLoot() && this.isEntityAlive() && !this.dead && this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
				List list = this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.boundingBox.expand(1.0D, 0.0D, 1.0D));
	            Iterator iterator = list.iterator(); 

		         while(iterator.hasNext()) {
		        	 EntityItem entityitem = (EntityItem)iterator.next();
		            if (!entityitem.isDead && entityitem.getEntityItem() != null) {
		               this.loot(entityitem);
		            }
		         }
		      }
			
			this.worldObj.theProfiler.endSection();
		}
	}
	
	protected void loot(EntityItem item) {
	      ItemStack itemStack = item.getEntityItem();
	      if (this.tryEquip(itemStack)) {
	         // this.method_29499(item); advancement check
	         this.onItemPickup(item, itemStack.stackSize);
	         item.setDead();
	      }

	   }

	   public boolean tryEquip(ItemStack equipment) {
		   EntityLiving dis = ((EntityLiving)(Object)this);
		   
	      int equipmentSlot = dis.getArmorPosition(equipment);
	      if(equipmentSlot > -1) {
		      ItemStack itemStack = this.getEquipmentInSlot(equipmentSlot);
		      boolean bl = this.prefersNewEquipment(equipment, itemStack);
		      if (bl && this.canPickupItem(equipment)) {
		         double d = (double)this.getDropChance(equipmentSlot);
		         if (itemStack != null && this.rand.nextFloat() - 0.1F < d) {
		            this.entityDropItem(itemStack, 0.0F);
		         }
		         
		         equipLootStack(equipmentSlot, itemStack);
		         return true;
		      } else {
		         return false;
		      }
	      } else {
	    	  return false;
	      }
	   }
	   
	   public double getDropChance(int equipmentSlot) {
		   return this.equipmentDropChances[equipmentSlot];
	   }
	   
	   public boolean prefersNewEquipment(ItemStack itemstack, ItemStack itemstack1) {
		   boolean flag = true;
		   if (itemstack1 != null)
           {
               //if (i == 0)
               //{
                   if (itemstack.getItem() instanceof ItemSword && !(itemstack1.getItem() instanceof ItemSword))
                   {
                       flag = true;
                   }
                   else if (itemstack.getItem() instanceof ItemSword && itemstack1.getItem() instanceof ItemSword)
                   {
                       ItemSword itemsword = (ItemSword)itemstack.getItem();
                       ItemSword itemsword1 = (ItemSword)itemstack1.getItem();

                       if (itemsword.func_150931_i() == itemsword1.func_150931_i())
                       {
                           flag = itemstack.getItemDamage() > itemstack1.getItemDamage() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
                       }
                       else
                       {
                           flag = itemsword.func_150931_i() > itemsword1.func_150931_i();
                       }
                   }
                   //else
                   //{
                   //    flag = false;
                   //}
               //}
               else if (itemstack.getItem() instanceof ItemArmor && !(itemstack1.getItem() instanceof ItemArmor))
               {
                   flag = true;
               }
               else if (itemstack.getItem() instanceof ItemArmor && itemstack1.getItem() instanceof ItemArmor)
               {
                   ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
                   ItemArmor itemarmor1 = (ItemArmor)itemstack1.getItem();

                   if (itemarmor.damageReduceAmount == itemarmor1.damageReduceAmount)
                   {
                       flag = itemstack.getItemDamage() > itemstack1.getItemDamage() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
                   }
                   else
                   {
                       flag = itemarmor.damageReduceAmount > itemarmor1.damageReduceAmount;
                   }
               }
               else
               {
                   flag = false;
               }
           }
		   return flag;
	   }

	protected void equipLootStack(int slot, ItemStack stack) {
		this.setCurrentItemOrArmor(slot, stack);
		this.equipmentDropChances[slot] = 2.0F;
		this.persistenceRequired = true;
	}

	public boolean canPickupItem(ItemStack stack) {
		return true;
	}
	
}
