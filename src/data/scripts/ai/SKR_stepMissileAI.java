//By Tartiflette.
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.magiclib.util.MagicTargeting;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_stepMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f(0f,0f);
    
    private boolean started = false;
    //data
    private final float MAX_SPEED, OFFSET;
    private final float DAMPING = 0.1f;
    //delay before engine ignition
    private float delay = 0;
    private float eccm=3;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public SKR_stepMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.engine = Global.getCombatEngine();
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm=1;
        }
        OFFSET=(((float)Math.random()*2)-1);
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused()) {return;}
        
        if (missile.isFading()){
            engine.applyDamage(
                    missile,
                    missile.getLocation(),
                    1000,
                    DamageType.FRAGMENTATION,
                    0,
                    true,
                    false,
                    target
            );  
            return;
        } 
        
        //assigning a target if there is none or it got destroyed
        if (!started && (
                target == null
                || target.getOwner()==missile.getOwner()
                || (target instanceof ShipAPI && ((ShipAPI) target).isHulk()) //comment out this line to remove target reengagement
                || !engine.isEntityInPlay(target)
                )){
            setTarget(MagicTargeting.pickMissileTarget(missile,
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    (int)missile.getWeapon().getRange(),
                    360,
                    0,
                    1,
                    2,
                    4,
                    4
            ));
            return;
        }
              
        if(missile.getElapsed()<1){
            delay+=amount;  
            if(delay >= 0.1f){
                delay-=0.1f;
                //fiding lead point to aim to
                lead = AIUtils.getBestInterceptPoint(
                        missile.getLocation(),
                        MAX_SPEED*eccm,
                        target.getLocation(),
                        target.getVelocity()
                );
                if (lead == null ) {
                    lead = target.getLocation();
                }
            }
            //debug
//            engine.addHitParticle(
//                    lead,
//                    new Vector2f(),
//                    10,
//                    1,
//                    2f,
//                    Color.blue
//            );
            //aimAngle = angle between the missile facing and the lead direction
            float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), lead));
            aimAngle+=OFFSET*eccm;
                    
            if (aimAngle < 0) {
                missile.giveCommand(ShipCommand.TURN_RIGHT);
            } else {
                missile.giveCommand(ShipCommand.TURN_LEFT);
            }
            missile.giveCommand(ShipCommand.DECELERATE);
            
            // Damp angular velocity if the missile aim is getting close to the targeted angle
            if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
                missile.setAngularVelocity(aimAngle / DAMPING);
            }
        } else if (target != null || started) {
            if(!started){
                started = true;
                Global.getSoundPlayer().playSound(
                        "SKR_inertialess_fire",
                        1,
                        1,
                        missile.getLocation(),
                        missile.getVelocity()
                );
            }
            missile.setAngularVelocity(0);
            missile.giveCommand(ShipCommand.ACCELERATE);
        } else {
            engine.applyDamage(
                    missile,
                    missile.getLocation(),
                    1000,
                    DamageType.FRAGMENTATION,
                    0,
                    true,
                    false,
                    missile
            );  
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
