//By Tartiflette: this low impact script hide a deco weapon when the ship is in refit

package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;

public class SKR_decoHiddenInRefit implements EveryFrameWeaponEffectPlugin{
    
    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        if (!runOnce){
            runOnce=true;
            if(weapon.getShip().getOriginalOwner()==-1){
                weapon.getSprite().setColor(new Color(1,1,1,0f));
            }
        }
    }
}