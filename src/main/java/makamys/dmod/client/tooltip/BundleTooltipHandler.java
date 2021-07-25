package makamys.dmod.client.tooltip;


import static codechicken.lib.gui.GuiDraw.drawRect;

import java.awt.Dimension;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.gui.GuiDraw.ITooltipLineHandler;
import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makamys.dmod.DMod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BundleTooltipHandler implements ITooltipLineHandler {

	public static final ResourceLocation TEXTURE = new ResourceLocation(DMod.MODID, "textures/gui/container/bundle.png");
	private static final int field_32381 = 4;
	private static final int field_32382 = 1;
	private static final int field_32383 = 128;
	private static final int field_32384 = 18;
	private static final int field_32385 = 20;
	
	private List<ItemStack> inventory;
	private int occupancy;
	
	public BundleTooltipHandler(List<ItemStack> stacks, int slots) {
		this.inventory = stacks;
		this.occupancy = slots;
	}
	
	@Override
	public Dimension getSize() {
		return new Dimension(this.getColumns() * 18 + 2, this.getRows() * 20 + 2 + 4);
	}

	@Override
	public void draw(int x, int y) {
		int i = this.getColumns();
		int j = this.getRows();
		boolean bl = this.occupancy >= 64;
		int k = 0;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
		
		for (int l = 0; l < j; ++l) {
			for (int m = 0; m < i; ++m) {
				int n = x + m * 18 + 1;
				int o = y + l * 20 + 1;
				this.drawSlot(n, o, k++, bl, GuiDraw.fontRenderer, GuiContainerManager.drawItems, GuiDraw.renderEngine);
			}
		}
		
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
		
		this.drawOutline(x, y, i, j, GuiDraw.renderEngine);
	}

	private void drawSlot(int x, int y, int index, boolean shouldBlock, FontRenderer textRenderer,
			RenderItem itemRenderer, TextureManager textureManager) {
		if (index >= this.inventory.size()) {
			this.draw(x, y, textureManager,
					shouldBlock ? Sprite.BLOCKED_SLOT : Sprite.SLOT);
		} else {
			ItemStack itemStack = (ItemStack) this.inventory.get(index);
			// calling drawItem sets some kind of state we need, which needs to
			// be set before this.draw()... this is my workaround
			GuiContainerManager.drawItem(x + 1, y + 1, itemStack);
			this.draw(x, y, textureManager, Sprite.SLOT);
			GuiContainerManager.drawItem(x + 1, y + 1, itemStack);
			if (index == 0) {
				GuiDraw.drawRect(x + 1, y + 1, 16, 16, 0x80FFFFFF);//highlight
			}

		}
	}

	private void drawOutline(int x, int y, int columns, int rows,
			TextureManager textureManager) {
		this.draw(x, y, textureManager, Sprite.BORDER_CORNER_TOP);
		this.draw(x + columns * 18 + 1, y, textureManager,
				Sprite.BORDER_CORNER_TOP);

		int j;
		for (j = 0; j < columns; ++j) {
			this.draw(x + 1 + j * 18, y, textureManager,
					Sprite.BORDER_HORIZONTAL_TOP);
			this.draw(x + 1 + j * 18, y + rows * 20, textureManager,
					Sprite.BORDER_HORIZONTAL_BOTTOM);
		}

		for (j = 0; j < rows; ++j) {
			this.draw(x, y + j * 20 + 1, textureManager, Sprite.BORDER_VERTICAL);
			this.draw(x + columns * 18 + 1, y + j * 20 + 1, textureManager,
					Sprite.BORDER_VERTICAL);
		}

		this.draw(x, y + rows * 20, textureManager, Sprite.BORDER_CORNER_BOTTOM);
		this.draw(x + columns * 18 + 1, y + rows * 20, textureManager,
				Sprite.BORDER_CORNER_BOTTOM);
	}

	private void draw(int x, int y, TextureManager textureManager,
			Sprite sprite) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GuiDraw.changeTexture(TEXTURE);
		GuiDraw.drawTexturedModalRect(x, y, sprite.u, sprite.v, sprite.width, sprite.height);
	}

	private int getColumns() {
		return Math.max(2, (int) Math.ceil(Math.sqrt((double) this.inventory.size() + 1.0D)));
	}

	private int getRows() {
		return (int) Math.ceil(((double) this.inventory.size() + 1.0D) / (double) this.getColumns());
	}

	@SideOnly(Side.CLIENT)
	private static enum Sprite {
		SLOT(0, 0, 18, 20), BLOCKED_SLOT(0, 40, 18, 20), BORDER_VERTICAL(0, 18, 1, 20),
		BORDER_HORIZONTAL_TOP(0, 20, 18, 1), BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1), BORDER_CORNER_TOP(0, 20, 1, 1),
		BORDER_CORNER_BOTTOM(0, 60, 1, 1);

		public final int u;
		public final int v;
		public final int width;
		public final int height;

		private Sprite(int u, int v, int width, int height) {
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}
	}

}
