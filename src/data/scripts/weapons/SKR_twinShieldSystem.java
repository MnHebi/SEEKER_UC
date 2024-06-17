package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class SKR_twinShieldSystem implements EveryFrameWeaponEffectPlugin {
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float shieldArc =0;
    private boolean bonus = false, runOnce=false, noShield=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused() || noShield) {return;}
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getShip().getShield()==null){
                noShield=true;
                return;
            }
            ship=weapon.getShip();
            system=ship.getSystem();
            shieldArc=ship.getShield().getArc();
        }
        
        if (system.isActive()) {
            bonus = true;
            float level = system.getEffectLevel();                
            ship.getShield().setArc(shieldArc-(level*0.66f*shieldArc));
        } else if (bonus){
            bonus = false;
            ship.getShield().setArc(shieldArc);
        }
    } 
}
