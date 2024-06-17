//By Tartiflette

package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SKR_quicksilverEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce = false, boost=false, activation=true, pulse=false, SHADER;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private final String ID="SKR_quicksilverBoost";
    private final IntervalUtil TimePulse=new IntervalUtil(0.1f,0.15f);
    private float TimeDoors=0, TimeGlow=0, GrowPulse=0;
    private Vector2f LocPulse=new Vector2f();
    
    private WeaponAPI PARTICLESL, PARTICLESR;
    private SpriteAPI SYSTEM, SHADOW, DOORL, DOORR, BLOCKL, BLOCKR;
    private float dX, dY, bX, bY, DirDoors=0, DirGlow=0;
    
    private final String zapSprite="zap_0";
    private final int zapFrames=8, offsetbX=8, offsetbY=33, offsetdX=4, offsetdY=-10;    
    
    private final float PULSE_RANGE=1200;
    private float RANGE_MULT;
    
    private final Map<ShipAPI.HullSize,Float> OVERLOAD = new HashMap<>();
    {
        OVERLOAD.put(HullSize.FIGHTER, 1.5f);
        OVERLOAD.put(HullSize.FRIGATE, 2f);
        OVERLOAD.put(HullSize.DESTROYER, 3f);
        OVERLOAD.put(HullSize.CRUISER, 4f);
        OVERLOAD.put(HullSize.CAPITAL_SHIP, 5f);
        OVERLOAD.put(HullSize.DEFAULT, 2f);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) {return;}
        
        if (!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();  
            //distortion + light effects
            SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");    
            
            weapon.getAnimation().setFrame(1);
            SYSTEM=weapon.getSprite();
            SYSTEM.setColor(Color.BLACK);
            
            RANGE_MULT = ship.getMutableStats().getSystemRangeBonus().getBonusMult();
            
            for(WeaponAPI w : ship.getAllWeapons()){
                switch(w.getSlot().getId()){
                    case "DOOR_L":
                        PARTICLESL=w;
                        DOORL=w.getSprite();
                        dX=DOORL.getCenterX();
                        dY=DOORL.getCenterY();
                        break;
                    case "DOOR_R":
                        PARTICLESR=w;
                        DOORR=w.getSprite();
                        break;
                    case "BLOCK_L":
                        BLOCKL=w.getSprite();
                        bX=BLOCKL.getCenterX();
                        bY=BLOCKL.getCenterY();
                        break;
                    case "BLOCK_R":
                        BLOCKR=w.getSprite();
                        break;
                    case "SHADOW":
                        SHADOW=w.getSprite();
                        break;
                    default:
                }
            }
        }
        
        if(!ship.isAlive()){             
            SYSTEM.setColor(Color.BLACK);
            return;
        }
        
        if(system.isActive()){
            float charge=system.getEffectLevel();
            if(!pulse){
                if(charge<1 && charge>0.25f){
                    //only spawn the particles when close to the camera
                    if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
                        for(int i=0; i<amount*50*charge; i++){
                            Vector2f point, vel=new Vector2f();
                            float dir;
                            if(Math.random()>0.5f){
                                dir = ship.getFacing()-90;
                                point=MathUtils.getRandomPointInCone(PARTICLESR.getLocation(), 25+50*charge, dir-20-10*charge, dir+20+10*charge);
                                Vector2f.sub(PARTICLESR.getLocation(), point, vel);
                            } else {                        
                                dir = ship.getFacing()+90;
                                point=MathUtils.getRandomPointInCone(PARTICLESL.getLocation(), 25+50*charge, dir-20-10*charge, dir+20+10*charge);
                                Vector2f.sub(PARTICLESL.getLocation(), point, vel);
                            }

                            engine.addHitParticle(
                                    point,
                                    vel,
                                    3+charge, 
                                    0.5f+0.5f*charge, 
                                    (0.5f+charge+(float)Math.random())/6, 
                                    new Color(charge*0.5f,charge*0.9f,1)
                            );

                            if(Math.random()>0.8){
                                engine.addHitParticle(
                                        PARTICLESL.getLocation(),
                                        ship.getVelocity(),
                                        3+charge*(float)Math.random()*150, 
                                        0.5f, 
                                        0.1f+(float)Math.random()*0.2f, 
                                        new Color(charge*0.5f,charge*0.9f,1)
                                );
                            }
                            if(Math.random()>0.8){
                                engine.addHitParticle(
                                        PARTICLESR.getLocation(),
                                        ship.getVelocity(),
                                        3+charge*(float)Math.random()*150, 
                                        0.5f, 
                                        0.1f+(float)Math.random()*0.2f, 
                                        new Color(charge*0.5f,charge*0.9f,1)
                                );
                            }
                        }
                    }
                } else if(charge==1){
                    pulse=true;
                    GrowPulse=0;
                    LocPulse = ship.getLocation();
                    PARTICLESL.beginSelectionFlash();
                    PARTICLESR.beginSelectionFlash();
                    DirGlow=-0.25f;
                    TimeGlow=1;
                    SYSTEM.setColor(Color.WHITE);
                    
                    //sound stuff
                    
                    Global.getSoundPlayer().playSound(
                            "SKR_pulse_out",
                            (float)Math.random()/5+0.9f,
                            3,
                            LocPulse,
                            new Vector2f()
                    );
                    
                    //visual stuff
                    engine.addSmoothParticle(
                            LocPulse,
                            ship.getVelocity(),
                            500,
                            0.5f,
                            0.5f,
                            Color.pink);
                    engine.addHitParticle(
                            LocPulse,
                            ship.getVelocity(),
                            300,
                            2,
                            0.25f,
                            Color.white);                    
                    
                    if(SHADER){
                        SKR_graphicLibEffects.quicksilverRing(ship);
                    }
                    
                    float rotation = (float)Math.random()*360;
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx","glowRing_01"),
                            LocPulse, 
                            new Vector2f(),
                            new Vector2f(200f,200f), 
                            (Vector2f) new Vector2f(3800f,3800f).scale(RANGE_MULT), 
                            rotation, 
                            0,
                            Color.WHITE,
                            true,
                0,0,0.4f,0.8f,0,
                            0f, 
                            0.1f, 
                            0.25f,
                            CombatEngineLayers.BELOW_SHIPS_LAYER
                    );
                    
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx","glowRing_02"),
                            LocPulse, 
                            new Vector2f(),
                            new Vector2f(200f,200f), 
                            (Vector2f) new Vector2f(3800f,3800f).scale(RANGE_MULT), 
                            rotation, 
                            0,
                            Color.WHITE,
                            true,
                0,0,0.4f,0.8f,0,
                            0.35f, 
                            0f, 
                            0.5f,
                            CombatEngineLayers.BELOW_SHIPS_LAYER
                    );
                }
            }            
            if(activation){
                activation=false;
                DirDoors=2f;
            }
                        
        } else if(!activation){
            activation=true;
            pulse=false;
            GrowPulse=0;
            DirDoors=-1;
            SYSTEM.setColor(Color.BLACK);
        }        
        
        //DOORS STUFF
        if(DirDoors!=0){
            
            TimeDoors+=DirDoors*(amount/2);            
            if(DirDoors>0){
                if(TimeDoors>1){
                    TimeDoors=1;
                    DirDoors=0;
                    DirGlow=2f;
                }
            } else if(TimeDoors<0){
                TimeDoors=0;
                DirDoors=0;
            }
            
            BLOCKR.setCenter(bX-(MagicAnim.smoothNormalizeRange(TimeDoors,0f,0.75f)*offsetbX), bY+(MagicAnim.smoothNormalizeRange(TimeDoors,0.25f,1f)*offsetbY));                    
            BLOCKL.setCenter(bX+(MagicAnim.smoothNormalizeRange(TimeDoors,0f,0.75f)*offsetbX), bY+(MagicAnim.smoothNormalizeRange(TimeDoors,0.25f,1f)*offsetbY));
            
            DOORR.setCenter(dX-(MagicAnim.smoothNormalizeRange(TimeDoors,0f,0.75f)*offsetdX), dY+(MagicAnim.smoothNormalizeRange(TimeDoors,0.25f,1f)*offsetdY));                    
            DOORL.setCenter(dX+(MagicAnim.smoothNormalizeRange(TimeDoors,0f,0.75f)*offsetdX), dY+(MagicAnim.smoothNormalizeRange(TimeDoors,0.25f,1f)*offsetdY));
            
            float color = 1-(MagicAnim.smoothNormalizeRange(TimeDoors,0,0.75f));
            SHADOW.setColor(new Color(1f,1f,1f,color));
        }
        
        //GLOW STUFF
        if(DirGlow!=0){
            TimeGlow+=DirGlow*amount;
            if(DirGlow>0 && TimeGlow>=1){
                TimeGlow=1;
                DirGlow=0;
                SYSTEM.setColor(Color.WHITE);
            } else if(DirGlow<0 && TimeGlow<=0){
                TimeGlow=0;
                DirGlow=0;
                SYSTEM.setColor(Color.BLACK);
            } else {                
                float glow = MagicAnim.smoothNormalizeRange(TimeGlow, 0f, 1f);
                SYSTEM.setColor(new Color(MagicAnim.smoothNormalizeRange(TimeGlow, 0.1f, 0.75f),glow,glow));
            }
        }
        
        //PULSE STUFF        
        if(pulse && GrowPulse<1){
            TimePulse.advance(amount);
            GrowPulse+=amount;
            
            //deactivation Stuff
            if(TimePulse.intervalElapsed()){
                for(CombatEntityAPI e : CombatUtils.getEntitiesWithinRange(LocPulse, RANGE_MULT*PULSE_RANGE*GrowPulse)){
                    
                    if(e.getCollisionClass()==CollisionClass.NONE){
                        continue;
                    }
                    
                    //EMP ships
                    if(e instanceof ShipAPI){
                        ShipAPI s = (ShipAPI)e;
                        
                        //ignore self and phased ships
                        if(s.isPhased() || s==ship){
                            continue;
                        }
                        
                        //allies get half the damage
                        if(s.getOwner()==ship.getOwner()){
                            s.getFluxTracker().beginOverloadWithTotalBaseDuration(0.5f);
                        } else {
                            //disable
                            s.getFluxTracker().beginOverloadWithTotalBaseDuration(OVERLOAD.get(s.getHullSize()));
                        
                            Vector2f loc = MathUtils.getPoint(
                                    ship.getLocation(),
                                    ship.getCollisionRadius()-100,
                                    VectorUtils.getAngle(e.getLocation(),ship.getLocation())+180
                            );

                            engine.spawnEmpArc(
                                    ship,
                                    loc,
                                    new SimpleEntity(e.getLocation()),
                                    e,
                                    DamageType.FRAGMENTATION,
                                    250+(1-GrowPulse)*500,
                                    500+(1-GrowPulse)*1000,
                                    3000,
                                    null,
                                    (1-GrowPulse)*5+2,
                                    new Color(255,75,150,155),
                                    new Color(255,150,200,200)
                            );
                        }
                        
                        //push
                        CombatUtils.applyForce(e, VectorUtils.getAngle(LocPulse, e.getLocation()), 10);
                        
                    //DENY MISSILES AND PROJECTILES
                    }else if(!MathUtils.isWithinRange(e, LocPulse, RANGE_MULT*PULSE_RANGE*GrowPulse-300)){
                            
                        if(e instanceof MissileAPI){
                            MissileAPI m = (MissileAPI)e;                                
                            m.flameOut();
                            m.setArmedWhileFizzling(false);
                            m.setArmingTime(10);

                            MagicRender.objectspace(
                                    Global.getSettings().getSprite("fx",zapSprite+new Random().nextInt(zapFrames)),
                                    e,
                                    new Vector2f(), 
                                    new Vector2f(),
                                    new Vector2f(e.getCollisionRadius(),e.getCollisionRadius()), 
                                    new Vector2f(), 
                                    (float)Math.random()*360, 
                                    0,
                                    true,
                                    Color.WHITE,
                                    true,
                                    0, 
                                    0.2f, 
                                    0.1f,
                                    true
                            );           

                            //push
                            CombatUtils.applyForce(e, VectorUtils.getAngle(LocPulse, e.getLocation()), 5);
                            
                        } else if(e instanceof DamagingProjectileAPI){
                            engine.addSmoothParticle(
                                    e.getLocation(),
                                    new Vector2f(),
                                    25,
                                    1,
                                    0.25f,
                                    new Color(255,75,150,155)
                            );
                            engine.removeEntity(e);                                
                        }
                    }                    
                }
            }
        }
        
        //PHASE BOOST
        if(ship.isPhased()){
            if(!boost){
                boost=true;
                ship.getMutableStats().getMaxSpeed().modifyFlat(ID, 100);
                //using percentage modifier for proper additive boost
                ship.getMutableStats().getAcceleration().modifyPercent(ID, 300);
                ship.getMutableStats().getDeceleration().modifyPercent(ID, 300);
                ship.getMutableStats().getMaxTurnRate().modifyPercent(ID, 500);
                ship.getMutableStats().getTurnAcceleration().modifyPercent(ID, 500);   
            }
        } else if(boost){
            boost=false;
            ship.getMutableStats().getMaxSpeed().unmodify(ID);
            ship.getMutableStats().getAcceleration().unmodify(ID);
            ship.getMutableStats().getDeceleration().unmodify(ID);
            ship.getMutableStats().getMaxTurnRate().unmodify(ID);
            ship.getMutableStats().getTurnAcceleration().unmodify(ID);            
        }        
    }  
}