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
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_gatlingEffect implements BeamEffectPlugin {
    
    private final Color PARTICLE_COLOR = new Color(125,0,155,255);
    private boolean hasFired=false, runOnce=false;
    private final float WIDTH = 20, MAX_OFFSET=2.5f;
    private IntervalUtil timer = new IntervalUtil(0.11f,0.14f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        if(!runOnce){
            runOnce=true;
            beam.getWeapon().ensureClonedSpec();
        }
        
        if(beam.getBrightness()==1) {            
            Vector2f start = beam.getFrom();
            Vector2f end = beam.getTo();
            
            if (MathUtils.getDistanceSquared(start, end)==0){
                return;
            }
            
            //Visual effects are tiggered from the end of the script due to the weapon offsets being applied for the next frame
            if (!hasFired){   
                hasFired=true;
                
                if (beam.getDamageTarget()!=null && MagicRender.screenCheck(0.5f, end)){
                    //visual effect
                    engine.addHitParticle(
                            end,
                            new Vector2f(),
                            150,
                            1f,
                            0.1f,
                            Color.WHITE
                    );
                    engine.spawnExplosion(
                        //where
                        end,
                        //speed
                        (Vector2f) new Vector2f(0,0),
                        //color
                        PARTICLE_COLOR,
                        //size
                        MathUtils.getRandomNumberInRange(50f,100f),
                        //duration
                        0.2f
                    );
                }
                //weapon glow
                engine.addHitParticle(
                        start,
                        new Vector2f(),
                        75,
                        1f,
                        0.3f,
                        new Color(225,100,255,255)
                );
                engine.addHitParticle(
                        start,
                        new Vector2f(),
                        50,
                        1f,
                        0.1f,
                        Color.WHITE
                );
                //play sound (to avoid limitations with the way weapon sounds are handled)
                Global.getSoundPlayer().playSound("SKR_gatling_fire", 1f, 1f, start, beam.getSource().getVelocity());                
            }
            
            if (beam.didDamageThisFrame() && MagicRender.screenCheck(0.25f, end)){
                //visual effect
                engine.spawnExplosion(
                    //where
                    end,
                    //speed
                    (Vector2f) new Vector2f(0,0),
                    //color
                    PARTICLE_COLOR,
                    //size
                    MathUtils.getRandomNumberInRange(50f,100f),
                    //duration
                    0.2f
                );
            }
            
            float theWidth = WIDTH * ( 0.5f * (float) FastTrig.cos( 20*MathUtils.FPI * Math.min(timer.getElapsed(),0.05f) ) + 0.5f ) ;
            beam.setWidth(theWidth);            
            
            timer.advance(amount);
            if (timer.intervalElapsed()){
                hasFired=false;
                
                float offset=
                        MAX_OFFSET * 
                        beam.getSource().getMutableStats().getMaxRecoilMult().getModifiedValue() * 
                        MathUtils.getRandomNumberInRange(-1, 1);
                beam.getWeapon().getSpec().getHardpointAngleOffsets().set(0, offset/2);
                beam.getWeapon().getSpec().getTurretAngleOffsets().set(0, offset);
                beam.getWeapon().getSpec().getHiddenAngleOffsets().set(0, offset);
            }
        }
        
        if(beam.getWeapon().getChargeLevel()<1){
            hasFired=false;
            timer.setElapsed(0);
        }
    }
}