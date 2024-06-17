package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SKR_destroyedDeck extends BaseHullMod {
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        String hullId = ((ShipAPI)stats.getEntity()).getId();
//        if(hullId.startsWith("SKR_clipper_d")){
            stats.getNumFighterBays().modifyFlat(id, -1);
//        } else {
//            stats.getNumFighterBays().unmodify(id);
//            ((ShipAPI)stats.getEntity()).getVariant().getHullMods().remove("SKR_noDecks");
//        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}
