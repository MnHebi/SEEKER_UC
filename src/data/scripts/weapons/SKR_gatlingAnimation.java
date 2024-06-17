//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicInterference;
import org.lazywizard.lazylib.MathUtils;

public class SKR_gatlingAnimation implements EveryFrameWeaponEffectPlugin{
        
    private float delay = 0.1f;
    private float timer = 0;
    private float SPINUP = 0.01f;
    private float SPINDOWN = 10f;
    private float PITCH=0;
    
    
    private boolean runOnce=false;
    private boolean hidden=false;
    private AnimationAPI theAnim;
    private int maxFrame;
    private int frame;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                theAnim=weapon.getAnimation();
                maxFrame=theAnim.getNumFrames();
                frame=MathUtils.getRandomNumberInRange(0, maxFrame-1);
            }
            
            //only affect non built-in
            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){
                MagicInterference.applyInterference(weapon.getShip().getVariant());
            }
            
            return;
        }
        
        if(engine.isPaused()||hidden){return;}
        
        timer+=amount;
        if (timer >= delay){
            int skip=0;
            for(float i=timer; i>=delay; i-=delay){
                timer-=delay;
                skip++;
            }
            if (weapon.getChargeLevel()>0){
                delay = Math.max(
                            delay - SPINUP,
                            0.02f
                        );
            } else {
                delay = Math.min(
                            delay + delay/SPINDOWN,
                            0.1f
                        );
            }
            if (!hidden && delay!=0.1f){
                for(int i=skip; i>0; i--){
                    frame++;
                    if (frame==maxFrame){
                        frame=0;
                    }
                }                
                theAnim.setFrame(frame);
            }
        }
        
        //play the spinning sound
        if (delay<0.1){            
            Global.getSoundPlayer().playLoop(
                    "SKR_gatling_spin",
                    weapon,
                    1f-(delay*10f)+PITCH,
                    0.8f-(float)Math.pow(delay*10,3),
                    weapon.getLocation(),
                    weapon.getShip().getVelocity()
            );            
        }
    }
}
