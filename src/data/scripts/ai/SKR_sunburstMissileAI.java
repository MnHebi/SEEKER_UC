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
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicTargeting;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_sunburstMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    //data
    private final float MAX_SPEED;
    private final float DAMPING = 0.1f;
    private float DETONATION_RANGE=150;
    //delay between target actualisation
    private boolean launch=true, SHADER=false;
    private float eccm=0.5f, timer=0, check=0.25f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public SKR_sunburstMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.engine = Global.getCombatEngine();
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            eccm=0.25f;
        }
        DETONATION_RANGE*=DETONATION_RANGE;
        //distortion + light effects
        SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused or the missile is fading
        if (engine.isPaused() || missile.isFading() || missile.isFizzling()) {return;}
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || target.getOwner()==missile.getOwner()
                || !engine.isEntityInPlay(target)
                ){
            setTarget(MagicTargeting.pickMissileTarget(missile,
                    MagicTargeting.targetSeeking.NO_RANDOM,
                    (int)missile.getWeapon().getRange(),
                    360,
                    0,
                    1,
                    3,
                    4,
                    5
            ));
            //forced acceleration by default
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
       
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            
            float dist=MathUtils.getDistanceSquared(missile, target);
            if(dist<DETONATION_RANGE){
                //DETONATE IN RANGE
                detonation();
                return;
            } else if (
                    //DETONATE IF ABOUT TO MISS
                    missile.getElapsed()>2
                    && dist<250000
                    && Math.abs(MathUtils.getShortestRotation(VectorUtils.getFacing(missile.getVelocity()), VectorUtils.getAngle(missile.getLocation(), target.getLocation())))>90
                    ){
                detonation();
                return;                
            }
                    
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
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        missile.getLocation(),
                        lead
                );
        
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
    
    private void detonation(){
        
        // Visual explosion
        engine.spawnExplosion(missile.getLocation(), new Vector2f(), new Color(255,75,160,150), 300, 3+(float)Math.random());
        engine.addHitParticle(missile.getLocation(), new Vector2f(),300,0.5f,0.25f,Color.WHITE);
        
        //createSmoothFlare(CombatEngineAPI engine, ShipAPI origin, Vector2f point, float thickness, float length, float angle, Color fringeColor, Color coreColor)
        MagicLensFlare.createSharpFlare(engine, missile.getSource(), missile.getLocation(), 50, 1000, 0, new Color(255,75,160,255), new Color(255,175,250,255));
        if(SHADER){
            SKR_graphicLibEffects.customLight(missile.getLocation(), null, 500, 0.3f, new Color(255,75,160,255), 0, 0.15f, 0.25f);
        }
        
        for(int i=0; i<30; i++){
            
            Vector2f loc=new Vector2f(missile.getLocation());
            Vector2f vel=MathUtils.getRandomPointInCircle(new Vector2f(), 150);
            Vector2f.add(loc, vel, loc);
            float rand=(float)Math.random();
            engine.addHitParticle(
                    loc,
                    vel,
                    5+(float)Math.random()*5,
                    0.5f,
                    1+(float)Math.random(),
                    new Color(1,0.25f*rand,0.7f*rand)
            );
        }
        
        
        // Spawn mines
        for(int u=0; u<6; u++){
            engine.spawnProjectile(
                    missile.getSource(),
                    missile.getWeapon(),
                    "SKR_sunburstSecondaryA",
                    missile.getLocation(),
                    (float)Math.random()*360,
                    MathUtils.getRandomPointInCircle(
                            new Vector2f(),
                            500
                    )
            );
            
            engine.spawnProjectile(
                    missile.getSource(),
                    missile.getWeapon(),
                    "SKR_sunburstSecondaryB",
                    missile.getLocation(),
                    (float)Math.random()*360,
                    MathUtils.getRandomPointInCircle(
                            new Vector2f(),
                            500
                    )
            );
            
            engine.spawnProjectile(
                    missile.getSource(),
                    missile.getWeapon(),
                    "SKR_sunburstSecondaryC",
                    missile.getLocation(),
                    (float)Math.random()*360,
                    MathUtils.getRandomPointInCircle(
                            new Vector2f(),
                            500
                    )
            );
            
            engine.spawnProjectile(
                    missile.getSource(),
                    missile.getWeapon(),
                    "SKR_sunburstSecondaryD",
                    missile.getLocation(),
                    (float)Math.random()*360,
                    MathUtils.getRandomPointInCircle(
                            new Vector2f(),
                            500
                    )
            );
        }
        
        //sound effect
        
        Global.getSoundPlayer().playSound(
                "SKR_sunburst_deploy",
                1,
                1,
                missile.getLocation(),
                new Vector2f()
        );
        
        // Destroy missile
        engine.applyDamage(
                missile,
                missile.getLocation(),
                1000,
                DamageType.KINETIC,
                0,
                false,
                false,
                missile,
                false
        );
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
