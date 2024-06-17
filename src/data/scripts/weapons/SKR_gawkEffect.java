//by Tartiflette, 
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SKR_gawkEffect implements BeamEffectPlugin {
    
    private final Color FLARE_COLOR = new Color(255,36,96,255);
    private boolean runOnce=false;
    private final float WIDTH = 15;
    private final IntervalUtil timer = new IntervalUtil(0.05f,0.15f);
    private ShipAPI target;
    private float power=0;
    private final Vector2f SIZE= new Vector2f(198,17);
    
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
        
        if(beam.getBrightness()>0.5f) {
            
            timer.advance(amount);
            if(timer.intervalElapsed()){
                
                //occasional emp arc across the beam length
                if(Math.random()>0.9){
                    engine.spawnEmpArc(
                            beam.getSource(),
                            beam.getFrom(),
                            beam.getSource(),
                            new SimpleEntity(beam.getTo()),
                            DamageType.KINETIC,
                            0,
                            0,
                            2000,
                            null,
                            15,
                            FLARE_COLOR,
                            Color.WHITE
                    );
                }
                
                //check target for EMP
                if(beam.getDamageTarget() instanceof ShipAPI){
                    ShipAPI t = (ShipAPI)beam.getDamageTarget();

                    if(t!=target){
                        target=t;
                        power=0;
                    } else {
                        power=Math.min(10, power + timer.getElapsed());
                    }

                } else {
                    power=Math.max(0, power-timer.getElapsed());
                }
                
                //deal cummulative EMP damage
                if(power>0&&target!=null){
                    
//                    //square root ramp up
//                    if(Math.random()<(power*power/100)){

                    //linear ramp up
                    if(Math.random()<(power/10)){
                        
                        Vector2f from = MathUtils.getRandomPointOnLine(beam.getFrom(), beam.getTo());
                        //random shield piercing arc
                        if(Math.random()*target.getFluxTracker().getMaxFlux()<target.getFluxTracker().getHardFlux()){
                            engine.spawnEmpArcPierceShields(
                                    beam.getSource(),
                                    from,
                                    beam.getSource(),
                                    target,
                                    DamageType.KINETIC,
                                    0,
                                    100,
                                    2000,
                                    "tachyon_lance_emp_impact",
                                    10+10*(float)Math.random(),
                                    new Color((int)(FLARE_COLOR.getRed()*(0.5f+0.5f*(float)Math.random())),FLARE_COLOR.getGreen(),FLARE_COLOR.getBlue()),
                                    Color.WHITE
                            );
                        } else {
                            engine.spawnEmpArc(
                                    beam.getSource(),
                                    from,
                                    beam.getSource(),
                                    target,
                                    DamageType.KINETIC,
                                    0,
                                    100,
                                    2000,
                                    "tachyon_lance_emp_impact",
                                    10+10*(float)Math.random(),
                                    new Color((int)(FLARE_COLOR.getRed()*(0.5f+0.5f*(float)Math.random())),FLARE_COLOR.getGreen(),FLARE_COLOR.getBlue()),
                                    Color.WHITE
                            );
                        }
                    }
                }
            }
        } else{
            timer.setElapsed(0);
            power=0;
            target=null;
        }
        
        if(Math.random()<amount*30){  
            Vector2f size = new Vector2f(SIZE);
            size.scale(2*beam.getBrightness()*(float)Math.random());
            
            Vector2f growth = new Vector2f(SIZE);
            growth.scale((float)Math.random());
            
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "SKR_flare"),
                    new Vector2f(beam.getFrom()),
                    new Vector2f(beam.getSource().getVelocity()),
                    new Vector2f (size),
                    new Vector2f (growth),
                    0,
                    0,
                    FLARE_COLOR,
                    true,
                    0,
                    0.05f,
                    0.05f
            );
            
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "SKR_flare"),
                    new Vector2f(beam.getFrom()),
                    new Vector2f(beam.getSource().getVelocity()),
                    new Vector2f ((Vector2f)size.scale(0.5f)),
                    new Vector2f (growth),
                    0,
                    0,
                    FLARE_COLOR,
                    true,
                    0,
                    0.05f,
                    0.05f
            );
            
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), 150*(float)Math.random()*beam.getBrightness(), 0.5f, 0.1f, FLARE_COLOR);
            engine.addHitParticle(beam.getFrom(), beam.getSource().getVelocity(), 50*(float)Math.random()*beam.getBrightness(), 0.5f, 0.1f, Color.WHITE);
        }
        beam.setWidth((WIDTH+(float)Math.random()*2*WIDTH)*beam.getBrightness());
    }
}