package makamys.dmod;

import cpw.mods.fml.common.Loader;
import makamys.mclib.sloppydeploader.SloppyDepLoader;
import makamys.mclib.sloppydeploader.SloppyDependency;
import makamys.updatechecklib.UpdateCheckAPI;

public class UpdateCheckHelper {
    
    public static final String UCL_VERSION = "1.0";
    
    public static void init(String modid) {
        SloppyDepLoader.addDependency(new SloppyDependency("https://github.com/makamys/UpdateCheckLib/releases/download/v" + UCL_VERSION, "UpdateCheckLib-" + Loader.MC_VERSION + "-" + UCL_VERSION + ".jar", "makamys.updatechecklib.UpdateCheckLib"));
        if(Loader.isModLoaded("UpdateCheckLib")) {
            initUpdateCheckLib(modid);
        }
    }
    
    @cpw.mods.fml.common.Optional.Method(modid = "UpdateCheckLib")
    private static void initUpdateCheckLib(String modid) {
        UpdateCheckAPI.submitModTask(modid, "https://raw.githubusercontent.com/makamys/Satchels/master/updatejson/update.json");
    }
    
}
