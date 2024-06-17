package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_ravager_hitEffect implements OnHitEffectPlugin {
    
    private Color COLOR = new Color(200,75,50);
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        boolean visible = MagicRender.screenCheck(1, point);
        
        if(shieldHit){
            float arc = target.getShield().getActiveArc()*(1-0.06f*projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue());
            target.getShield().setActiveArc(arc);
            COLOR = target.getShield().getInnerColor();
            if(arc<15){
                ((ShipAPI)target).getFluxTracker().beginOverloadWithTotalBaseDuration(0.25f);
            }
        } else if(visible){            
            engine.spawnExplosion(
                    point,
                    new Vector2f(),
                    new Color(75,50,50,255),
                    50,
                    0.5f+(float)Math.random()*0.5f
            );
        }
        
        if(visible){
            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    100,
                    0.25f,
                    0.25f,
                    COLOR
            );        
            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    75,
                    0.25f,
                    0.05f,
                    Color.WHITE
            );

            for(int i=0; i<5; i++){
                float mult=(float)Math.random()*5;
                Vector2f dir = MathUtils.getPoint(
                        new Vector2f(),
                        50*mult,
                        VectorUtils.getAngle(target.getLocation(), point)-5+(float)Math.random()*10
                );
                engine.addHitParticle(
                        point, 
                        dir, 
                        7-mult, 
                        1,
                        2/(1+mult),
                        COLOR
                );
            }
        }
    }
}
