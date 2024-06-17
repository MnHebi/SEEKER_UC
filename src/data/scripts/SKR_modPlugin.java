package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import static data.campaign.ids.SKR_ids.THEME_PLAGUEBEARER;
import data.scripts.ai.SKR_akitaAI;
import data.scripts.ai.SKR_antiMissileAI;
import data.scripts.ai.SKR_canAI;
import data.scripts.ai.SKR_flareAI;
import data.scripts.ai.SKR_modsAI;
import data.scripts.ai.SKR_obsidianMissileAI;
import data.scripts.ai.SKR_oversteerMissileAI;
import data.scripts.ai.SKR_stepMissileAI;
import data.scripts.ai.SKR_sunburstMissileAI;
import org.magiclib.util.MagicSettings;
import data.scripts.util.SKR_plagueEffect;
import static data.scripts.util.SKR_txt.txt;
import data.scripts.world.SKR_seekerGen;
import java.util.HashMap;
import java.util.Map;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class SKR_modPlugin extends BaseModPlugin {

    public static final String antiMissile_ID = "SKR_antiMissile";
    public static final String stepMissile_ID = "SKR_stepMissile";
    public static final String accelMissile_ID = "SKR_accelMissile";
    public static final String flare_ID = "SKR_flareMirv_shot";
    public static final String sunburst_ID = "SKR_sunburstMissile";
    public static final String obsidian_ID = "SKR_obsidianMissile";
    public static final String mods_ID = "SKR_modsMissile";
    public static final String stare_ID = "SKR_akitaMissile";
    public static final String can_ID = "SKR_can";

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case antiMissile_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_antiMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case stepMissile_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_stepMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case accelMissile_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_oversteerMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case flare_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_flareAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case sunburst_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_sunburstMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case obsidian_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_obsidianMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case mods_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_modsAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case stare_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_akitaAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case can_ID:
                return new PluginPick<MissileAIPlugin>(new SKR_canAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }
    
    public static Map<String,String> bossArrivalSounds = new HashMap<>();   
    
    @Override
    public void onApplicationLoad(){        
        if(Global.getSettings().getModManager().isModEnabled("shaderLib")){
            ShaderLib.init();  
            LightData.readLightDataCSV(MagicSettings.getString("seeker", "graphicLib_lights")); 
            TextureData.readTextureDataCSV(MagicSettings.getString("seeker", "graphicLib_maps")); 
        }
        
        SKR_plagueEffect.loadPlagueData();
        bossArrivalSounds=MagicSettings.getStringMap("seeker", "plaguebearer_warp");
    }
    
    @Override
    public void onNewGame() {
        //save mod version for save patching
        Global.getSector().getMemoryWithoutUpdate().set("$seeker_version", 0.523f);
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
        //SAVE PATCHING CODE
        
        //0.52 RC3 
        //fixing missing global memkeys for boss bounties
        
        if(Global.getSector().getMemoryWithoutUpdate().getFloat("$seeker_version")<0.523f){
            Global.getSector().getMemoryWithoutUpdate().set("$seeker_version", 0.523f);
            
            for(StarSystemAPI system : Global.getSector().getStarSystems()){
                if(system.hasTag(THEME_PLAGUEBEARER)){
                    for(CampaignFleetAPI fleet:system.getFleets()){
                        if( fleet.getFlagship().getShipName().equals(txt("plague_A_boss"))){
                            Global.getSector().getMemoryWithoutUpdate().set("$SKR_safeguard_boss", true);
                        } else if( fleet.getFlagship().getShipName().equals(txt("plague_B_boss"))){
                            Global.getSector().getMemoryWithoutUpdate().set("$SKR_rampage_boss", true);
                        } else if( fleet.getFlagship().getShipName().equals(txt("plague_C_boss"))){
                            Global.getSector().getMemoryWithoutUpdate().set("$SKR_whitedwarf_boss", true);
                        } else if( fleet.getFlagship().getShipName().equals(txt("plague_D_boss"))){
                            Global.getSector().getMemoryWithoutUpdate().set("$SKR_cataclysm_boss", true);
                        }else if( fleet.getFlagship().getShipName().equals(txt("nova_boss"))){
                            Global.getSector().getMemoryWithoutUpdate().set("$SKR_nova", false);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void onNewGameAfterEconomyLoad() {
        //enforce relationships after every other mod did their stuff
        SKR_seekerGen.initFactionRelationships(Global.getSector());
             
        SKR_seekerGen.addExplorationContent();
    }
}
