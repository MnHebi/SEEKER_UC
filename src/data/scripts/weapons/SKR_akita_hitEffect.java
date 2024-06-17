package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_akita_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(255,24,64,255);
    
    private final Map<ShipAPI.HullSize, Float> MULT = new HashMap<>();
    {
        MULT.put(ShipAPI.HullSize.DEFAULT, 1f);
        MULT.put(ShipAPI.HullSize.FIGHTER, 1f);
        MULT.put(ShipAPI.HullSize.FRIGATE, 1f);
        MULT.put(ShipAPI.HullSize.DESTROYER, 0.75f);
        MULT.put(ShipAPI.HullSize.CRUISER, 0.5f);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.25f);
    }
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        //visual
        if(MagicRender.screenCheck(1, point)){
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    800,
                    1,
                    0.05f,
                    Color.WHITE
            );
            
            for(int i=0; i<6; i++){
                engine.addSmoothParticle(
                        point,
                        new Vector2f(),
                        400,
                        1,
                        0.5f,
                        Color.WHITE
                );
            }
            for(int i=0; i<20; i++){
                Vector2f loc = MathUtils.getRandomPointInCircle(new Vector2f(), 75);
                engine.addHitParticle(
                        new Vector2f(point.x+loc.x,point.y+loc.y),
                        loc,
                        5+5*(float)Math.random(),
                        1,
                        1+2*(float)Math.random(),
                        COLOR
                );
            }
            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    500,
                    0.5f,
                    2f,
                    COLOR
            );
            
            for(int i=0; i<6; i++){
                MagicLensFlare.createSharpFlare(engine, projectile.getSource(), MathUtils.getRandomPointInCircle(point, 75), 5+5*(float)Math.random(), 250+250*(float)Math.random(), 0, COLOR, Color.white);
            }
        }

        //EMP
        float pow = 1;
        if(shieldHit){
            pow = 0.66f;
            for(int i=0; i<5; i++){
                if(Math.random()<((ShipAPI)target).getFluxTracker().getHardFlux()/((ShipAPI)target).getMaxFlux()){
                    engine.spawnEmpArcPierceShields(
                            projectile.getSource(),
                            point,
                            target,
                            target,
                            DamageType.KINETIC,
                            0, 
                            200,
                            1000,
                            "tachyon_lance_emp_impact",
                            15,
                            COLOR,                                    
                            Color.WHITE
                    );
                } else {
                    engine.spawnEmpArc(
                            projectile.getSource(),
                            point,
                            target,
                            target,
                            DamageType.KINETIC,
                            0, 
                            200,
                            1000,
                            "tachyon_lance_emp_impact",
                            15,
                            COLOR,                                    
                            Color.WHITE
                    );
                }
            }
        } else {     
            for(int i=0; i<5; i++){
                engine.spawnEmpArc(
                        projectile.getSource(),
                        point,
                        target,
                        target,
                        DamageType.KINETIC,
                        0, 
                        200,
                        1000,
                        "tachyon_lance_emp_impact",
                        15,
                        COLOR,                                    
                        Color.WHITE
                );
            }
        }
        
        if(target instanceof ShipAPI){
            pow*=MULT.get(((ShipAPI)target).getHullSize());
        }
        Vector2f vel = target.getVelocity();
        Vector2f.add(
                MathUtils.getPoint(
                        new Vector2f(),
                        500*pow,
                        VectorUtils.getAngle(point, target.getLocation())
                ),
                vel,
                vel);
    }
}
