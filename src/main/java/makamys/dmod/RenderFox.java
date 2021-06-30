package makamys.dmod;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
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
            return foxEntity.isSleeping() ? sleepingTexture : texture;
         } else {
            return foxEntity.isSleeping() ? sleepingSnowTexture : snowTexture;
         }
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity p_110775_1_)
    {
        return this.getEntityTexture((FoxEntity)p_110775_1_);
    }
}