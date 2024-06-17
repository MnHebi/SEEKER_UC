package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.SKR_txt.txt;

public class SKR_economic extends BaseHullMod {

    private final float MAINTENANCE_BONUS=0.66f;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSuppliesPerMonth().modifyMult(id, MAINTENANCE_BONUS);
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return (int)((1-MAINTENANCE_BONUS)*100)+txt("%");
        return null;
    }
}
