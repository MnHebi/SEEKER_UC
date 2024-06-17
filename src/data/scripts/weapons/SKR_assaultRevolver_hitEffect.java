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

public class SKR_assaultRevolver_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(250,155,100,255);
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        if(target==null)return;
        
        if(shieldHit){
            
            float direction=VectorUtils.getFacing(projectile.getVelocity());
            direction+= 180 + 180 * FastTrig.sin( MathUtils.FPI * MathUtils.getShortestRotation(direction, VectorUtils.getAngle( point, target.getShield().getLocation() )) / 360 );
            
            for(int i=0; i<10; i++){
                Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 50, direction-45, direction+45);
                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(point, 10),
                        vel,
                        2+6*(float)Math.random(),
                        2,
                        .5f+2*(float)Math.random(),
                        COLOR
                );
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
                    .5f,
                    Color.ORANGE
            );
            
        } else {
            
            for(int i=0; i<10; i++){
                Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 75, projectile.getFacing()+140, projectile.getFacing()+220);
                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(point, 10),
                        vel,
                        2+6*(float)Math.random(),
                        2,
                        .5f+2*(float)Math.random(),
                        COLOR
                );
            }
                
            float size = MathUtils.getRandomNumberInRange(16, 24);
            MagicRender.battlespace(
                Global.getSettings().getSprite("fx","SKR_plasma_sparks"),
                point,
                new Vector2f(),
                new Vector2f(size,size),
                new Vector2f(16*size,16*size),
                projectile.getFacing()+90,
                0,
                Color.WHITE,
                true,
                0,0,
                0,0,0,
                //1,1,0.05f,
                0.05f,
                0.15f,
                0.1f,
                CombatEngineLayers.BELOW_INDICATORS_LAYER
            );
                    
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
                    .5f,
                    Color.ORANGE
            );
        }
    }
}
