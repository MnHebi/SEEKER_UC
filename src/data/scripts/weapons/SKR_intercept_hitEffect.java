package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_intercept_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(250,155,100,255);
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        if(target==null)return;
        
        if(shieldHit){
            
            float direction=VectorUtils.getFacing(projectile.getVelocity());
            direction+= 180 + 180 * FastTrig.sin( MathUtils.FPI * MathUtils.getShortestRotation(direction, VectorUtils.getAngle( point, target.getShield().getLocation() )) / 360 );
            
            for(int i=0; i<10; i++){
                Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 100, direction-45, direction+45);
                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(point, 10),
                        vel,
                        2+4*(float)Math.random(),
                        2,
                        .25f+1*(float)Math.random(),
                        COLOR
                );
                
                if(Math.random()>0.5f){
                    MagicRender.battlespace(
                        Global.getSettings().getSprite("fx","sweetener"),
                        MathUtils.getRandomPointInCircle(point, 5),
                        vel,
                        new Vector2f(
                                MathUtils.getRandomNumberInRange(6, 10),
                                MathUtils.getRandomNumberInRange(24, 48)
                        ),
                        new Vector2f(
                                MathUtils.getRandomNumberInRange(-2, -4),
                                MathUtils.getRandomNumberInRange(-8, -16)
                        ),
                        VectorUtils.getFacing(vel)+90,
                        0,
                        Color.ORANGE,
                        true,
                        0,0,
                        0,0,0,
                        //1,1,0.05f,
                        0.05f,
                        0.25f,
                        MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                        CombatEngineLayers.BELOW_INDICATORS_LAYER
                    );
                }
            }
            engine.addHitParticle(
                    point,
                    target.getVelocity(),
                    150,
                    2,
                    .1f,
                    Color.WHITE
            );
            engine.addHitParticle(
                    point,
                    target.getVelocity(),
                    100,
                    .5f,
                    .35f,
                    Color.RED
            );
            
        } else {
            
            for(int i=0; i<10; i++){
                Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 175, projectile.getFacing()-20, projectile.getFacing()+20);
                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(point, 10),
                        vel,
                        2+4*(float)Math.random(),
                        2,
                        .25f+1*(float)Math.random(),
                        COLOR
                );
                
                if(Math.random()>0.5f){
                    MagicRender.battlespace(
                        Global.getSettings().getSprite("fx","sweetener"),
                        MathUtils.getRandomPointInCircle(point, 5),
                        vel,
                        new Vector2f(
                                MathUtils.getRandomNumberInRange(6, 10),
                                MathUtils.getRandomNumberInRange(24, 48)
                        ),
                        new Vector2f(
                                MathUtils.getRandomNumberInRange(-2, -4),
                                MathUtils.getRandomNumberInRange(-8, -16)
                        ),
                        VectorUtils.getFacing(vel)+90,
                        0,
                        Color.ORANGE,
                        true,
                        0,0,
                        0,0,0,
                        //1,1,0.05f,
                        0.05f,
                        0.25f,
                        MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                        CombatEngineLayers.BELOW_INDICATORS_LAYER
                    );
                }
            }
            engine.addHitParticle(
                    point,
                    target.getVelocity(),
                    150,
                    2,
                    .1f,
                    Color.WHITE
            );
            engine.addHitParticle(
                    point,
                    target.getVelocity(),
                    100,
                    .5f,
                    .35f,
                    Color.RED
            );
        }
    }
}
