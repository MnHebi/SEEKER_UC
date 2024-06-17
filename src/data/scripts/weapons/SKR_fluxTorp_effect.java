package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

public class SKR_fluxTorp_effect implements OnHitEffectPlugin {
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        if(shieldHit){
            engine.applyDamage(target, point, 1000, DamageType.ENERGY, 0, false, true, projectile.getSource());
        } else if(target instanceof ShipAPI){
            ShipAPI ship = (ShipAPI)target;
            ship.getFluxTracker().setCurrFlux(Math.min(ship.getFluxTracker().getCurrFlux()+1750, ship.getFluxTracker().getMaxFlux()));
        }        
    }
}