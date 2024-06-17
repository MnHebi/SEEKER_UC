//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_minigunEffect implements EveryFrameWeaponEffectPlugin{
  
    private float charge = 0f, spinUp=0.02f, delay = 0.1f, timer = 0f, tracer = 0;
    private int frame = 0, numFrames;
    private float minDelay;
    private boolean runOnce = false, sound=false;
    private AnimationAPI theAnim;
    
    private IntervalUtil recoil = new IntervalUtil (0.2f,0.4f);
    private float drift = 0, angle=0, direction=1;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused() || weapon.getSlot().isHidden()) {
            return;
        }
        
        if(!runOnce){
            runOnce=true;
            theAnim = weapon.getAnimation();
            numFrames=theAnim.getNumFrames();
            minDelay=1/theAnim.getFrameRate();
            weapon.ensureClonedSpec();
            angle = 0;
        }
        
        float mult=1;
        if (weapon.getShip() != null) {
            mult /= weapon.getShip().getMutableStats().getBallisticRoFMult().getModifiedValue();
        }
        
        
        //animation
        timer += amount;
        if (timer >= delay) {
            timer -= delay;
            if (weapon.getChargeLevel() >= charge && weapon.getChargeLevel()>0) {
                delay = Math.max(delay - spinUp, minDelay*mult);                
                
            } else {
                
                delay = Math.min(delay + (delay * 4f * spinUp), 0.1f);
                
            }
            if (delay != 0.1f) {
                frame++;
                if (frame == numFrames) {
                    frame = 0;
                }
            }
        }
        theAnim.setFrame(frame);

        charge = Math.max(weapon.getChargeLevel(), charge-0.05f);
        
        if(weapon.getChargeLevel()==1){
            //inacuracy
            
            sound=true;
            recoil.advance(amount);
            if(recoil.intervalElapsed()){
                if(Math.random()<Math.abs(angle/2f)){
                    direction*=-1;
                    if(angle>0){
                        angle=1.9f*weapon.getShip().getMutableStats().getMaxRecoilMult().computeMultMod();
                    } else {
                        angle=-1.9f*weapon.getShip().getMutableStats().getMaxRecoilMult().computeMultMod();
                    }
                }
                drift=MathUtils.getRandomNumberInRange(0,20f);
                recoil.setInterval(0.2f, MathUtils.getRandomNumberInRange(0.25f, 0.55f));
            }

            angle+=amount*drift*direction;

            if(Math.abs(angle)>2f*weapon.getShip().getMutableStats().getMaxRecoilMult().computeMultMod()){
                recoil.forceIntervalElapsed();
            }

            weapon.getSpec().getHardpointAngleOffsets().set(0, angle);
            weapon.getSpec().getTurretAngleOffsets().set(0, angle);
        } else {
            if(sound==true){
                Global.getSoundPlayer().playSound("SKR_minigun_trail", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
                sound=false;
            }
        }
        
        //tracers
        /*
        if(weapon.getChargeLevel()==1){
            tracer+=amount*mult;
            if(tracer>0.5f){
                tracer-=0.5f;
                Vector2f point = MathUtils.getPoint(weapon.getLocation(), 39, weapon.getCurrAngle()+angle);
                engine.spawnProjectile(weapon.getShip(), weapon, "SKR_minigun_tracer", point, weapon.getCurrAngle()+angle, weapon.getShip().getVelocity());
            }
        } else {
            tracer=1;
        }
        */
    }
}
