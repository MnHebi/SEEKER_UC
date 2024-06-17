//by Tartiflette,
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.SKR_plagueEffect;

public class SKR_addPlagueEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=false;
            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){
                SKR_plagueEffect.ApplyPlague(weapon.getShip().getVariant());
            }
        }
    }
}