//by Tartiflette, 
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
//import data.scripts.util.MagicInterference;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_glareEffect implements BeamEffectPlugin {
    
    private final Color PARTICLE_COLOR = new Color(155,125,0,255);
    private final float PUSH = 0.25f;
//    private final float WIDTH = 10;
//    private boolean runOnce=false;
    private final IntervalUtil timer = new IntervalUtil(0.1f,0.2f);
//    private float barrel = 5;
//    private WeaponAPI weapon;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
                
//        if(!runOnce){
//            runOnce=true;
//            
//            weapon = beam.getWeapon();
//            
//            //only affect non built-in
//            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){
//                MagicInterference.ApplyInterference(weapon.getShip().getVariant());
//            }
//            
//            
////            if(weapon.getSlot().isHidden()){
////                hidden=true;                
////            } else {                
////                weapon.ensureClonedSpec();
////                //reset the horizontal offsets that may have been moved by a previous burst
////                Vector2f Hoffset=weapon.getSpec().getHardpointFireOffsets().get(0);
////                Hoffset.set(Hoffset.x,0);
////                
////                Vector2f Toffset=weapon.getSpec().getTurretFireOffsets().get(0);
////                Toffset.set(Toffset.x,0);
////            }
//        }
        
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        
        //VISUAL EFFECTS
        
        timer.advance(amount);
        if(timer.intervalElapsed()){
            //play sound (to avoid limitations with the way weapon sounds are handled)
//            Global.getSoundPlayer().playSound("SKR_glare_fire", 1f, 1f, weapon.getLocation(), beam.getSource().getVelocity());

            //jazz up the beam source
            engine.addHitParticle(
                    beam.getFrom(),
                    beam.getSource().getVelocity(), 
                    100,
                    0.5f,
                    0.2f, 
                    PARTICLE_COLOR
            );
            engine.addHitParticle(
                    beam.getFrom(),
                    beam.getSource().getVelocity(), 
                    30,
                    0.25f,
                    0.1f, 
                    Color.WHITE
            );
        }
        
        //I'm using a sine three times, thus the timer must run its course three  times over one burst of one second
//        timer= Math.min(1, timer + amount*3);
//        if(timer==1){
//            timer = 0;
//            //timer reached it's cap, move the beam
//            if(!hidden){
//                barrel *= -1;
//                
//                Vector2f Hoffset=weapon.getSpec().getHardpointFireOffsets().get(0);
//                Hoffset.set(Hoffset.x,barrel);
//                
//                Vector2f Toffset=weapon.getSpec().getTurretFireOffsets().get(0);
//                Toffset.set(Toffset.x,barrel);
//            }
//        } else {
//            //else, make the beam flicker
//            float theWidth = Math.max(
//                    0,
//                    Math.min(
//                            WIDTH,
//                            WIDTH * ( 2f * (float) FastTrig.cos( MathUtils.FPI * timer ) -0.2f)
//                    )
//            ) ;
//            beam.setWidth(theWidth);
//        }
        
        //PUSHING EFFECT
        
        if(beam.getDamageTarget()!=null){
            float mult = 1f;
            CombatEntityAPI target = beam.getDamageTarget();
            
            if(target instanceof ShipAPI){
                if(((ShipAPI)target).isCapital()){
                    mult=0.25f;
                } else if(((ShipAPI)target).isCruiser()){
                    mult=0.60f;
                } else if(((ShipAPI)target).isDestroyer()){
                    mult=0.85f;
                }
            }
            
            Vector2f pushing = MathUtils.getPoint(new Vector2f(), 1, beam.getWeapon().getCurrAngle()+180);
            pushing.scale(PUSH * mult * amount);
            Vector2f vel = target.getVelocity();
            vel.scale(1-mult);
        }
    }    
}