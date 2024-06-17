package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.magiclib.util.MagicRender;
import java.awt.Color;

public class SKR_driftStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        //visual effect
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship!=null){
            if(MagicRender.screenCheck(1, ship.getLocation())){
                if(Math.random()>0.9f){
                    ship.addAfterimage(new Color(0,200,255,64), 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 5+50*effectLevel, 0, 0, 2*effectLevel, false, false, false);
                }  
            }
            if(state==State.IN && !stats.getMaxSpeed().getPercentMods().containsKey(id)){
                Global.getSoundPlayer().playSound("SKR_drift_charge", 1, 1.75f, ship.getLocation(), ship.getVelocity());
            }
            if(state==State.ACTIVE && !stats.getTimeMult().getPercentMods().containsKey(id)){
                Global.getSoundPlayer().playSound("SKR_drift_active", 1, 1.5f, ship.getLocation(), ship.getVelocity());
            }
        }
        
        if(state==State.IN){
            //ship freezes while charging
            stats.getTurnAcceleration().modifyPercent(id, -100f*effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, -100f*effectLevel);
            stats.getMaxSpeed().modifyPercent(id, -100f*effectLevel);
            stats.getAcceleration().modifyPercent(id, -100f*effectLevel);
            stats.getDeceleration().modifyPercent(id, -100f*effectLevel);
        }
        
        if(state==State.ACTIVE){
            //ship can reorient
            stats.getTurnAcceleration().modifyPercent(id, 300f);
            stats.getMaxTurnRate().modifyPercent(id, 200f);
            //ship can slightly jump forward
            stats.getMaxSpeed().modifyPercent(id, 500f);
            stats.getAcceleration().modifyPercent(id, 500f);
            stats.getDeceleration().modifyPercent(id, 500f);
            //time drift
            stats.getTimeMult().modifyPercent(id, 1000f);            
        }
        
        if(state==State.OUT){
            //ship can reorient
            stats.getTurnAcceleration().modifyPercent(id, 300f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 200f*effectLevel);
            //ship can slightly jump forward
            stats.getMaxSpeed().modifyPercent(id, 500f*effectLevel);
            stats.getAcceleration().modifyPercent(id, 500f);
            stats.getDeceleration().modifyPercent(id, 500f);
            //time drift
            stats.getTimeMult().modifyPercent(id, 1000f*effectLevel);
        }
        
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        
        stats.getTimeMult().unmodify(id);
    }
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Phase Drift in progress", false);
        }
        return null;
    }
}