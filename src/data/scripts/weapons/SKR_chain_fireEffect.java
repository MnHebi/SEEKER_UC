//by Tartiflette,
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.combat.CombatUtils;

public class SKR_chain_fireEffect implements EveryFrameWeaponEffectPlugin {
    
//    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
//        if(!runOnce){
//            runOnce=true;
//        }
        
        if(weapon.getChargeLevel()==1){
            if(MagicRender.screenCheck(1, weapon.getLocation())){
                engine.addHitParticle(weapon.getLocation(), weapon.getShip().getVelocity(), 250, 0.5f, 0.5f, new Color(255,150,255,200));
                engine.addHitParticle(weapon.getLocation(), weapon.getShip().getVelocity(), 150, 0.5f, 0.15f, Color.WHITE);
                MagicLensFlare.createSharpFlare(engine, weapon.getShip(), weapon.getLocation(), 20, 500, 0, Color.cyan, Color.white);

//                for(DamagingProjectileAPI p : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 100)){
//                    if(p.getWeapon()==weapon){
//                         engine.spawnEmpArc(weapon.getShip(), weapon.getLocation(), weapon.getShip(), p, DamageType.KINETIC, 0, 0, 10000, null, 5, Color.CYAN, Color.white);
//                        break;
//                    }
//                }
            }
        }
    }
}