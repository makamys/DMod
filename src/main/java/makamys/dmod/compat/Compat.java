package makamys.dmod.compat;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.blocks.BlockBerryBush;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Compat {
    
    private static List<IBerryBushHandler> berryBushHandlers = new ArrayList<>();
    
    public static void init() {
        if(Loader.isModLoaded("etfuturum")) {
            registerBerryBushHandler(new EtFuturumBerryBushHandler());
        }
    }
    
    public static void registerBerryBushHandler(IBerryBushHandler bbh) {
        berryBushHandlers.add(bbh);
    }
    
    public static BerryBushState getBerryBushState(World world, int x, int y, int z) {
        for(IBerryBushHandler bbh : berryBushHandlers) {
            if(bbh.isBerryBush(world, x, y, z)) {
                return new BerryBushState(world, x, y, z, bbh);
            }
        }
        return null;
    }
    
    public static boolean isBerryBushDamageSource(DamageSource source) {
        return berryBushHandlers.stream().anyMatch(bbh -> bbh.isBerryBushDamageSource(source));
    }
    
    public static class BerryBushState {
        public World world;
        public int x;
        public int y;
        public int z;
        public IBerryBushHandler handler;
        
        public BerryBushState(World world, int x, int y, int z, IBerryBushHandler bbh) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.handler = bbh;
        }
        
        public int getAge() {
            return handler.getBerryBushAge(world, x, y, z);
        }
        
        public int getMetaForNewAge(int newAge) {
            return handler.getMetaForNewAge(world, x, y, z, newAge);
        }
    }
    
    static class EtFuturumBerryBushHandler implements IBerryBushHandler {

        private final Block sweetBerryBush;
        private final Item sweetBerry;
        
        public EtFuturumBerryBushHandler() {
            sweetBerryBush = GameRegistry.findBlock("etfuturum", "sweet_berry_bush");
            sweetBerry = GameRegistry.findItem("etfuturum", "sweet_berries");
        }
        
        @Override
        public boolean isBerryBush(World world, int x, int y, int z) {
            return world.getBlock(x, y, z) == sweetBerryBush;
        }

        @Override
        public int getBerryBushAge(World world, int x, int y, int z) {
            return world.getBlockMetadata(x, y, z);
        }
        
        @Override
        public int getMetaForNewAge(World world, int x, int y, int z, int newAge) {
            return newAge;
        }

        @Override
        public Item getSweetBerryItem() {
            return sweetBerry;
        }

        @Override
        public boolean isBerryBushDamageSource(DamageSource source) {
            return source == BlockBerryBush.SWEET_BERRY_BUSH;
        }
    }
    
}
