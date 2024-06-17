package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class SKR_tusk_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(255,155,50,255);
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        //visual
        if(MagicRender.screenCheck(1, point)){
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    600,
                    1,
                    0.05f,
                    Color.WHITE
            );
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    600,
                    1,
                    0.1f,
                    Color.WHITE
            );
        }
        
//        if(MagicRender.screenCheck(1, point)){
//            engine.addHitParticle(
//                    point,
//                    new Vector2f(),
//                    300,
//                    0.25f,
//                    0.25f,
//                    COLOR
//            );
//        }
    }
}
