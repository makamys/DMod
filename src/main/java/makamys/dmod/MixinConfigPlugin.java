package makamys.dmod;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import makamys.dmod.ConfigDMod.ForceableBoolean;
import net.minecraft.client.Minecraft;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    
    @Override
    public void onLoad(String mixinPackage) {
        ConfigDMod.reload();
    }

    @Override
    public String getRefMapperConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(Arrays.asList(
                "makamys.dmod.mixin.MixinEntityWolf"
                ).contains(mixinClassName)){
            return ConfigDMod.wolvesTargetFoxes;    
        } else if(Arrays.asList(
                "makamys.dmod.mixin.MixinEntityLivingBase"
                ).contains(mixinClassName)){
            return ConfigDMod.enableFox && ConfigDMod.lootingFoxFix != ForceableBoolean.FALSE;
        } else if(Arrays.asList(
                "makamys.dmod.mixin.MixinRenderItem"
                ).contains(mixinClassName)){
            return MixinEnvironment.getCurrentEnvironment().getSide() == Side.CLIENT && ConfigDMod.durabilityBarColor;
        } else if(Arrays.asList(
                "makamys.dmod.mixin.MixinContainer"
                ).contains(mixinClassName)){
            return ConfigDMod.enableBundle;
        } else {
            return true;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getMixins() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // TODO Auto-generated method stub
        
    }

}
