package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
//import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import org.lwjgl.util.vector.Vector2f;

public class SKR_rampageSystem implements EveryFrameWeaponEffectPlugin {
    
    private ShipAPI ship,moduleA,moduleB;
    private ShipSystemAPI system;
    private boolean bonus = false, runOnce=false,A=true,B=true,SHADER=false,active=false, arrival=false;
    private float burstTrigger = 0.9f;
    private final IntervalUtil burstDelay = new IntervalUtil (15,20), grumpy = new IntervalUtil(10,20);
    private float bursting = 0, burstRange;
    private final float BASE_BURST_RANGE=1000, FORCE_MULT=3000;
    private final Map<ShipAPI.HullSize, Float> MULT = new HashMap<>();
    {
        MULT.put(ShipAPI.HullSize.DEFAULT, 1f);
        MULT.put(ShipAPI.HullSize.FIGHTER, 0.9f);
        MULT.put(ShipAPI.HullSize.FRIGATE, 0.8f);
        MULT.put(ShipAPI.HullSize.DESTROYER, 0.7f);
        MULT.put(ShipAPI.HullSize.CRUISER, 0.6f);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.5f);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused()) {return;}
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();
            SHADER=Global.getSettings().getModManager().isModEnabled("shaderLib");
            
            if(!ship.getChildModulesCopy().isEmpty()){
                for (ShipAPI m : ship.getChildModulesCopy()) {
                    switch(m.getStationSlot().getId()) {
                        case "LEFT":
                            moduleA = m;
                            break;
                        case "RIGHT":
                            moduleB = m;
                            break;
                    }
                }
            }            
            return;
        }
            
        if(A && moduleA!=null && moduleA.isAlive()){
            if(system.isActive()){
                applySystemEffect(moduleA, system.getEffectLevel());
                applySystemVisual(moduleA, system.getEffectLevel());
                if(moduleA.getShield()!=null){
                    moduleA.getShield().toggleOff();
                }
            } else if (bonus){
                unapplySystemEffect(moduleA);
            }
        } else if(A){
            A=false;
            applyWeightLoss("moduleA");
        }
        
        if(B && moduleB!=null && moduleB.isAlive()){
            if(system.isActive()){
                applySystemEffect(moduleB, system.getEffectLevel());
                applySystemVisual(moduleB, system.getEffectLevel());
                if(moduleB.getShield()!=null){
                    moduleB.getShield().toggleOff();
                }
            } else if (bonus){
                unapplySystemEffect(moduleB);
            }
        } else if(B){
            B=false;
            applyWeightLoss("moduleB");
        }
        
        //charge system
        bonus=system.isActive();
        if(bonus){
            applySystemVisual(ship, system.getEffectLevel());
        }
        
        if(system.isActive()){
            float level = system.getEffectLevel();
            active=true;
            weapon.getAnimation().setFrame(1);            
            weapon.getSprite().setColor(
                    new Color(
                            Math.min(1,Math.max(0, level+0.5f)),
                            level,
                            level,
                            Math.min(1,Math.max(0, level*2))
                )
            );
        } else if(active){
            active=false;
            weapon.getAnimation().setFrame(0);
        }
        
        if(!system.isActive()){
            //Gravitic burst
            //triggers on hull loss
            if(ship.getHullLevel()<burstTrigger){
                bursting = 1;
                //range equals BASE_RANGE + up to BASE_RANGE again depending on hull level
                burstRange = 2*BASE_BURST_RANGE - BASE_BURST_RANGE*burstTrigger;
                burstTrigger-=0.2f;
                //reset timer
                burstDelay.setInterval(15, 30);
                //glowy
                weapon.getAnimation().setFrame(1);
            } else {
                //also trigger on a timer
                burstDelay.advance(amount);
                if(burstDelay.intervalElapsed()){
                    if(
                            AIUtils.getNearbyEnemies(ship, burstRange).size()>2 
                            && Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.CRUISER, burstRange, true)!=null
                            ){
                        burstDelay.setInterval(15, 30);
                        bursting = 1;
                        //range equals BASE_RANGE + up to BASE_RANGE again depending on hull level
                        burstRange = 2*BASE_BURST_RANGE - BASE_BURST_RANGE*burstTrigger;
                        //glowy
                        weapon.getAnimation().setFrame(1);
                    } else {
                        //set short timer
                        burstDelay.setInterval(5, 10);
                    }
                }
            }
        }
        
        //apply burst
        if(bursting>0){
            graviticBurst(engine, bursting,amount,burstRange,ship);
            bursting-=amount;
            if(bursting<0){
                bursting=0;
                weapon.getAnimation().setFrame(0);
            } else {
                weapon.getSprite().setColor(
                    new Color(
                            Math.min(1,Math.max(0, bursting+0.5f)),
                            bursting,
                            bursting,
                            Math.min(1,Math.max(0, bursting*2))
                    )
                );
            }
        }
        
        /*
        //warp in effect
        if(!arrival && weapon.getShip().getOriginalOwner()>0){
            arrival = true;
            grumpy.forceIntervalElapsed();
            arrivalEffect(weapon.getShip());
        }
        */
        
        //ambience sounds
        grumpy.advance(amount);
        if(grumpy.intervalElapsed()){
            Global.getSoundPlayer().playSound("SKR_rampage_grumpy", MathUtils.getRandomNumberInRange(0.95f, 1.05f), MathUtils.getRandomNumberInRange(0.5f, 0.75f), weapon.getShip().getLocation(), weapon.getShip().getVelocity());
        }
    }
    
    private void applyWeightLoss(String id){
        ship.getMutableStats().getAcceleration().modifyPercent(id, 50);
        ship.getMutableStats().getDeceleration().modifyPercent(id, 50);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id, 50);
        ship.getMutableStats().getMaxTurnRate().modifyFlat(id, 3);        
    }
    
    private void applySystemEffect(ShipAPI module, float level){        
        module.getMutableStats().getArmorDamageTakenMult().modifyMult(ship.getId(), 1-0.9f*level);
        module.getMutableStats().getHullDamageTakenMult().modifyMult(ship.getId(), 1-0.9f*level);
    }
    
    private void unapplySystemEffect(ShipAPI module){
        module.getMutableStats().getArmorDamageTakenMult().unmodify(ship.getId());
        module.getMutableStats().getHullDamageTakenMult().unmodify(ship.getId());
    }
    
    private void applySystemVisual(ShipAPI theShip, float level){
        if(level<1){
            theShip.setJitterUnder(
                    theShip,
                    new Color(0.25f+0.25f*level, 0.25f*level,0),
                    level,
                    3,
                    5+5*level
            );
            theShip.setJitter(
                    theShip,
                    new Color(0.25f+0.25f*level, 0.25f*level,0),
                    level/2,
                    2,
                    3+3*level
            );
        } else {
            theShip.setJitterUnder(
                    theShip,
                    new Color(0.25f+0.25f*level, 0.25f*level,0),
                    1,
                    4,
                    15
            );
            theShip.setJitter(
                    theShip,
                    new Color(0.25f+0.25f*level, 0.25f*level,0),
                    0.5f,
                    3,
                    10
            );
        }
        
        if(!theShip.getEngineController().getShipEngines().isEmpty()){
            theShip.getEngineController().fadeToOtherColor(0, new Color(50,100,200,255), new Color(50,75,100,50), level, 1);
            if(system.isChargedown()){
                theShip.getEngineController().extendFlame(0, level*2, level*2, level*2);
            } else {
                float power=-MagicAnim.smoothNormalizeRange(level, 0, 0.9f)+3*MagicAnim.smoothNormalizeRange(level, 0.9f, 1);
                theShip.getEngineController().extendFlame(0, power, power, power);
            }
//            if(level<1){
//                theShip.getEngineController().extendFlame(0, -level, -level, -level);
//            } else {
//                theShip.getEngineController().extendFlame(0, 2, 2, 2);
//            }
        }
    }
    
    private void graviticBurst (CombatEngineAPI engine, float intensity, float amount, float range, ShipAPI source){
        
        //flux relief
        if(intensity>0.1){
            source.getMutableStats().getFluxDissipation().modifyPercent("rampage_burst", 1000*intensity);
            source.getMutableStats().getTurnAcceleration().modifyPercent("rampage_burst", 500*intensity);
            source.getMutableStats().getMaxTurnRate().modifyPercent("rampage_burst", 500*intensity);
        } else {
            source.getMutableStats().getFluxDissipation().unmodify("rampage_burst");
            source.getMutableStats().getTurnAcceleration().unmodify("rampage_burst");
            source.getMutableStats().getMaxTurnRate().unmodify("rampage_burst");
        }
        
        //visual ring
        if(intensity==1){
            //sound
            Global.getSoundPlayer().playSound("SKR_rampage_burst", 1, 1, source.getLocation(), source.getVelocity());
            //ripple
            if(SHADER){
                SKR_graphicLibEffects.CustomRippleDistortion(
                        ship.getLocation(), 
                        new Vector2f(ship.getVelocity()),
                        range,
                        25, 
                        false,
                        0,
                        360,
                        0,
                        0.1f,
                        0.6f,
                        0.3f, 
                        0.5f, 
                        0f
                );
            }
            
            //jitter
            for(ShipAPI s : source.getChildModulesCopy()){
                if(s.isAlive()){
                    for(int i=0; i<6; i++){
                        s.addAfterimage(
                                new Color(0.5f,0.75f,0f,0.1f),
                                MathUtils.getRandomNumberInRange(-50, 50),
                                MathUtils.getRandomNumberInRange(-50, 50), 
                                MathUtils.getRandomNumberInRange(-100, 100),
                                MathUtils.getRandomNumberInRange(-100, 100), 
                                5,
                                0.1f,
                                0.2f, 
                                0.5f,
                                true,
                                true,
                                false
                        );
                    }
                }
            }
            for(int i=0; i<6; i++){
                source.addAfterimage(
                        new Color(0.5f,0.75f,0f,0.2f),
                        MathUtils.getRandomNumberInRange(-100, 100),
                        MathUtils.getRandomNumberInRange(-100, 100), 
                        MathUtils.getRandomNumberInRange(-100, 100),
                        MathUtils.getRandomNumberInRange(-100, 100), 
                        10,
                        0.2f,
                        0.3f, 
                        0.5f,
                        true,
                        true,
                        false
                );
            }
            
            //ring
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "glowRing_01"),
                    new Vector2f(source.getLocation()), 
                    new Vector2f(source.getVelocity()),
                    new Vector2f(256,256),
                    new Vector2f(range*2,range*2),
                    (float)Math.random()*360,
                    0,
                    new Color(0.5f,0.8f,0.2f),
                    true,
                    0,
                    0.2f,
                    0.4f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "glowRing_02"),
                    new Vector2f(source.getLocation()), 
                    new Vector2f(source.getVelocity()),
                    new Vector2f(320,320),
                    new Vector2f(range*1.9f,range*1.8f),
                    (float)Math.random()*360,
                    0,
                    new Color(0.1f,0.5f,0.3f),
                    true,
                    0.4f,
                    0f,
                    0.6f
            );
        }
        float effectRange = range*(1-intensity);
        //pushback
        for(CombatEntityAPI e : CombatUtils.getEntitiesWithinRange(source.getLocation(), effectRange)){
            float mult=1;
            //ignores allies
             if(e instanceof ShipAPI){
                 ShipAPI eShip=(ShipAPI)e;
                if (eShip.isAlive()
                        && (                        
                            eShip==ship
                            || eShip==moduleA
                            || eShip==moduleB
                            || eShip.getOwner()==ship.getOwner()
                            || eShip.isPhased()
                            )
                        ){
                    
                    //debug
//                    engine.addHitParticle(new Vector2f(e.getLocation()), new Vector2f(), e.getCollisionRadius()/4, 0.5f, 2f, new Color(0,1,intensity));
                    
                    continue;
                    
                } else {
                    mult=MULT.get(eShip.getHullSize());
                    
                    if (eShip.isDrone()||eShip.isFighter()){
                        eShip.getEngineController().forceFlameout(true);
                    }
                }
            }
            if(e instanceof DamagingProjectileAPI){
                engine.addHitParticle(new Vector2f(e.getLocation()), new Vector2f(), e.getCollisionRadius(), 0.5f, 0.5f, new Color(0,1,intensity));
                engine.addHitParticle(new Vector2f(e.getLocation()), new Vector2f(), e.getCollisionRadius(), 2, 0.1f, Color.WHITE);
                engine.removeEntity(e);
                continue;
            }
            if(e instanceof MissileAPI){
                ((MissileAPI) e).flameOut();
            }
            
            //debug
//            engine.addHitParticle(new Vector2f(e.getLocation()), new Vector2f(), e.getCollisionRadius()/2, 2, 2f, new Color(1,intensity,0));
            
//            float force = 
//                    amount 
//                    * mult
//                    * (0.5f + intensity/2)
//                    * Math.max(
//                            0,
//                            Math.min(
//                                    10,
//                                    (float)Math.pow(range*(1-intensity),2)/MathUtils.getDistanceSquared(e.getLocation(), new Vector2f(source.getLocation()))
//                            )
//                    );
            

            float force = 
                    amount 
                    * mult
                    * Math.max(
                            0,
                            MathUtils.getDistanceSquared(source.getLocation(),e.getLocation())/(float)Math.pow(effectRange,2)
                            
                    );
            
            Vector2f push = MathUtils.getPoint(new Vector2f(), force*FORCE_MULT, VectorUtils.getAngle(new Vector2f(source.getLocation()), e.getLocation()));
            Vector2f.add(new Vector2f(e.getVelocity()), push, e.getVelocity());
        }
    }
    /*
    private void arrivalEffect(ShipAPI ship){
         //sound
//        Global.getSoundPlayer().playSound("SKR_rampage_arrival", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1, ship.getLocation(), ship.getVelocity());
        Global.getSoundPlayer().playUISound("SKR_rampage_arrival", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1);

        Vector2f relocation = ship.getLocation();
        relocation.scale(0.5f);

        //ripple
        if(SHADER){
            SKR_graphicLibEffects.CustomRippleDistortion(
                    ship.getLocation(), 
                    new Vector2f(ship.getVelocity()),
                    720,
                    30, 
                    false,
                    0,
                    360,
                    0,
                    0.1f,
                    0.6f,
                    0.3f, 
                    0.5f, 
                    0f
            );
        }

        //rays
        for(int i=0; i<24; i++){
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "sweetener"
                    ),
                    MathUtils.getRandomPointInCone(
                            ship.getLocation(),
                            720,
                            ship.getFacing()+140,
                            ship.getFacing()+220
                    ),
                    MathUtils.getPoint(
                            new Vector2f(),
                            MathUtils.getRandomNumberInRange(256, 360),
                            ship.getFacing()
                    ),
                    new Vector2f(
                            MathUtils.getRandomNumberInRange(16, 32),
                            MathUtils.getRandomNumberInRange(512, 1024)
                    ),
                    new Vector2f(
                            0,
                            MathUtils.getRandomNumberInRange(-400, -500)
                    ),
                    ship.getFacing()-90,
                    0,
                    Color.WHITE,
                    true,
                    0,
                    0.1f,
                    MathUtils.getRandomNumberInRange(
                            0.5f,
                            3
                    )
            );
        }
        //swooshes
        for(int i=0; i<12; i++){
            float size = MathUtils.getRandomNumberInRange(512, 1024);
            float growth = MathUtils.getRandomNumberInRange(256, 512);
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "SKR_drill_swoosh"
                    ),
                    MathUtils.getRandomPointInCircle(
                            ship.getLocation(),
                            512-size/2
                    ),
                    MathUtils.getRandomPointInCone(
                            new Vector2f(),
                            64,
                            ship.getFacing()+150,
                            ship.getFacing()+210
                    ),
//                    new Vector2f(),
                    new Vector2f(size,size),
                    new Vector2f(growth,growth),
//                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(
                            ship.getFacing()-60,
                            ship.getFacing()-120
                    ),
                    MathUtils.getRandomNumberInRange(-15,15),
//                    0,
                    Color.WHITE,
                    true,
                    0,
                    0.2f,
                    MathUtils.getRandomNumberInRange(
                            0.5f,
                            1.5f
                    )
            );
        }
        
        //negative cloud
        MagicRender.battlespace(
                Global.getSettings().getSprite(
                        "fx",
                        "skr_can_glow"
                ),
                ship.getLocation(),
                new Vector2f(ship.getVelocity()),
                new Vector2f(1024,1027),
                new Vector2f(512,512),
                (float)Math.random()*360,
                MathUtils.getRandomNumberInRange(-5, 5),
                new Color(1f,1f,1f,1f),
                true,
                0,0,0,0,0,
                0,
                0.1f,
                0.2f,
                CombatEngineLayers.ABOVE_SHIPS_LAYER
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite(
                        "fx",
                        "skr_can_glow"
                ),
                ship.getLocation(),
                new Vector2f(ship.getVelocity()),
                new Vector2f(720,720),
                new Vector2f(512,512),
                (float)Math.random()*360,
                MathUtils.getRandomNumberInRange(-5, 5),
                new Color(1f,1f,1f,1f),
                0,0,0,0,0,
                0,
                0.1f,
                0.1f,
                CombatEngineLayers.ABOVE_SHIPS_LAYER,
                GL_ONE_MINUS_SRC_COLOR,
                GL_ONE_MINUS_SRC_ALPHA
        );
        
        //trail
        for(int i=0; i<30; i++){
            
            Vector2f point = MathUtils.getPoint(new Vector2f(),50*(i+1),ship.getFacing()+180);
            Vector2f vel = MathUtils.getPoint(new Vector2f(),25*(i+1),ship.getFacing());
            
            //modules
            for(ShipAPI s : ship.getChildModulesCopy()){
                if(s.isAlive()){
                    s.addAfterimage(
                    new Color(0.5f,0.25f,1f,0.15f),
                    point.x,
                    point.y,
                    vel.x,
                    vel.y,
                    0.1f,
                    0,
                    0.1f,
                    2.5f-(0.1f*i),
                    false, 
                    true,
                    false
                    );
                }
            }
            //ship
            ship.addAfterimage(
                    new Color(0.5f,0.25f,1f,0.15f),
                    point.x,
                    point.y,
                    vel.x,
                    vel.y,
                    0.1f,
                    0,
                    0.1f,
                    2.5f-(0.1f*i),
                    false, 
                    true,
                    false
            );
        }
    }
    */
}
