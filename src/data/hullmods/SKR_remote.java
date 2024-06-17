package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import static data.scripts.util.SKR_txt.txt;

public class SKR_remote extends BaseHullMod {

    private final int OVERHEAD = 20, LOSSES = 0;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterRefitTimeMult().modifyPercent(id, OVERHEAD);
        stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, LOSSES);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return (100-LOSSES)+txt("%");
        if (index == 1) return OVERHEAD + txt("%");
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getHullSpec().getFighterBays() > 0;	
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("noDeck");
    }
}