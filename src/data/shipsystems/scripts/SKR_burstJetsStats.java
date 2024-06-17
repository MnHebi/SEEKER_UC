package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.SKR_txt.txt;

public class SKR_burstJetsStats extends BaseShipSystemScript {

    private final float ACCELERATION = 4000;
    private final float DECELERATION = 2000;
    private final float SPEED = 300;
    private final float TURNING = 300;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getMaxTurnRate().modifyPercent(id, TURNING * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, TURNING * 2* effectLevel);

        if(state == State.ACTIVE || state == State.OUT){
            stats.getMaxSpeed().modifyPercent(id, SPEED * effectLevel);
        }
        
        if(effectLevel==1){
            stats.getAcceleration().modifyPercent(id, ACCELERATION);
            stats.getDeceleration().modifyPercent(id, DECELERATION);
        } else {
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }
	
    private final String MANEUVERABILITY = txt("maneuverability");
//    private final String SPEED = txt("speed");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(MANEUVERABILITY, false);
        } 
//        else if (index == 1) {
//            return new StatusData(SPEED+BONUS, false);
//        }
        return null;
    }
}
