package makamys.dmod.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;

public class EggHelper {
    
    private static Map<Class<? extends Entity>, Integer> addedEggIDs = new HashMap<>();
    
    public static void addEgg(Class<? extends Entity> clazz, int color1, int color2) {
        int eggID = getNextFreeEggSlot();
        EntityList.IDtoClassMapping.put(eggID, clazz);
        EntityList.entityEggs.put(eggID, new EntityEggInfo(eggID, color1, color2));
        addedEggIDs.put(clazz, eggID);
    }
    
    public static int getIDForClass(Class<? extends Entity> clazz) {
        return addedEggIDs.getOrDefault(clazz, -1);
    }
    
    public static int getNextFreeEggSlot() {
        int eggID = 0;
        while(EntityList.getStringFromID(eggID) != null) {
            eggID++;
        }
        return eggID;
    }
    
}
