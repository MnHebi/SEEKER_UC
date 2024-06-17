//by Tartiflette, Anti-missile missile AI: precise and able to randomly choose a target between nearby enemy missiles.
//feel free to use it, credit is appreciated but not mandatory
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
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.FastTrig;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

public class SKR_antiMissileAI implements MissileAIPlugin, GuidedMissileAI {

    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Map< Integer , CombatEntityAPI > MEMBERS = new HashMap();
    private Vector2f lead = new Vector2f();
    private float timer=0, check=0.25f;
    private boolean launch=true;
    //data
    private final float MAX_SPEED;
    private final int SEARCH_RANGE = 1000;
    private final int DANGER_RANGE = 250;
    private final float DAMPING = 0.1f;
    private final Color EXPLOSION_COLOR = new Color(255, 0, 0, 255);
    private final Color PARTICLE_COLOR = new Color(240, 200, 50, 255);
    private final int NUM_PARTICLES = 20;

    public SKR_antiMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed();
    }

    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()){
            return;
        }
        
        // if there is no target, assign one
        if (target == null 
                || !Global.getCombatEngine().isEntityInPlay(target)
                || target.getOwner()==missile.getOwner()
                ) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            setTarget(findRandomMissileWithinRange(missile));
            return;
        }

        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            float dist = MathUtils.getDistanceSquared(missile, target);
            if (dist<2500){
                proximityFuse();
                return;
            }
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.025f,
                            2*dist/4000000)
            );
            lead = AIUtils.getBestInterceptPoint(
                    missile.getLocation(),
                    MAX_SPEED,
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
        
        //turn the missile
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle);
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }
        missile.giveCommand(ShipCommand.ACCELERATE);
        
        // Damp angular velocity if we're getting close to the target angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    private CombatEntityAPI findRandomMissileWithinRange(MissileAPI missile){
        ShipAPI source = missile.getSource();        
        MissileAPI closest = AIUtils.getNearestEnemyMissile(source);

        if (closest != null && MathUtils.isWithinRange(source, closest, 2*SEARCH_RANGE)) {
            //if a missile come too close, or the closest is still far, target this one
            if (MathUtils.isWithinRange(source, closest, DANGER_RANGE)) {
                return closest;
            } else {
                //if the missiles are in normal range
                Vector2f epicenter = source.getLocation();
                MEMBERS.clear();
                MEMBERS.put(0, closest);
                int nbKey = 1;
                //seek all nearby missiles, and if they are hostile add them to the hashmap with a entry number
                for ( MissileAPI tmp : CombatUtils.getMissilesWithinRange(epicenter, SEARCH_RANGE)) {
                    if ( tmp!= null && tmp.getOwner()!=source.getOwner()) {
                        MEMBERS.put(nbKey, tmp);
                        nbKey++;
                    }
                }
                //choose a random integer within the number of entries, and return the coresponding missile                
                int chooser = (int)(Math.round( Math.random() * nbKey));
                return MEMBERS.get(chooser);
            }
        } else {
            //if no missiles are neaby, try fighters
            MEMBERS.clear();
            int nbKey = 0;         
            for ( ShipAPI tmp : AIUtils.getNearbyEnemies(source, SEARCH_RANGE)) {
                if ( tmp!= null && (tmp.isDrone()||tmp.isFighter()) && tmp.getOwner()!=source.getOwner() ) {
                    MEMBERS.put(nbKey, tmp);
                    nbKey++;
                }
            }
            //choose a random integer within the number of entries, and return the coresponding target
            if (nbKey!=0){
                int chooser = (int)(Math.round( Math.random() * nbKey));
                return MEMBERS.get(chooser);
            }
            else if (AIUtils.getNearestEnemy(source)!=null) {
                return AIUtils.getNearestEnemy(source);
            } else {
                return null;
            }
        }
    }
    
    void proximityFuse(){
        List<MissileAPI> closeMissiles = AIUtils.getNearbyEnemyMissiles(missile, 50); 
        for (MissileAPI cm : closeMissiles) {
            engine.applyDamage(
                    cm,
                    cm.getLocation(),
                    200*800/MathUtils.getDistanceSquared(missile, target),
                    DamageType.FRAGMENTATION,
                    0,
                    false,
                    true,
                    missile
            );
        }
        
        if(MagicRender.screenCheck(0.5f, missile.getLocation())){
            engine.addHitParticle(
                missile.getLocation(),
                new Vector2f(),
                100,
                1,
                0.25f,
                EXPLOSION_COLOR
            );

            for (int i=0; i<NUM_PARTICLES; i++){
                float axis = (float)Math.random()*360;
                float range = (float)Math.random()*100;
                engine.addHitParticle(
                    MathUtils.getPoint(missile.getLocation(), range/5, axis),
                    MathUtils.getPoint(new Vector2f(), range, axis),
                    2+(float)Math.random()*2,
                    1,
                    1+(float)Math.random(),
                    PARTICLE_COLOR
                );
            }
        }
        
        engine.applyDamage(
                missile,
                missile.getLocation(),
                missile.getHitpoints() * 2f,
                DamageType.FRAGMENTATION,
                0f,
                false,
                false,
                missile
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
        public void init(CombatEngineAPI engine) {
    }
}
