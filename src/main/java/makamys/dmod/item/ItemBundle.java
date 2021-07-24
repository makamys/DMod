package makamys.dmod.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makamys.dmod.DMod;
import makamys.dmod.future.item.IItemFuture;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBundle extends Item implements IConfigurable, IItemFuture {
	public ItemBundle() {
		setMaxStackSize(1);
		setUnlocalizedName(DMod.MODID + "." + "bundle");
		setCreativeTab(CreativeTabs.tabTools);
		setTextureName("bundle");
	}
	
   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister iconRegister) {
	   super.itemIcon = iconRegister.registerIcon("dmod:bundle");
   }

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, int button, EntityPlayer player) {
		return IItemFuture.super.onClicked(stack, otherStack, slot, button, player);
	}
	
	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, int button, EntityPlayer player) {
		if(slot.getHasStack()) {
			// test
			slot.decrStackSize(1);
			return true;
		} else {
			return false;
		}
	}
	
}