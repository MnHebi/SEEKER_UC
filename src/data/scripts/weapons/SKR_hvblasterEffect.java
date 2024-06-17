//by Tartiflette,
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SKR_plagueEffect;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_hvblasterEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false,hasFired=false;
    private final IntervalUtil particle = new IntervalUtil(0.025f,0.05f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=false;
            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){
                SKR_plagueEffect.ApplyPlague(weapon.getShip().getVariant());
            }
        }
        
        if(weapon.isFiring()){
            float charge = weapon.getChargeLevel();
            if(!hasFired){
                Global.getSoundPlayer().playLoop(
                        "SKR_hvblaster_charge",
                        weapon,
                        1,
                        1,
                        weapon.getLocation(),
                        weapon.getShip().getVelocity()
                );
                particle.advance(amount);
                if(particle.intervalElapsed()){
                    Vector2f loc = MathUtils.getPoint(weapon.getLocation(), 18.5f, weapon.getCurrAngle());
                    Vector2f vel = weapon.getShip().getVelocity();
                    
                    engine.addHitParticle(
//                            MathUtils.getRandomPointInCircle(loc, 5+5*charge),
                            loc,
                            vel,
                            MathUtils.getRandomNumberInRange(20, charge*60+20),
                            MathUtils.getRandomNumberInRange(0.5f, 0.5f+charge),
                            MathUtils.getRandomNumberInRange(0.1f, 0.1f+charge/10),
                            new Color(charge/2, charge/1.5f, charge)
                    );
                    
                    for(int i=0; i<5; i++){
                        Vector2f particleVel = MathUtils.getRandomPointInCircle(new Vector2f(), 35*charge);
                        
                        Vector2f particleLoc = new Vector2f();
                        Vector2f.sub(loc, new Vector2f(particleVel), particleLoc);
                        
                        Vector2f.add(vel, particleVel, particleVel);

                        engine.addHitParticle(
                                particleLoc,
                                particleVel,
                                MathUtils.getRandomNumberInRange(2, charge*2+2),
                                MathUtils.getRandomNumberInRange(0.5f, 0.5f+charge),
                                MathUtils.getRandomNumberInRange(0.75f, 0.75f+charge/4),
                                new Color(charge/4, charge/2, charge)
                        );
                    }
                }
            }
            if(charge==1){
                hasFired=true;
            }
        } else {hasFired=false;}
    }
}