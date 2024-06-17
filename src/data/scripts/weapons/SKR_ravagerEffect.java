//by Tartiflette
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicInterference;
//import data.scripts.util.SKR_interference;

public class SKR_ravagerEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce=false, hasFired=false;      
    private String COOL="SKR_ravager_cooldown";
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            //only affect non built-in
            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){                
                //INTERFERENCE CHECK
                //SKR_interference.ApplyInterference(weapon.getShip(), weapon);
                MagicInterference.applyInterference(weapon.getShip().getVariant());
                //INTERFERENCE CHECK
            }
            
            //get extra ammo from exanded mags
            if(weapon.getShip().getVariant().getHullMods().contains("magazines")){
                weapon.ensureClonedSpec();
                weapon.setMaxAmmo(30);
                weapon.setAmmo(30);
            }
        }
        
        if (engine.isPaused()) {
            return;
        }
        
        if(weapon.getChargeLevel()<1 && hasFired){
            hasFired=false;
            //sound
            Global.getSoundPlayer().playSound(
                    COOL, 
                    1,
                    1,
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );
        }
        
        if(weapon.getChargeLevel()==1 && weapon.getAmmo()>0){
            hasFired=true;
        }
    }
}
