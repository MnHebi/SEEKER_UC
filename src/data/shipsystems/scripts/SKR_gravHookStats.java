package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import static data.scripts.util.SKR_txt.txt;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class SKR_gravHookStats extends BaseShipSystemScript {
    
    private final float RANGE=1250, SPEED=0;
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {        
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyMult(id, SPEED * effectLevel);
        }    
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {        
        stats.getMaxSpeed().unmodify(id);
    }
    
    private final String HOOK = txt("hook");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HOOK, false);
        }
        return null;
    }

    private final String READY = txt("ready");
    private final String OUTOFRANGE = txt("range");
    private final String TARGET = txt("target");
    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return READY;
        }
        
        if ((target == null || target == ship) && ship.getShipTarget() != null) {
            return OUTOFRANGE;
        }
        return TARGET;
    }
    
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        if(
                target!=null 
                && 
                (!target.isDrone()||!target.isFighter()) 
                && 
                MathUtils.isWithinRange(ship, target, RANGE)
                &&
                Math.abs(MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), target.getLocation())))<10
                ){
            return target;
        } else {
            return null;
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }
}