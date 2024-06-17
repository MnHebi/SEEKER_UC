package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.util.SKR_plagueEffect;
import static data.scripts.util.SKR_txt.txt;
import java.util.ArrayList;

public class SKR_plagueCultist extends BaseHullMod {
     
    private final String EXCLUSIVE=txt("plagueLPCExclusive");
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return EXCLUSIVE;
        return null;
    }
     
//    private final float REFIT_BONUS=0.05f;
//    private final float REPLACEMENT_BONUS=0.5f;
    private final Integer LCP_REAL_COST=1000;
    private final String BAD_HULLMOD_NOTIFICATION_SOUND = "cr_allied_critical";
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        //faster repairs
//        stats.getFighterRefitTimeMult().modifyMult(id, REFIT_BONUS, "Plague Contamination protocols");
//        
//        //lower decay rate
//        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, REPLACEMENT_BONUS);

        //LCP cost reduction
        stats.getDynamic().getMod(Stats.ALL_FIGHTER_COST_MOD).modifyFlat(id, -LCP_REAL_COST);
        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, -LCP_REAL_COST);
        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyFlat(id, -LCP_REAL_COST);
        stats.getDynamic().getMod(Stats.INTERCEPTOR_COST_MOD).modifyFlat(id, -LCP_REAL_COST);
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyFlat(id, -LCP_REAL_COST);
    }
    
    @Override
    public boolean affectsOPCosts() {
        return true;
    }
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //For error sound
        boolean shouldSoundError = false;

        //Attempt to go through all fighter wings on the ship
        int i = 0;
        while (i < ship.getMutableStats().getNumFighterBays().getModifiedValue() && i < 20) {
            
            //Ignore empty slots
            if (ship.getVariant().getWing(i) == null) {
                i++;
                continue;
            }

            //Remove unallowed wings
            if (!SKR_plagueEffect.LPC.contains(ship.getVariant().getWing(i).getId())) {
                if (Global.getSector() != null) {
                    if (Global.getSector().getPlayerFleet() != null) {
                        Global.getSector().getPlayerFleet().getCargo().addFighters(ship.getVariant().getWingId(i), 1);
                    }
                }
                ship.getVariant().setWingId(i ,null);
                shouldSoundError = true;
            }

            //Finally, increase our iterator
            i++;
        }
        
        //check for 
        //Creates a list for later use
        ArrayList<String> deletionList = new ArrayList<>();

        //Checks if any given hullmods is forbidden. If it is, remove it
        for (String s : ship.getVariant().getNonBuiltInHullmods()) {
            if (s.contains("SKR_plagueLPC")) {
                deletionList.add(s);
            }
        }

        //Finally, deletes the hullmods we aren't allowed to have
        if (deletionList.size() > 0) {
            ship.getVariant().addMod("ML_incompatibleHullmodWarning");
            shouldSoundError = true;
        }
        for (String s : deletionList) {
            ship.getVariant().removeMod(s);
        }
        
        //...and plays an error sound if anything was removed
        if (shouldSoundError) {
            Global.getSoundPlayer().playUISound(BAD_HULLMOD_NOTIFICATION_SOUND, 0.7f, 1f);
        }
    }
}
