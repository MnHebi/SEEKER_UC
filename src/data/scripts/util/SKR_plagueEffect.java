/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import org.magiclib.util.MagicSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class SKR_plagueEffect {    
    
    //////////////////////////////
    //                          //
    //      PLAGUE EFFECT       //
    //                          //
    //////////////////////////////  

    /**
     * Called by the weapons scripts to enable the plague contamination
     * Reduces a ship's CR per plague weapon installed
     * 
     * @param shipVariant
     * Variant of the ship affected by the plague debuff
     */
    
    private static final String PLAGUE_HULLMOD = "SKR_plagueWarning";
    private static final String IMMUNE_HULLMOD_A = "SKR_plagueBearer";
    private static final String IMMUNE_HULLMOD_B = "SKR_plagueCultist";
    
    public static void ApplyPlague (ShipVariantAPI shipVariant){
        //get interference data
        if(RATES.isEmpty()){
            loadPlagueData();
        }
        if(shipVariant.getHullMods().contains(IMMUNE_HULLMOD_A) || shipVariant.getHullMods().contains(IMMUNE_HULLMOD_B))return;
        if(!shipVariant.getHullMods().contains(PLAGUE_HULLMOD)){
            shipVariant.getHullMods().add(PLAGUE_HULLMOD);
        }
    }
    
    //////////////////////////////
    //                          //
    //       PLAGUE DATA        //
    //                          //
    //////////////////////////////            
        
    private static final Logger LOG=  Global.getLogger(SKR_plagueEffect.class);
    
    public static float HSS =0;
    public static Map<String,Float> RATES = new HashMap<>();
    public static Map<String,Float> SOURCES = new HashMap<>();
    public static List<String> WEAPONS = new ArrayList<>();
    public static List<String> LPC = new ArrayList<>();
    
    public static void loadPlagueData(){
        HSS = MagicSettings.getFloat("seeker", "plague_HSSmult");
        RATES = MagicSettings.getFloatMap("seeker", "plague_rates");
        LPC = MagicSettings.getList("seeker", "plague_LPC");
        
        Map<String,String>rawWeapons = MagicSettings.getStringMap("seeker", "plague_weapons");
        for(Map.Entry<String,String> w : rawWeapons.entrySet()){
            SOURCES.put(w.getKey(), RATES.get(w.getValue()));
            WEAPONS.add(w.getKey());
        }
    }
    
    //////////////////////////////
    //                          //
    //   CONTAMINATION EFFECT   //
    //                          //
    //////////////////////////////  

    /**
     * Called by the contamination hullmod to compute the effect
     * 
     * @param shipVariant
     * Variant of the ship affected by the plague
     * 
     * @return
     * Uncapped CR reduction caused by the infected weapons
     */   
    public static Float getTotalInfectionCost (ShipVariantAPI shipVariant){
        
        float total=0;        
        Map<String,Float> theDebuffs = getDebuffs(shipVariant);
        
        float hullmod=1;
        //scan for interference-reducing hullmod
        if(shipVariant.getHullMods().contains("hardened_subsystems")){
            hullmod=HSS;
        }
        
        //compute total of the interferences
        if(!theDebuffs.isEmpty()){
            for(String mountID : theDebuffs.keySet()){
                total+=hullmod*theDebuffs.get(mountID);
            }
        }        
        return total;
    }
    
    /**
     *
     * @param shipVariant
     * Variant of the ship affected by the nano-plague
     * 
     * @return
     * Map of the mounts fitted with a weapon with interference, with their current individual effects
     */
    public static Map<String, Float> getDebuffs (ShipVariantAPI shipVariant){
        Map<String,Float> theDebuffs = new HashMap<>();
        
        if(Global.getSettings().isDevMode()){

            //LOG.info("computing plague debuff");
            //scan all weapons for plague sources
            //to ensure weapons of the same type do not override eachother when stored, the key is the weapon mount ID

            if(shipVariant.getFittedWeaponSlots()!=null && !shipVariant.getFittedWeaponSlots().isEmpty()){
                for(String mountID : shipVariant.getNonBuiltInWeaponSlots()){
                    if(shipVariant.getFittedWeaponSlots().contains(mountID) //is the slot fitted?
                            && SOURCES.containsKey(shipVariant.getWeaponId(mountID)) //is it a contaminating weapon?
                            ){   
                        theDebuffs.put(mountID, SOURCES.get(shipVariant.getWeaponId(mountID)));                
                        //LOG.info("added plague source: "+shipVariant.getWeaponId(mountID));
                    }
                }
            }
        } else {
            if(shipVariant.getFittedWeaponSlots()!=null && !shipVariant.getFittedWeaponSlots().isEmpty()){
                for(String mountID : shipVariant.getNonBuiltInWeaponSlots()){
                    if(shipVariant.getFittedWeaponSlots().contains(mountID)
                            && SOURCES.containsKey(shipVariant.getWeaponId(mountID))
                            ){   
                        theDebuffs.put(mountID, SOURCES.get(shipVariant.getWeaponId(mountID)));
                    }
                }
            }
        }
        return theDebuffs;
    }
}