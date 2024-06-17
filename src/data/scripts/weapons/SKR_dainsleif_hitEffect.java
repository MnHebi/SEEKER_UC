package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_dainsleif_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(100,155,200,255);
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        //sound
//        Global.getSoundPlayer().playSound("SKR_chain_hit", 1, 1, point, target.getVelocity());
        float size=100;
        if(!(target instanceof MissileAPI)){
            size=200;
            
            //particles post-impact
            for(int i=0; i<10; i++){
                Vector2f loc = MathUtils.getRandomPointInCone(new Vector2f(), 100, projectile.getFacing()-20, projectile.getFacing()+20);
                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(point, 50),
                        loc,
                        5+5*(float)Math.random(),
                        1,
                        1+2*(float)Math.random(),
                        COLOR
                );
            }
            
            //glow wave
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","SKR_drill_cloud"),
                    point,
                    new Vector2f(),
                    new Vector2f(size/2,size/2),
                    new Vector2f(
                            MathUtils.getRandomNumberInRange(size/2, size),
                            MathUtils.getRandomNumberInRange(size/2, size)
                    ),
                    MathUtils.getRandomNumberInRange(-180,180),
                    MathUtils.getRandomNumberInRange(-30, 30),
                    Color.WHITE,
                    true,
                    0,
                    0,
                    1,
                    1,
                    0.05f,
                    0.05f,
                    0.1f,
                    0.4f,
                    CombatEngineLayers.BELOW_INDICATORS_LAYER
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","SKR_drill_cloud"),
                    point,
                    new Vector2f(),
                    new Vector2f(size/2,size/2),
                    new Vector2f(
                            MathUtils.getRandomNumberInRange(size/2, size),
                            MathUtils.getRandomNumberInRange(size/2, size)
                    ),
                    MathUtils.getRandomNumberInRange(-180,180),
                    MathUtils.getRandomNumberInRange(-30, 30),
                    Color.WHITE,
                    true,
                    0,
                    0,
                    1,
                    1,
                    0.05f,
                    0.05f,
                    0.1f,
                    0.5f,
                    CombatEngineLayers.BELOW_INDICATORS_LAYER
            );
//            MagicRender.battlespace(
//                    Global.getSettings().getSprite("fx","SKR_drill_aura"),
//                    point,
//                    new Vector2f(),
//                    new Vector2f(size/2,size/2),
//                    new Vector2f(
//                            MathUtils.getRandomNumberInRange(size/4, size/2),
//                            MathUtils.getRandomNumberInRange(size/4, size/2)
//                    ),
//                    MathUtils.getRandomNumberInRange(-180,180),
//                    MathUtils.getRandomNumberInRange(-30, 30),
//                    Color.WHITE,
//                    true,
//                    0,
//                    0,
//                    1,
//                    1,
//                    0.05f,
//                    0.1f,
//                    0.1f,
//                    0.7f,
//                    CombatEngineLayers.BELOW_INDICATORS_LAYER
//            );
        }
        //visual
        if(MagicRender.screenCheck(1, point)){
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    size*0.66f,
                    1,
                    0.05f,
                    Color.WHITE
            );
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    size,
                    1,
                    0.075f,
                    Color.WHITE
            );
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    size*1.5f,
                    1,
                    0.1f,
                    Color.WHITE
            );
            
//            for(int i=0; i<6; i++){
//                engine.addSmoothParticle(
//                        point,
//                        new Vector2f(),
//                        400,
//                        1,
//                        0.5f,
//                        Color.WHITE
//                );
//            }

            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    size,
                    0.5f,
                    1f,
                    COLOR
            );
//            MagicLensFlare.createSharpFlare(engine, projectile.getSource(), point, 10, 300, 0, COLOR, Color.white);

        }
    }
}
