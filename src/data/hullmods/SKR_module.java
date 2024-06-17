package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.awt.Color;

public class SKR_module extends BaseHullMod {
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI parent = ship.getParentStation();
        if(!ship.isAlive() || parent==null || !parent.isAlive()){
            return;
        }
        
        //engines
        if(!ship.getEngineController().getShipEngines().isEmpty()){
            if(parent.getEngineController().isAccelerating()){
                ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
            } else if(parent.getEngineController().isAcceleratingBackwards()){
                ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
            } else if(parent.getEngineController().isDecelerating()){
                ship.giveCommand(ShipCommand.DECELERATE, null, 0);
            } else if(parent.getEngineController().isStrafingLeft()){
                ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            } else if(parent.getEngineController().isStrafingRight()){
                ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            }

            if(parent.getSystem().isActive()){
                float level = parent.getSystem().getEffectLevel();
                ship.getEngineController().fadeToOtherColor(0, new Color(100,255,100,255), new Color(100,255,100,255), level, 1);
                ship.getEngineController().extendFlame(0, 1+level, 1+level, 1+0.5f*level);
            }
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
}