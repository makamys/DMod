package makamys.dmod;

import org.lwjgl.opengl.GL11;

import codechicken.lib.math.MathHelper;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderFox extends RenderLiving
{
    private static final ResourceLocation texture = new ResourceLocation("textures/entity/fox/fox.png");
    private static final ResourceLocation sleepingTexture = new ResourceLocation("textures/entity/fox/fox_sleep.png");
    private static final ResourceLocation snowTexture = new ResourceLocation("textures/entity/fox/snow_fox.png");
    private static final ResourceLocation sleepingSnowTexture = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

    public RenderFox(ModelBase p_i1252_1_, float p_i1252_2_)
    {
        super(p_i1252_1_, p_i1252_2_);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(FoxEntity foxEntity)
    {
        if (foxEntity.getFoxType() == FoxEntity.Type.RED) {
            return foxEntity.isPlayerSleeping() ? sleepingTexture : texture;
         } else {
            return foxEntity.isPlayerSleeping() ? sleepingSnowTexture : snowTexture;
         }
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity p_110775_1_)
    {
        return this.getEntityTexture((FoxEntity)p_110775_1_);
    }
    
    protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_)
    {
        this.renderEquippedItems((FoxEntity)p_77029_1_, p_77029_2_);
    }
    
    protected void renderEquippedItems(FoxEntity p_77029_1_, float p_77029_2_)
    {
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        super.renderEquippedItems(p_77029_1_, p_77029_2_);
        ItemStack itemstack = p_77029_1_.getHeldItem();

        if (itemstack != null)
        {
            GL11.glPushMatrix();
            float f1;
            
            ModelFox mf = (ModelFox)mainModel;
            GL11.glTranslatef(mf.head.rotationPointX/16f, mf.head.rotationPointY/16f, mf.head.rotationPointZ/16f);
            
            GL11.glRotatef((float)MathHelper.todeg * mf.head.rotateAngleY, 0f, 1f, 0f);
            GL11.glRotatef((float)MathHelper.todeg * mf.head.rotateAngleX, 1f, 0f, 0f);
            GL11.glTranslatef(0.03f, 0.3f, -0.35f);
            GL11.glScalef(0.7f, 0.7f, 0.7f);
            
            
            //GL11.glRotatef(p_77029_1_.ticksExisted / 0.1F, 1.0F, 0F, 0F);
            if (this.mainModel.isChild)
            {
                f1 = 0.5F;
                GL11.glTranslatef(0.0F, 0.625F, 0.0F);
                GL11.glRotatef(-20.0F, -1.0F, 0.0F, 0.0F);
                GL11.glScalef(f1, f1, f1);
            }

            //this.witchModel.villagerNose.postRender(0.0625F);
            //GL11.glTranslatef(-0.0625F, 0.53125F, 0.21875F);

            if (itemstack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType()))
            {
                f1 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                f1 *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f1, -f1, f1);
            }
            else if (itemstack.getItem() == Items.bow)
            {
                f1 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f1, -f1, f1);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (itemstack.getItem().isFull3D())
            {
                f1 = 0.625F;

                if (itemstack.getItem().shouldRotateAroundWhenRendering())
                {
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                }

                this.func_82410_b();
                GL11.glScalef(f1, -f1, f1);
                //GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                //GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                f1 = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(f1, f1, f1);
                /*GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);*/
            }

            //GL11.glRotatef(-15.0F, 1.0F, 0.0F, 0.0F);
            //GL11.glRotatef(40.0F, 0.0F, 0.0F, 1.0F);
            //GL11.glRotatef(90F, 1.0F, 0F, 0F);
            
            
            
            //GL11.glTranslatef(-0.1F, -1.5F, -0.6F);
            GL11.glRotatef(-45F, 0.0F, 1.0F, 0.0F);
            
            GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
            
            // undo renderItem's rotation
            GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
            
            
            this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack, 0);
            //EntityItem entityitem = new EntityItem(p_77029_1_.worldObj, 0.0D, 0.0D, 0.0D, itemstack);
            
            /*RenderItem.renderInFrame = true;
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            RenderItem.renderInFrame = false;*/

            /*if (itemstack.getItem().requiresMultipleRenderPasses())
            {
                this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack, 1);
            }*/

            GL11.glPopMatrix();
        }
    }
    
    protected void func_82410_b()
    {
        //GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
    }
}