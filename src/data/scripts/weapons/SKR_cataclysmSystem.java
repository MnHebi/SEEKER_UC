package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicRender;
import static data.scripts.util.SKR_txt.txt;
import data.shipsystems.scripts.SKR_pullStats;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_cataclysmSystem implements EveryFrameWeaponEffectPlugin {
    
    private ShipAPI ship,m_fl,m_ml,m_rl,m_fr,m_mr,m_rr;
    private ShipSystemAPI system;
    private boolean sound=false,runOnce=false,A=true,B=true,C=true,D=true,E=true,F=true,active=false;
    private final IntervalUtil grumpy = new IntervalUtil(10,20);
    private final Map<ShipAPI.HullSize, Float> MULT = new HashMap<>();
    {
        MULT.put(ShipAPI.HullSize.DEFAULT, 1f);
        MULT.put(ShipAPI.HullSize.FIGHTER, 0.75f);
        MULT.put(ShipAPI.HullSize.FRIGATE, 0.5f);
        MULT.put(ShipAPI.HullSize.DESTROYER, 0.3f);
        MULT.put(ShipAPI.HullSize.CRUISER, 0.2f);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.1f);
    }
    
    private Map<ShipAPI, Float> CR_SAP = new WeakHashMap<>();
    private ShipAPI target = null;
    private final IntervalUtil sapping = new IntervalUtil(.25f,1.75f);
    private final Integer MAX_CR_LOSS_PER_SECOND = 5;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {return;}
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();
            
            if(!ship.getChildModulesCopy().isEmpty()){
                for (ShipAPI m : ship.getChildModulesCopy()) {
                    switch(m.getStationSlot().getId()) {
                        case "M_FL":
                            m_fl = m;
                            break;
                        case "M_ML":
                            m_ml = m;
                            break;
                        case "M_RL":
                            m_rl = m;
                            break;
                        case "M_FR":
                            m_fr = m;
                            break;
                        case "M_MR":
                            m_mr = m;
                            break;
                        case "M_RR":
                            m_rr = m;
                            break;
                    }
                }
            }
            return;
        }
            
        //module loss
        if(A && (m_fl==null || !m_fl.isAlive())){
            A=false;
            applyWeightLoss("moduleA");
        }
        if(B && (m_ml==null || !m_ml.isAlive())){
            B=false;
            applyWeightLoss("moduleB");
        }
        if(C && (m_rl==null || !m_rl.isAlive())){
            C=false;
            applyWeightLoss("moduleC");
        }
        if(D && (m_fr==null || !m_fr.isAlive())){
            D=false;
            applyWeightLoss("moduleE");
        }
        if(E && (m_mr==null || !m_mr.isAlive())){
            E=false;
            applyWeightLoss("moduleF");
        }
        if(F && (m_rr==null || !m_rr.isAlive())){
            F=false;
            applyWeightLoss("moduleG");
        }
        
        if(system.isActive()){
            
            float level = system.getEffectLevel();            
            
            if(level==1 && !sound){
                sound=true;
                Global.getSoundPlayer().playSound("SKR_pull_fire", 1, 1, ship.getLocation(), ship.getVelocity());
            }
            
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
            
            sapping.advance(amount);
            if(sapping.intervalElapsed()){
                //find the most up-to-date system target
                ShipAPI newTarget = findTarget(ship);
                if(newTarget!=null){
                    target=newTarget;
                } else {
                    // no target available, see if the previous target is still in range
                    if(target!=null && MathUtils.getDistanceSquared(weapon.getLocation(), target.getLocation())>SKR_pullStats.RANGE*SKR_pullStats.RANGE){
                        target=null;
                    }
                }
                
                if(target!=null){    
                    
                    //save cr loss target status
                    if(!CR_SAP.containsKey(target)){
                        CR_SAP.put(target, target.getCurrentCR());
                    }
                    
                    //apply cr loss
                    if(system.isChargedown()){
                        //cr goes back up during chargedown
                        target.setCurrentCR(Math.min(CR_SAP.get(target), target.getCurrentCR()+level*MathUtils.getRandomNumberInRange(0.8f, 1.25f)*MAX_CR_LOSS_PER_SECOND/100));
                    } else {
                        //cr goes down during chargeup and active time (clamped at 5%)
                        target.setCurrentCR(Math.max(0.05f, target.getCurrentCR()-level*MathUtils.getRandomNumberInRange(0.8f, 1.25f)*MAX_CR_LOSS_PER_SECOND/100));
                    }
                    
                    //visual effects                    
                    spawnTentacle(weapon);
                    
                    //swooshes
                    spawnSwooshes();
                }
                
            }
            
            //status for player
            if(CR_SAP.containsKey(engine.getPlayerShip())){
                engine.maintainStatusForPlayerShip("cata_debuff", "graphics/SEEKER/icons/hullsys/SKR_cataDebuff.png", txt("plague_D_aura"), txt("plague_D_auraEffect"), true);
            }
            
        } else if(active||sound){
            active=false;
            sound=false;
            weapon.getAnimation().setFrame(0);
            
            //cr recovery
            target=null;
            for(ShipAPI s : CR_SAP.keySet()){
                s.setCurrentCR(CR_SAP.get(s));
            }
            CR_SAP.clear();
            
        }
                
        //ambience sounds
        grumpy.advance(amount);
        if(grumpy.intervalElapsed()){
            Global.getSoundPlayer().playSound("SKR_rampage_grumpy", MathUtils.getRandomNumberInRange(0.95f, 1.05f), MathUtils.getRandomNumberInRange(0.5f, 0.75f), weapon.getShip().getLocation(), weapon.getShip().getVelocity());
        }
        
    }
    
    private ShipAPI findTarget(ShipAPI ship) {
        
        Misc.FindShipFilter filter = new Misc.FindShipFilter() {
            @Override
            public boolean matches(ShipAPI ship) {
                return (!ship.getEngineController().isFlamedOut()&&!ship.isPhased());
            }
        };

        float range = SKR_pullStats.RANGE;
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) {
                target = null;
            }
        } else {
            if (target == null || target.getOwner() == ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FIGHTER, range, true, filter);
                } else {
                    Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) {
                            target = null;
                        }
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FIGHTER, range, true, filter);
            }
        }
        return ship.getShipTarget();
    }
    
    private void applyWeightLoss(String id){
        ship.getMutableStats().getAcceleration().modifyPercent(id, 10);
        ship.getMutableStats().getDeceleration().modifyPercent(id, 10);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id, 10);
        ship.getMutableStats().getMaxTurnRate().modifyFlat(id, 1);        
    }
    
    private void spawnTentacle(WeaponAPI weapon){
        
        /////////////////////////////
        //                         //
        //    swirling tentacles   //
        //                         //
        /////////////////////////////


        //MagicTrailPlugin.getUniqueID()
        float id=MagicTrailPlugin.getUniqueID();

//        Vector2f from = new Vector2f(weapon.getLocation());
        Vector2f from = MathUtils.getRandomPointInCircle(weapon.getLocation(), 50);
        Vector2f to = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());

        Vector2f go = new Vector2f();
        Vector2f.sub(to, from, go);

        float distance = MathUtils.getDistance(from, to); //only one square root calculation,
        float emmitAngle = weapon.getCurrAngle()+MathUtils.getRandomNumberInRange(-45f, 45f);

        Vector2f point = new Vector2f(from);
        float angle = emmitAngle;

        float waveAmplitude=MathUtils.getRandomNumberInRange(-45f, 45f);
        float waveFrequency=MathUtils.getRandomNumberInRange(9f, 3f);
        float arcMultiplier=MathUtils.getRandomNumberInRange(-1f, 1f);

        for(Integer i=0; i<50; i++){

//                        point = MathUtils.getPoint(point, (i*i*distance)/2500, angle);

            float waveSine = (float)Math.sin(i/waveFrequency);
            float waveOffsetSine = (float)Math.sin((i/waveFrequency)+MathUtils.FPI/2);
            float arcIntensity= -((float)Math.cos(i*MathUtils.FPI/25))/2+0.5f; //"bell curve" along the length of the tentacle to have fixed ends and a moving middle

            point = MathUtils.getPoint(point, distance/40, angle+waveSine*waveAmplitude*arcIntensity);
//            point = MathUtils.getPoint(point, distance/40, angle);

            Vector2f.sub(to, point, go);

            angle += MathUtils.getShortestRotation(angle, VectorUtils.getFacing(go))/15;

            Color trailColor = new Color(
                    MathUtils.clamp(0.25f + Math.max(0,0.5f-i/20f) + Math.max(0f, (i-30f)*0.025f), 0f, 1f),
                    MathUtils.clamp(Math.max(0f, 0.4f-i/20f), 0f, 1f),
                    0.4f+0.2f*arcMultiplier*arcIntensity
            );

//                        //test particle
//                        engine.addHitParticle(
//                                point,
//                                MathUtils.getPoint(
//                                        new Vector2f(),
//                                        50,
//                                        angle
//                                ),
//                                15f,
//                                2f,
//                                i/25f,
//                                3f,
//                                Color.PINK
//                        );                        

            MagicTrailPlugin.addTrailMemberAdvanced(
                    //entity
                    ship,
                    //trail ID
                    id,
                    //sprite
                    Global.getSettings().getSprite("fx", "base_trail_fuzzy"),
//                    Global.getSettings().getSprite("fx", "base_trail_debug"),
                    //segment edge location
                    point,
                    //start speed
                    50f,
                    //end speed
                    0f, 
                    //angle
                    angle+waveOffsetSine*waveAmplitude*arcIntensity,
                    //start angular velocity
                    waveOffsetSine*waveAmplitude*arcIntensity/MathUtils.FPI,
                    //0,
                    //end angular velocity
                    waveOffsetSine*waveAmplitude*arcIntensity/MathUtils.FPI,
                    //0,
                    //start size
                    64f+(32f*arcMultiplier*arcIntensity), 
                    //end size
                    64f+(64f*arcMultiplier*arcIntensity),
                    //start color
                    trailColor,
                    //end color
                    new Color(0.05f,0.05f,0.15f),
                    //opacity
                    Math.min(0.5f+0.4f*arcMultiplier*arcIntensity, 1.7f-i/30f),
                    //fade in
                    i/10f, 
                    //full opacity
                    1f,
                    //fade out
                    3f,
                    //additive
                    true,
                    //texture length
                    512,
                    //texture scroll
                    -128,
                    //other options I don't care about
                    MathUtils.getPoint(
                            new Vector2f(),
                            30*arcIntensity,
                            angle+waveSine*waveAmplitude*2
                    ),
//                    new Vector2f(),
                    null,null,1
            );

        }
    }
    
    private void spawnSwooshes(){
        float size=MathUtils.getRandomNumberInRange(128, 256);
        float angle = MathUtils.getRandomNumberInRange(0,360);
        Vector2f point=MathUtils.getPoint(new Vector2f(), MathUtils.getRandomNumberInRange(0, size/4), angle);

        MagicRender.objectspace(
                Global.getSettings().getSprite("fx", "SKR_drill_swoosh"),
                target,
                point,
                new Vector2f((Vector2f)point.scale(0.5f)),
                new Vector2f(size,size),
                new Vector2f(size*1.5f,size*1.5f),
                angle+MathUtils.getRandomNumberInRange(-45,45),
                MathUtils.getRandomNumberInRange(-45,45),
                false,
                new Color(255,255,255,64), 
                false, 
                1f,
                0.5f, 
                2f,
                false
        );
    }
}
