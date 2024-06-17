package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SKR_overdrive extends BaseHullMod {

    private final float TURRET_SPEED_BONUS=400;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
    }
    
//    @Override
//    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//    }
    
//    @Override
//    public void advanceInCombat(ShipAPI ship, float amount) {
//    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}
