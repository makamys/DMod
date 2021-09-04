package makamys.dmod.client.tooltip;

import java.util.List;
import java.util.stream.Collectors;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.IContainerTooltipHandler;
import makamys.dmod.future.item.ItemFuture;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

public class DTooltipHandler implements IContainerTooltipHandler {
    
    @Override
    public List<String> handleTooltip(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public List<String> handleItemDisplayName(GuiContainer gui, ItemStack itemstack, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack itemstack, int mousex, int mousey,
            List<String> currenttip) {
        if(itemstack != null && itemstack.getItem() instanceof ItemFuture) {
            List<String> strings = ((ItemFuture)itemstack.getItem()).getTooltipHandlers(itemstack).stream().map(handler -> GuiDraw.TOOLTIP_HANDLER + GuiDraw.getTipLineId(handler)).collect(Collectors.toList());
            currenttip.addAll(1, strings);
        }
        return currenttip;
    }

}
