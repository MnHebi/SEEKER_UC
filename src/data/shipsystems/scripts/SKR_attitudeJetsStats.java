package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.SKR_txt.txt;

public class SKR_attitudeJetsStats extends BaseShipSystemScript {

    private final float BONUS = 100;
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getMaxSpeed().modifyFlat(id, BONUS);
        stats.getAcceleration().modifyPercent(id, BONUS*3 * effectLevel);
        stats.getDeceleration().modifyPercent(id, BONUS*3 * effectLevel);
        stats.getTurnAcceleration().modifyFlat(id, BONUS/3 * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, BONUS*2 * effectLevel);
        stats.getMaxTurnRate().modifyFlat(id, BONUS/3);
        stats.getMaxTurnRate().modifyPercent(id, BONUS*2);

        if (stats.getEntity() instanceof ShipAPI && false) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            String key = ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            if (state == State.IN) {
                if (test == null && effectLevel > 0.2f) {
                    Global.getCombatEngine().getCustomData().put(key, new Object());
                    ship.getEngineController().getExtendLengthFraction().advance(1f);
                    for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                        if (engine.isSystemActivated()) {
                            ship.getEngineController().setFlameLevel(engine.getEngineSlot(), 1f);
                        }
                    }
                }
            } else {
                Global.getCombatEngine().getCustomData().remove(key);
            }
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
    private final String SPEED = txt("speed");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(MANEUVERABILITY, false);
        } else if (index == 1) {
            return new StatusData(SPEED+BONUS, false);
        }
        return null;
    }
}
