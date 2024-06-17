package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.SKR_txt.txt;

public class SKR_redlineStats extends BaseShipSystemScript {

//    public static final float BALLISTIC_BUFF = 0.75f;
    public final float BALLISTIC_ROF = 1.5f;
//    public static final float FLUX_REDUCTION = 0.75f;
    public final float ENGINE_BUFF = 2f;
//    public static final float SPEED_DEBUFF = 0.33f;
    public final int EXTRA_AMMO = 2;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        if(!stats.getBallisticRoFMult().getPercentMods().containsKey(id)){
            ShipAPI ship = (ShipAPI)stats.getEntity();
            for(WeaponAPI w : ship.getAllWeapons()){
                if(w.getSlot().getSlotSize()== WeaponSize.LARGE){
                    w.setAmmo(w.getSpec().getMaxAmmo()*EXTRA_AMMO);
                    break;
                }
            }
        }
        
//        stats.getFluxDissipation().modifyMult(id, 1 - FLUX_REDUCTION * effectLevel);
        
        stats.getBallisticRoFMult().modifyPercent(id, 100*BALLISTIC_ROF*effectLevel);
            
//        stats.getMaxSpeed().modifyMult(id, 1-(SPEED_DEBUFF*effectLevel));
        stats.getAcceleration().modifyPercent(id, 100*ENGINE_BUFF*effectLevel);
        stats.getDeceleration().modifyPercent(id, 100*ENGINE_BUFF*effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 100*ENGINE_BUFF*effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, 100*ENGINE_BUFF*effectLevel);
            
//            stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1 - BALLISTIC_BUFF*effectLevel);
//            stats.getBallisticRoFMult().modifyMult(id, 1+BALLISTIC_ROF);
//        } else {
//            stats.getBallisticWeaponFluxCostMod().unmodify(id);
//            stats.getBallisticRoFMult().modifyMult(id, 1-BALLISTIC_ROF);
//        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
//        stats.getBallisticWeaponFluxCostMod().unmodify(id);
//        stats.getFluxDissipation().unmodify(id);

//        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        
            stats.getBallisticRoFMult().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(
                        txt("redline_1")
                        + (int) (ENGINE_BUFF*effectLevel*100)
                        + txt("%"),
                        false);
        }
        if (index == 1) {
            return new StatusData(
                    txt("redline_2")
                    + (int) (100*BALLISTIC_ROF*effectLevel)
                    + txt("%"),
                    false);
        }
        return null;
    }
}
