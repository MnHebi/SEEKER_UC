//by Tartiflette, 
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_revolverEffect implements BeamEffectPlugin {
    
    private final Color PARTICLE_COLOR = new Color(255,240,150,255);
    private final IntervalUtil timer = new IntervalUtil(0.03f,0.1f);
    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        //VISUAL EFFECTS
        timer.advance(amount);
        if(!runOnce){
            runOnce=true;
            timer.forceIntervalElapsed();
            if(MagicRender.screenCheck(0.25f, beam.getFrom())){
                for(int i=0; i<10; i++){
                    
                    Vector2f vel = MathUtils.getPoint(new Vector2f(), (float)Math.random()*400f, beam.getWeapon().getCurrAngle());
                    Vector2f.add(vel, beam.getWeapon().getShip().getVelocity(), vel);
                    Vector2f loc = MathUtils.getRandomPointInCircle(beam.getFrom(), 5);
                    engine.addHitParticle(loc, vel, MathUtils.getRandomNumberInRange(4, 8), 1f, MathUtils.getRandomNumberInRange(0.3f, 0.5f),  PARTICLE_COLOR);
                }
                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx", "SKR_plasma_muzzle"),
                        beam.getFrom(),
                        new Vector2f(beam.getWeapon().getShip().getVelocity()),
                        new Vector2f(32,32),
                        new Vector2f(64,512),
                        beam.getWeapon().getCurrAngle()+ MathUtils.getRandomNumberInRange(-5, 5)-90,
                        MathUtils.getRandomNumberInRange(-10, 10),
                        Color.WHITE, 
                        true,
                        0.05f,
                        0.05f,
                        0.1f
                );
                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx", "SKR_plasma_sparks"),
                        beam.getFrom(),
                        new Vector2f(beam.getWeapon().getShip().getVelocity()),
                        new Vector2f(32,32),
                        new Vector2f(360,720),
                        beam.getWeapon().getCurrAngle()+ MathUtils.getRandomNumberInRange(-5, 5)-90,
                        MathUtils.getRandomNumberInRange(-10, 10),
                        Color.WHITE, 
                        true,
                        0f,
                        0f,
                        0.15f
                );
            }
        }
        if(timer.intervalElapsed()){
            if(MagicRender.screenCheck(0.25f, beam.getFrom())){
                //jazz up the beam source
                engine.addHitParticle(
                        beam.getFrom(),
                        beam.getSource().getVelocity(), 
                        25+75*beam.getBrightness(),
                        0.5f,
                        0.2f, 
                        PARTICLE_COLOR
                );
                engine.addHitParticle(
                        beam.getFrom(),
                        beam.getSource().getVelocity(), 
                        10+20*beam.getBrightness(),
                        1f,
                        0.1f, 
                        Color.WHITE
                );
            }
        }
        if(beam.didDamageThisFrame() && beam.getBrightness()>0.5f && MagicRender.screenCheck(0.25f, beam.getTo())){
            
            engine.addHitParticle(
                    beam.getTo(),
                    beam.getDamageTarget().getVelocity(), 
                    50+100*beam.getBrightness(),
                    0.5f,
                    0.3f, 
                    PARTICLE_COLOR
            );
            engine.addHitParticle(
                    beam.getTo(),
                    beam.getDamageTarget().getVelocity(), 
                    25+50*beam.getBrightness(),
                    1f,
                    0.1f, 
                    Color.WHITE
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "SKR_plasma_muzzle"),
                    new Vector2f(beam.getTo()),
                    new Vector2f(beam.getDamageTarget().getVelocity()),
                    new Vector2f(32,32),
                    new Vector2f(128+128*beam.getBrightness(),128),
                    beam.getWeapon().getCurrAngle()+ MathUtils.getRandomNumberInRange(-5, 5)+90,
                    MathUtils.getRandomNumberInRange(-10, 10),
                    new Color(1f,beam.getBrightness(),beam.getBrightness()), 
                    true,
                    0,
                    0.05f+0.05f*beam.getBrightness(),
                    0.05f+beam.getBrightness()*0.15f
            );
        }
    }    
}