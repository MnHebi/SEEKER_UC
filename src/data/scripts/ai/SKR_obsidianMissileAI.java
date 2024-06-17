//By Tartiflette, precise oversteering missile AI that tries to correct it's vector to intercept a target as fast as possible.
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.magiclib.util.MagicTargeting;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_obsidianMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    //data
    private final float MAX_SPEED;
    private final float DAMPING = 0.1f;
    private final int SEARCH_CONE=90;
    //delay between target actualisation
    private boolean launch=true;
    private float eccm=3;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public SKR_obsidianMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.engine = Global.getCombatEngine();
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm=1;
        }
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused()) {return;}
        
        if(missile.isFizzling()){
            engine.removeEntity(missile);
            engine.addHitParticle(
                    missile.getLocation(),
                    missile.getVelocity(),
                    25, 
                    0.5f,
                    0.2f,
                    Color.GREEN
            );
            engine.addHitParticle(
                    missile.getLocation(),
                    missile.getVelocity(),
                    50, 
                    0.5f,
                    0.1f,
                    Color.WHITE
            );
        }
        
        //assigning a target upon launch       
        if(launch){
            launch=false;
            setTarget(MagicTargeting.pickMissileTarget(missile,
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    (int)missile.getWeapon().getRange(),
                    SEARCH_CONE,
                    0,
                    1,
                    2,
                    4,
                    4
            ));
            //get a target point
            if(target!=null){
                
                Vector2f point;
                //lead the target
                point = AIUtils.getBestInterceptPoint(
                        missile.getLocation(),
                        MAX_SPEED*eccm,
                        target.getLocation(),
                        target.getVelocity()
                );
                if (lead == null ) {
                    point = target.getLocation(); 
                }
                //spread the shots a bit
                Vector2f.add(
                        point,
                        MathUtils.getRandomPointInCircle(
                                new Vector2f(),
                                target.getCollisionRadius()/2
                        ),
                        point
                );
                
                lead = new Vector2f(point);
            }
            //forced acceleration by default
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
        
        if (target == null
                || target.getOwner()==missile.getOwner()
                || (target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                || !engine.isEntityInPlay(target)
                ){
            //forced acceleration by default
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
        
//        //best velocity vector angle for interception
//        float correctAngle = VectorUtils.getAngle(
//                        missile.getLocation(),
//                        lead
//                );
//        
//        //velocity angle correction
//        float offCourseAngle = MathUtils.getShortestRotation(
//                VectorUtils.getFacing(missile.getVelocity()),
//                correctAngle
//                );
//        
//        float correction = MathUtils.getShortestRotation(                
//                correctAngle,
//                VectorUtils.getFacing(missile.getVelocity())+180
//                ) 
//                * 0.5f * //oversteer
//                (float)((FastTrig.sin(MathUtils.FPI/90*(Math.min(Math.abs(offCourseAngle),45))))); //damping when the correction isn't important
//        
//        //modified optimal facing to correct the velocity vector angle as soon as possible
//        correctAngle = correctAngle+correction;

        float correctAngle=VectorUtils.getAngle(missile.getLocation(), lead);
        
        //Particle for testing purposes
//        engine.addHitParticle(
//                MathUtils.getPoint(missile.getLocation(), 50, correctAngle),
//                new Vector2f(),
//                10,
//                1,
//                0.05f,
//                Color.yellow
//        );
//        
//        engine.addHitParticle(
//                MathUtils.getPoint(missile.getLocation(), 50, VectorUtils.getAngle(missile.getLocation(), lead)),
//                new Vector2f(),
//                10,
//                1,
//                0.05f,
//                Color.red
//        );
//        
//        engine.addHitParticle(
//                lead,
//                new Vector2f(),
//                10,
//                1,
//                0.05f,
//                Color.blue
//        );
        
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle);
        
        missile.giveCommand(ShipCommand.ACCELERATE);            
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}
