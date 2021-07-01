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
            GL11.glTranslatef(0f, 0.3f, -0.35f);
            
            
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
                f1 *= 0.45F;
                
                /*GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(75.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(13.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-40.0F, 1.0F, 0.0F, 0.0F);*/
                
                //GL11.glRotatef(45.0F, 0.0F, 0.0F, 1.0F);
                //GL11.glRotatef((p_77029_1_.ticksExisted+p_77029_2_)*32f, 1.0F, 0.0F, 0.0F);
                /*GL11.glRotatef(92F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-15F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-15F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(50F, 1.0F, 0.0F, 0.0F);*/
                
                GL11.glScalef(f1, -f1, f1);
                GL11.glTranslatef(0.3f, 1f, 1.5f);
                //GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            }
            else {
            	if (itemstack.getItem() == Items.bow)
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
	                f1 = 0.625F * 0.7f;
	
	                if (itemstack.getItem().shouldRotateAroundWhenRendering())
	                {
	                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
	                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
	                }
	                
	                GL11.glTranslatef(0.02f, 0.0f, 0f);
	                this.func_82410_b();
	                GL11.glScalef(f1, -f1, f1);
	                //GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
	                //GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	            }
	            else
	            {
	                f1 = 0.335F;
	                //GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
	                GL11.glScalef(f1, f1, f1);
	                GL11.glTranslatef(0.72f,-0.15f,-0.45f);
	                GL11.glRotatef(-45.0F + 90f, 0.0F, 1.0F, 0.0F);
	                
	                /*GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
	                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
	                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);*/
	            }
            	GL11.glRotatef(-45F, 0.0F, 1.0F, 0.0F);
                
                GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
                
                // undo renderItem's rotation
                
                GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
            }
            
            
            if (itemstack.getItem().requiresMultipleRenderPasses())
            {
                for (int k = 0; k < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); ++k)
                {
                    int i = itemstack.getItem().getColorFromItemStack(itemstack, k);
                    float f12 = (float)(i >> 16 & 255) / 255.0F;
                    float f3 = (float)(i >> 8 & 255) / 255.0F;
                    float f4 = (float)(i & 255) / 255.0F;
                    GL11.glColor4f(f12, f3, f4, 1.0F);
                    this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack, k);
                }
            }
            else
            {
                int k = itemstack.getItem().getColorFromItemStack(itemstack, 0);
                float f11 = (float)(k >> 16 & 255) / 255.0F;
                float f12 = (float)(k >> 8 & 255) / 255.0F;
                float f3 = (float)(k & 255) / 255.0F;
                GL11.glColor4f(f11, f12, f3, 1.0F);
                this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack, 0);
            }
            
            //this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack, 0);
            //EntityItem entityitem = new EntityItem(p_77029_1_.worldObj, 0.0D, 0.0D, 0.0D, itemstack);
            
            /*RenderItem.renderInFrame = true;
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            RenderItem.renderInFrame = false;*/
/*
            if (itemstack.getItem().requiresMultipleRenderPasses())
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