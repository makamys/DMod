package makamys.dmod.future.nbt;

import java.util.List;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

public class NBTTagListFuture {
	
	public static List<NBTBase> toList(NBTTagList tagList) {
		return ReflectionHelper.getPrivateValue(NBTTagList.class, tagList, "tagList", "field_74747_a");
	}
	
	public static void add(NBTTagList tagList, int index, NBTBase element) {
		if(tagList.tagCount() == 0) {
			tagList.appendTag(element);
		} else {
			toList(tagList).add(index, element);
		}
	}
	
}
