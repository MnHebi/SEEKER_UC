/*
    By Tartiflette
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
//import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_gawonSystemScript implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false, SHADER=false, sound=false;
    private ShipSystemAPI SYSTEM;
    private ShipAPI SHIP;
    private boolean active=false;
    private Vector2f source, target;
    private final float MAX_DISPLACEMENT = 1000;
    private final float MAX_REPULSION = 750;
    private final Map<HullSize, Float> MULT = new HashMap<>();
    {
        MULT.put(HullSize.DEFAULT, 1f);
        MULT.put(HullSize.FIGHTER, 0.75f);
        MULT.put(HullSize.FRIGATE, 0.5f);
        MULT.put(HullSize.DESTROYER, 0.3f);
        MULT.put(HullSize.CRUISER, 0.2f);
        MULT.put(HullSize.CAPITAL_SHIP, 0.1f);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            SHIP=weapon.getShip();
            SYSTEM=SHIP.getSystem();
            //distortion + light effects
            SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
        }
        
        if(SYSTEM==null || engine.isPaused() || !SHIP.isAlive()) return;
        
        if(weapon.isFiring()&&!sound){
            sound=true;
            Global.getSoundPlayer().playSound("SKR_gawk_fire", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
        } else if(!weapon.isFiring() && sound){
            sound=false;
        }
        
        if(SYSTEM.isOn()){
            
            if(!active){
//                active=true;
                source = new Vector2f(SHIP.getLocation());
                //find target point
                if(engine.getPlayerShip()==SHIP && SHIP.getMouseTarget()!=null){
                    //try mouse cursor for the player ship
                    target = new Vector2f(SHIP.getMouseTarget());
                    
                    //debug
//                    engine.addHitParticle(target, new Vector2f(), 100, 1, 0.5f, Color.blue);
//                    engine.addHitParticle(source, new Vector2f(), 100, 1, 0.5f, Color.red);
                    
                    if(MathUtils.isWithinRange(target, source, MAX_DISPLACEMENT)){
                        Vector2f.sub(target, source, target);
                    } else {
                        Vector2f.sub(target, source, target);
                        VectorUtils.clampLength(target, MAX_DISPLACEMENT);
                    }
                } else {
                    //use velocity vector otherwise
                    target = new Vector2f(SHIP.getVelocity());
                    if(target==new Vector2f()){
                        target=MathUtils.getPoint(new Vector2f(), MAX_DISPLACEMENT, SHIP.getFacing());
                    } else {
                        VectorUtils.clampLength(target, MAX_DISPLACEMENT, MAX_DISPLACEMENT);
                    }
                }
            }
            
            //smooth the charge for a less jerky effect
//            float smoothCharge = MagicAnim.smooth(SYSTEM.getEffectLevel());
            
            Vector2f loc = new Vector2f(target);
//            loc.scale(smoothCharge);
            loc.scale(SYSTEM.getEffectLevel());
            Vector2f.add(loc, source, loc);
            
            Vector2f shipLoc = SHIP.getLocation();
            shipLoc.set(loc);
            
            //secondary repulsor effect
            for(CombatEntityAPI c : CombatUtils.getEntitiesWithinRange(SHIP.getLocation(), MAX_REPULSION)){
                
                if(c==SHIP)continue;
                if(c instanceof DamagingProjectileAPI && ((DamagingProjectileAPI)c).getSource()==SHIP)continue;
                if(c instanceof ShipAPI && ((ShipAPI)c).isPhased())continue;
                if(!MathUtils.isWithinRange(c.getLocation(), SHIP.getLocation(), MAX_REPULSION))continue;
                
                Vector2f repulsion = MathUtils.getPoint(
                        SHIP.getLocation(), 
                        MAX_REPULSION, 
                        VectorUtils.getAngle(
                                SHIP.getLocation(), 
                                c.getLocation()
                        )
                );
                
                Vector2f.sub(repulsion, c.getLocation(), repulsion);
                repulsion.scale(amount*3);
                
                Vector2f cLoc = c.getLocation();
                Vector2f.add(cLoc, repulsion, cLoc);
                
                Vector2f push = new Vector2f(repulsion);
                if(c instanceof ShipAPI){
                    push.scale(MULT.get(((ShipAPI)c).getHullSize()));
                }
                
                Vector2f cVel = c.getVelocity();
                Vector2f.add(cVel, push, cVel);
                
                if(c.getOwner()!=SHIP.getOwner()){
                    if(c instanceof DamagingProjectileAPI){
                        c.setCollisionClass(CollisionClass.PROJECTILE_FF);
                    }
                    if(c instanceof MissileAPI){
                        ((MissileAPI)c).flameOut();
                    }
                    if(c instanceof ShipAPI && ((ShipAPI)c).isFighter() && !((ShipAPI)c).isPhased()){
                        ((ShipAPI)c).getEngineController().forceFlameout();
                    }
                }
            }
            
            //VISUAL EFFECT
            if(!active){
                active=true;
                if(MagicRender.screenCheck(1, SHIP.getLocation())){
                    //distortion
                    if(SHADER){
                        SKR_graphicLibEffects.CustomRippleDistortion(
                                SHIP.getLocation(),
                                target,
                                375f,
                                100f,
                                false,
                                0,
                                360f,
                                0,
                                1f,
                                1f,
                                0f,
                                0.1f,
                                0f
                        );
                    }
                    //particles
                    for(int i=0; i<50; i++){
                        Vector2f prtcl = MathUtils.getRandomPointInCircle(new Vector2f(), 500);
                        engine.addHitParticle(
                                new Vector2f(SHIP.getLocation().x+prtcl.x/5,SHIP.getLocation().y+prtcl.y/5), 
                                prtcl, 
                                3f+5*(float)Math.random(), 
                                2, 
                                0.25f+(float)Math.random(), 
                                new Color(0.5f+0.25f*(float)Math.random(),0.25f,0.35f)
                        );
                    }
                    //aura
                    float angle = VectorUtils.getFacing(target);
                    
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "SKR_hueShift"),
                            new Vector2f(SHIP.getLocation()),
                            new Vector2f(target),
                            new Vector2f(750,750),
                            new Vector2f(),
                            angle-90,
                            45,
                            new Color(64,64,64),
                            false,
                            0.3f,
                            0.4f,
                            0.3f
                    );
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "SKR_hueShift"),
                            new Vector2f(SHIP.getLocation()),
                            new Vector2f(target),
                            new Vector2f(750,750),
                            new Vector2f(),
                            angle-90,
                            -45,
                            new Color(64,64,64),
                            false,
                            0.3f,
                            0.4f,
                            0.3f
                    );
                }
            }
            float mult = MagicAnim.smoothReturnNormalizeRange(SYSTEM.getEffectLevel(),0,1);
            Vector2f trail = new Vector2f(target);
            trail.scale(0.5f*mult);
            VectorUtils.rotate(trail, 180);
            Vector2f.add(trail, MathUtils.getRandomPointInCircle(new Vector2f(), mult*200), trail);
            
            SHIP.addAfterimage(
//                    new Color(1-SYSTEM.getEffectLevel(),0.5f,SYSTEM.getEffectLevel(),0.1f+mult*0.15f),
                    new Color(0.25f+0.5f*mult,0.25f,0.35f,0.05f+0.15f*mult),
                    0,
                    0,
                    trail.x,
                    trail.y,
                    5f*mult,
//                    0,
                    0.25f,
                    0.1f,
                    0.25f+0.25f*mult,
                    false,
                    true,
                    false
            );
        } else if(active){
            active=false;
        }
    }
}