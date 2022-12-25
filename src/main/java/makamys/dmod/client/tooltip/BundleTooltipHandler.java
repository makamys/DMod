package makamys.dmod.client.tooltip;

import static makamys.dmod.DModConstants.MODID;

import java.awt.Dimension;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.gui.GuiDraw.ITooltipLineHandler;
import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import makamys.dmod.ConfigDMod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BundleTooltipHandler implements ITooltipLineHandler {

    public static final ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/container/bundle.png");
    private static int field_32381 = 4;
    private static int field_32382 = 1;
    private static int field_32383 = 128;
    private static int SLOT_WIDTH = 18;
    private static int SLOT_HEIGHT = 20;
    
    private List<ItemStack> inventory;
    private int occupancy;
    
    private static Sprites sprites = new Sprites();
    
    public BundleTooltipHandler(List<ItemStack> stacks, int slots) {
        this.inventory = stacks;
        this.occupancy = slots;
        
        int newSlotHeight = ConfigDMod.compactBundleGUI ? 18 : 20;
        if(newSlotHeight != SLOT_HEIGHT) {
            SLOT_HEIGHT = newSlotHeight;
            sprites = new Sprites();
        }
    }
    
    @Override
    public Dimension getSize() {
        return new Dimension(this.getColumns() * 18 + 2, this.getRows() * SLOT_HEIGHT + 2 + 4
                + (ConfigDMod.compactBundleGUI ? 1 : 0));
    }

    @Override
    public void draw(int x, int y) {
        int i = this.getColumns();
        int j = this.getRows();
        boolean bl = this.occupancy >= 64;
        int k = 0;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        GuiContainerManager.enable2DRender();
        
        for (int l = 0; l < j; ++l) {
            for (int m = 0; m < i; ++m) {
                int n = x + m * 18 + 1;
                int o = y + l * SLOT_HEIGHT + 1;
                this.drawSlot(n, o, k++, bl, GuiDraw.fontRenderer, GuiContainerManager.drawItems, GuiDraw.renderEngine);
            }
        }
        
        this.drawOutline(x, y, i, j, GuiDraw.renderEngine);
        
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }

    private void drawSlot(int x, int y, int index, boolean shouldBlock, FontRenderer textRenderer,
            RenderItem itemRenderer, TextureManager textureManager) {
        if (index >= this.inventory.size()) {
            this.draw(x, y, textureManager,
                    shouldBlock ? sprites.BLOCKED_SLOT : sprites.SLOT);
        } else {
            ItemStack itemStack = (ItemStack) this.inventory.get(index);
            this.draw(x, y, textureManager, sprites.SLOT);
            GuiContainerManager.drawItems.zLevel += 250f;
            GuiContainerManager.drawItem(x + 1, y + 1, itemStack);
            GuiContainerManager.drawItems.zLevel -= 250f;
            if (index == 0) {
                GuiDraw.drawRect(x + 1, y + 1, 16, 16, 0x80FFFFFF);//highlight
            }
        }
    }

    private void drawOutline(int x, int y, int columns, int rows,
            TextureManager textureManager) {
        this.draw(x, y, textureManager, sprites.BORDER_CORNER_TOP);
        this.draw(x + columns * 18 + 1, y, textureManager,
                sprites.BORDER_CORNER_TOP);

        int bottomOff = ConfigDMod.compactBundleGUI ? 1 : 0;
        
        int j;
        for (j = 0; j < columns; ++j) {
            this.draw(x + 1 + j * 18, y, textureManager,
                    sprites.BORDER_HORIZONTAL_TOP);
            this.draw(x + 1 + j * 18, y + rows * SLOT_HEIGHT + bottomOff, textureManager,
                    sprites.BORDER_HORIZONTAL_BOTTOM);
        }

        for (j = 0; j < rows; ++j) {
            this.draw(x, y + j * SLOT_HEIGHT + 1, textureManager, sprites.BORDER_VERTICAL);
            this.draw(x + columns * 18 + 1, y + j * SLOT_HEIGHT + 1, textureManager,
                    sprites.BORDER_VERTICAL);
        }

        this.draw(x, y + rows * SLOT_HEIGHT + bottomOff, textureManager, sprites.BORDER_CORNER_BOTTOM);
        this.draw(x + columns * 18 + 1, y + rows * SLOT_HEIGHT + bottomOff, textureManager,
                sprites.BORDER_CORNER_BOTTOM);
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
    
    private static class Sprites {
        public Sprite
            SLOT = new Sprite(0, 0, 18, SLOT_HEIGHT),
            BLOCKED_SLOT = new Sprite(0, 40, 18, SLOT_HEIGHT),
            BORDER_VERTICAL = new Sprite(0, 18, 1, SLOT_HEIGHT),
            BORDER_HORIZONTAL_TOP = new Sprite(0, SLOT_HEIGHT, 18, 1),
            BORDER_HORIZONTAL_BOTTOM = new Sprite(0, 60, 18, 1),
            BORDER_CORNER_TOP = new Sprite(0, SLOT_HEIGHT, 1, 1),
            BORDER_CORNER_BOTTOM = new Sprite(0, 60, 1, 1);
    }
    
    @SideOnly(Side.CLIENT)
    private static class Sprite {
        public int u;
        public int v;
        public int width;
        public int height;

        private Sprite(int u, int v, int width, int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }

}
