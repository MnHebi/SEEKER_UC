package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
//import data.scripts.util.MagicIncompatibleHullmods;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tartiflette
 */
public class SKR_siegfriedLoadout extends BaseHullMod{
    
//    private boolean stripRequired=false;
    
    private final Map<String,String> SWITCH_A = new HashMap<>();
    {
        SWITCH_A.put("SKR_siegfried_axx", "a");
        SWITCH_A.put("SKR_siegfried_bxx", "b");
    }
    private final Map<String,String> SWITCH_B = new HashMap<>();
    {
        SWITCH_B.put("SKR_siegfried_xax", "a");
        SWITCH_B.put("SKR_siegfried_xbx", "b");
    }
    private final Map<String,String> SWITCH_C = new HashMap<>();
    {
        SWITCH_C.put("SKR_siegfried_xxa", "a");
        SWITCH_C.put("SKR_siegfried_xxb", "b");
    }
    
    private final Map<String,String> SWITCH_TO = new HashMap<>();
    {
        SWITCH_TO.put("a", "b");
        SWITCH_TO.put("b", "a");
    }
    
    private final List<String>EMPTY_layout = new ArrayList<>();
    {
        EMPTY_layout.add("FIRECANE1");
        EMPTY_layout.add("FIRECANE2");
        EMPTY_layout.add("LARGE2");
        EMPTY_layout.add("LARGE3");
        EMPTY_layout.add("LARGE4");
        EMPTY_layout.add("LARGE5");
        EMPTY_layout.add("MOUNTS1");
        EMPTY_layout.add("MOUNTS2");
    }
    private final List<String>EMPTY_deck = new ArrayList<>();
    {
        EMPTY_deck.add("DECKS");
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        //trigger a loadout switch if at least one of the selector hullmods is absent
        int toSwitch=0;
        String core = "o";
        String system = "o";
        String loadout = "o";
        for(String h : stats.getVariant().getHullMods()){
            
            for(String H : SWITCH_A.keySet()){
                if(h.equals(H)){
                    toSwitch++;
                    core=SWITCH_A.get(H);
                    break;
                }
            }
            
            for(String H : SWITCH_B.keySet()){
                if(h.equals(H)){
                    toSwitch++;
                    system=SWITCH_B.get(H);
                    break;
                }
            }
            
            for(String H : SWITCH_C.keySet()){
                if(h.equals(H)){
                    toSwitch++;
                    loadout=SWITCH_C.get(H);
                    break;
                }
            }
        }
        
        if(toSwitch==0){
            //initial setup or strip revert
            
            if(stats.getVariant().getHullSpec().getHullId().equals("SKR_siegfried")){
                //initial setup
                
                stats.getVariant().addMod("SKR_siegfried_axx");   
                stats.getVariant().addMod("SKR_siegfried_xax");
                stats.getVariant().addMod("SKR_siegfried_xxa");
                ShipHullSpecAPI ship = Global.getSettings().getHullSpec("SKR_siegfried_aaa");
                stats.getVariant().setHullSpecAPI(ship);
                
                
            } else {
                //reinstall the hullmod switches
                
                //find the current variant of the ship 
                //WARNING, ID SENSITIVE
                String currentCore = stats.getVariant().getHullSpec().getHullId().substring(14, 15);
                String currentSystem = stats.getVariant().getHullSpec().getHullId().substring(15, 16);            
                String currentLoadout = stats.getVariant().getHullSpec().getHullId().substring(16, 17);

                stats.getVariant().addMod("SKR_siegfried_"+currentCore+"xx");   
                stats.getVariant().addMod("SKR_siegfried_x"+currentSystem+"x");
                stats.getVariant().addMod("SKR_siegfried_xx"+currentLoadout);
            }
            
        } else
        if(toSwitch<3){
            
            stats.getVariant().clearHullMods();
//            stats.getVariant().clearPermaMods();

            //regular switch
            String newVariant = "ooo";            
            //find the current variant of the ship 
            //WARNING, ID SENSITIVE
            String currentCore = stats.getVariant().getHullSpec().getHullId().substring(14, 15);
            String currentSystem = stats.getVariant().getHullSpec().getHullId().substring(15, 16);            
            String currentLoadout = stats.getVariant().getHullSpec().getHullId().substring(16, 17);
            
            //deduct the new variant
            if(core.equals("o")){
                //remove ITU
                stats.getVariant().removeMod("targetingunit");
                stats.getVariant().removePermaMod("targetingunit");
                newVariant = SWITCH_TO.get(currentCore)+currentSystem+currentLoadout;
                stats.getVariant().addMod("SKR_siegfried_"+SWITCH_TO.get(currentCore)+"xx");     
                stats.getVariant().addMod("SKR_siegfried_x"+currentSystem+"x");
                stats.getVariant().addMod("SKR_siegfried_xx"+currentLoadout);     
            } else if (system.equals("o")){
                //remove deck deco from slots for config changes
                for(WeaponSlotAPI s : stats.getVariant().getHullSpec().getAllWeaponSlotsCopy()){
                    if(EMPTY_deck.contains(s.getId()))stats.getVariant().clearSlot(s.getId());
                }
                newVariant = currentCore+SWITCH_TO.get(currentSystem)+currentLoadout;
                stats.getVariant().addMod("SKR_siegfried_"+currentCore+"xx");   
                stats.getVariant().addMod("SKR_siegfried_x"+SWITCH_TO.get(currentSystem)+"x");
                stats.getVariant().addMod("SKR_siegfried_xx"+currentLoadout);
            } else if (loadout.equals("o")){
                //remove weapons from slots for loadout changes
                for(WeaponSlotAPI s : stats.getVariant().getHullSpec().getAllWeaponSlotsCopy()){
                    if(EMPTY_layout.contains(s.getId()))stats.getVariant().clearSlot(s.getId());
                }
                newVariant = currentCore+currentSystem+SWITCH_TO.get(currentLoadout);
                stats.getVariant().addMod("SKR_siegfried_"+currentCore+"xx");   
                stats.getVariant().addMod("SKR_siegfried_x"+currentSystem+"x");
                stats.getVariant().addMod("SKR_siegfried_xx"+SWITCH_TO.get(currentLoadout));
            }
            
            //swap the base hull
            ShipHullSpecAPI ship = Global.getSettings().getHullSpec("SKR_siegfried_"+newVariant);
            stats.getVariant().setHullSpecAPI(ship);
//            stats.getVariant().refreshBuiltInWings();
//            stats.getFleetMember().updateStats();
            stats.getVariant().clearHullMods();
//            stripRequired=true;
        }
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        if(ship.getOriginalOwner()<0){
            //undo fix for weapons put in cargo
            if(
                    Global.getSector()!=null && 
                    Global.getSector().getPlayerFleet()!=null && 
                    Global.getSector().getPlayerFleet().getCargo()!=null && 
                    Global.getSector().getPlayerFleet().getCargo().getStacksCopy()!=null &&
                    !Global.getSector().getPlayerFleet().getCargo().getStacksCopy().isEmpty()
                    ){
                for (CargoStackAPI s : Global.getSector().getPlayerFleet().getCargo().getStacksCopy()){
                    if(
                            s.isWeaponStack() 
                            && s.getWeaponSpecIfWeapon().getWeaponId().startsWith("SKR_firecane")
                            ){
                        Global.getSector().getPlayerFleet().getCargo().removeStack(s);
                    }
                }
            }
        }
    }
    
    private final Color BAD=Misc.getNegativeHighlightColor();
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
	
        //title
        tooltip.addSectionHeading(txt("TTIP_sieg0"), Alignment.MID, 15); //"WARNING"
        
        //total effect
        tooltip.addPara(
                txt("TTIP_sieg1")
                + txt("TTIP_sieg2")
                + txt("TTIP_sieg3"),
                10,
                BAD,
                txt("TTIP_sieg2"));
    }
}
