package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.util.SKR_txt.txt;

public class SKR_burnStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 50f * effectLevel);
//            stats.getAcceleration().modifyFlat(id, 50f * effectLevel);
            
//            stats.getMaxTurnRate().modifyMult(id, 1+ (1 * effectLevel));
//            stats.getTurnAcceleration().modifyMult(id, 1+ (0.5f * effectLevel));

            //using percentage modifier for proper additive boost
            stats.getMaxTurnRate().modifyPercent(id, 100 * effectLevel);
//            stats.getTurnAcceleration().modifyPercent(id, 50 * effectLevel);
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
//        stats.getTurnAcceleration().unmodify(id);
//        stats.getAcceleration().unmodify(id);
//        stats.getDeceleration().unmodify(id);
    }

    
    private final String ENGINE = txt("engine");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(ENGINE, false);
        }
        return null;
    }
}