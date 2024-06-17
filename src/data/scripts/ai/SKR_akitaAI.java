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
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_akitaAI implements MissileAIPlugin, GuidedMissileAI {
    
    private final CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    //data
    private final float MAX_SPEED;
//    private final float MAX_ACCELERATION;
//    private final float MAX_TURN_RATE;
    private final float DAMPING = 0.1f;
    //delay between target actualisation
    private boolean launch=true;
    private float eccm=2f, timer=0, check=0.25f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public SKR_akitaAI(MissileAPI missile, ShipAPI launchingShip) {
        this.engine = Global.getCombatEngine();
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
//        MAX_ACCELERATION = missile.getAcceleration();
//        MAX_TURN_RATE = missile.getMaxTurnRate();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm=0.5f;
        }
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused or the missile is fading
        if (engine.isPaused() || missile.isFading() || missile.isFizzling()) return;
        
        //always accelerate
        missile.giveCommand(ShipCommand.ACCELERATE);
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (target instanceof ShipAPI && !((ShipAPI)target).isAlive())
                || target.getOwner()==missile.getOwner()
                || !engine.isEntityInPlay(target)
                ){
            setTarget(
                    MagicTargeting.pickMissileTarget(
                            missile,
                            MagicTargeting.targetSeeking.NO_RANDOM,
                            (int)missile.getWeapon().getRange(),
                            360,
                            0,
                            1,
                            3,
                            4,
                            5
                    )
            );
            return;
        }
       
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer=0;
            
            float dist=MathUtils.getDistanceSquared(missile, target);
                    
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            1.5f*dist/6000000)
            );
            
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
        
        //best angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
        //velocity angle correction
        float offCourseAngle = MathUtils.getShortestRotation(
                VectorUtils.getFacing(missile.getVelocity()),
                correctAngle
                );
        
        float correction = MathUtils.getShortestRotation(                
                correctAngle,
                VectorUtils.getFacing(missile.getVelocity())+180
                ) 
                * 0.5f * //oversteer
                (float)((FastTrig.sin(MathUtils.FPI/90*(Math.min(Math.abs(offCourseAngle),45))))); //damping when the correction isn't important
        
        //modified optimal facing to correct the velocity vector angle as soon as possible
        correctAngle = correctAngle+correction;
        
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle);
        
        //smooth accelerations
//        float acc = missile.getAcceleration();
//        acc = MAX_ACCELERATION * (0.2f + 0.8f*(90-Math.min(90, Math.abs(aimAngle)))/90); 
//        float turn = missile.getMaxSpeed();
//        turn = MAX_TURN_RATE * (0.2f + 0.8f*(Math.min(90, Math.abs(aimAngle)))/90);
        
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
