package makamys.dmod.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makamys.dmod.DMod;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBundle extends Item implements IConfigurable {
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
	
}