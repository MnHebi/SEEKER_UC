//By Tartiflette: this low impact script hide a deco weapon when the ship is destroyed

package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;

public class SKR_deckLightsEffect implements EveryFrameWeaponEffectPlugin{
    
    private boolean alive = true, runOnce=false;
    private float timer=0;
    private int range = 0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if (engine.isPaused() || !alive) {return;}
                
        if (!runOnce){
            runOnce=true;
            if(weapon.getShip().getOriginalOwner()==-1){
                weapon.getSprite().setColor(new Color(1,1,1,0.5f));
                return;
            }
        }
        
        timer+=amount;
        if (timer>1){
            timer=0;
            if (weapon.getShip() == null || !weapon.getShip().isAlive()){
                if (range>=1){
                    weapon.getSprite().setColor(Color.BLACK);
                    alive = false;
                }
                range++;
            }
        }
    }
}