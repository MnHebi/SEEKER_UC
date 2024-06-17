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
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_modsAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f(0f,0f);
    
    private boolean runOnce = false;
    //data
    private final float MAX_SPEED, OFFSET;
    private final float DAMPING = 0.5f;
    //delay before engine ignition
    private float timer = 1, check=0.25f, dist=0;
    private float eccm=2;
    private int subshots=4;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public SKR_modsAI(MissileAPI missile, ShipAPI launchingShip) {
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
        
        //sideways launch
        if(!runOnce){
            runOnce=true;
            
            float angle=MathUtils.getShortestRotation(VectorUtils.getAngle(missile.getWeapon().getLocation(),missile.getLocation()),missile.getSource().getFacing());
            if(angle>0){
                angle = missile.getSource().getFacing()-90;
            } else {
                angle = missile.getSource().getFacing()+90;
            }
            
            Vector2f.add(
                    missile.getVelocity(), 
                    MathUtils.getPoint(
                            new Vector2f(),
                            50, 
                            angle
                    ), 
                    missile.getVelocity()
            );
            
        }
        //assigning a target if there is none or it got destroyed
        if (target == null
                || target.getOwner()==missile.getOwner()
                || (target instanceof ShipAPI && ((ShipAPI) target).isHulk()) //comment out this line to remove target reengagement
                || !engine.isEntityInPlay(target)
                ){
            
            setTarget(MagicTargeting.pickMissileTarget(missile,
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    (int)missile.getWeapon().getRange(),
                    360,
                    0,
                    1,
                    2,
                    4,
                    4
                )
            );
            return;
        }
              
        if(missile.getElapsed()<0.5f){
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(timer>=check){
            timer =0;
            dist = MathUtils.getDistanceSquared(missile, target);
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.1f,
                            dist/4000000)
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
            
            if(dist<90000 && subshots>0){
                
                Vector2f vel = new Vector2f(missile.getVelocity());
                Vector2f.add(vel, 
                        MathUtils.getPoint(
                                new Vector2f(),
                                100,
                                missile.getFacing()+90+180*subshots),
                        vel
                );
                
                engine.spawnProjectile(
                        missile.getSource(), 
                        missile.getWeapon(), 
                        "SKR_modsSub", 
                        missile.getLocation(), 
                        missile.getFacing(),
                        vel
                );
                
                Global.getSoundPlayer().playSound("annihilator_fire", 1, 0.5f, missile.getLocation(), vel);
                
                subshots-=1;
            }
        }
        
        //best angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
        float wave = (float)FastTrig.cos(missile.getFlightTime()*2+OFFSET)*eccm*Math.min(15-missile.getFlightTime(), (dist/90000));
        
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle+wave);
        
//        //DEBUG
//        SpriteRenderManager.objectspaceRender(
//                Global.getSettings().getSprite("fx","bar"),
//                missile,
//                new Vector2f(),
//                new Vector2f(),
//                new Vector2f(32,64),
//                new Vector2f(),
//                aimAngle,
//                0,
//                true,
//                Color.BLUE,
//                true,
//                0,
//                0,
//                0.1f,
//                false
//        );
        
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
