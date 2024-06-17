package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;

public class SKR_bullhornDecoEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false;
    private WeaponAPI turret=null;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) return;
        
        //SETUP        
        if(!runOnce){
            runOnce=true;
            for(WeaponAPI w : weapon.getShip().getAllWeapons()){
                if(w==weapon)continue;
                if(MathUtils.isWithinRange(weapon.getLocation(), w.getLocation(), 5)){
                    turret = w;
                }
            }
        }
        if(turret!=null){
             weapon.setCurrAngle(turret.getCurrAngle());
        }
    }
}