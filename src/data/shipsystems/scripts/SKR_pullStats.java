package data.shipsystems.scripts;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
//import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;

public class SKR_pullStats extends BaseShipSystemScript {

    public static final float RANGE = 1500f;
    public final Color EFFECT_COLOR = new Color(250, 65, 5, 200);
    
//    private final IntervalUtil zap = new IntervalUtil(0.1f,0.3f);

    @Override
    public void apply(MutableShipStatsAPI stats, final String id, State state, float effectLevel) {
        
//        ShipAPI ship = null;
//        if (stats.getEntity() instanceof ShipAPI) {
//            ship = (ShipAPI) stats.getEntity();
//        } else {
//            return;
//        }        
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        
        FindShipFilter filter = new FindShipFilter() {
            @Override
            public boolean matches(ShipAPI ship) {
                return (!ship.getEngineController().isFlamedOut()&&!ship.isPhased());
            }
        };

        float range = getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) {
                target = null;
            }
        } else {
            if (target == null || target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true, filter);
                } else {
                    Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) {
                            target = null;
                        }
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, range, true, filter);
            }
        }
        return ship.getShipTarget();
    }

    protected float getMaxRange(ShipAPI ship) {
        return RANGE;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != SystemState.IDLE) {
            return null;
        }

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return "READY";
        }
        if (target == null && ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (system.isActive()) {
            return true;
        }
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

}
