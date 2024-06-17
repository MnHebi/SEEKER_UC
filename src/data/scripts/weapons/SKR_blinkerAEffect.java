package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;

public class SKR_blinkerAEffect implements EveryFrameWeaponEffectPlugin {    
    
    private boolean lightOn = true, runOnce=false;
    private int range = 0;
    private float timer=0;
    private final float FREQUENCY =1.5f;
    private final float OFFSET =3f;
    private final float AMPLITUDE = 5;
    private float TIME=0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if (engine.isPaused() || !lightOn) {return;}
        
        if (!runOnce){
            runOnce=true;
            if(weapon.getShip().getOriginalOwner()==-1){
                weapon.getSprite().setColor(Color.BLACK);
                lightOn=false;
                return;
            }
            TIME=(float) Math.random();
        }
        
        float blink = Math.min(
                1, //upper clamp
                Math.max(
                        0, //lower clamp
                        OFFSET-(float)FastTrig.cos((TIME+engine.getTotalElapsedTime(false))*FREQUENCY)*AMPLITUDE
                )
        );
        
        weapon.getSprite().setColor(new Color (1,1,1,blink));
        
        timer+=amount;
        if (timer>1){
            timer=0;
            if (weapon.getShip() == null || !weapon.getShip().isAlive()){
                if (range>=1){
                    weapon.getSprite().setColor(Color.BLACK);
                    lightOn = false;
                }
                range++;
            }
        }
    }    
}