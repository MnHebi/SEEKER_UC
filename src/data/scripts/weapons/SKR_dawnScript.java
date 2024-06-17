/*
    By Tartiflette
 */
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

public class SKR_dawnScript implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false;
    private ShipSystemAPI system;
    
    IntervalUtil delay = new IntervalUtil(0.5f,1);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            system=weapon.getShip().getSystem();
        }
        
        if(system==null || engine.isPaused() || !weapon.getShip().isAlive()) return;
        
        if(system.isOn() && MagicRender.screenCheck(0.5f, weapon.getLocation())){
            
            for(int i=0; i<Math.random()*5; i++){
                
                SimpleEntity point = new SimpleEntity(MathUtils.getRandomPointInCircle(weapon.getShip().getLocation(), weapon.getShip().getCollisionRadius()/2));
                /*
                public CombatEntityAPI spawnEmpArc(
                        ShipAPI damageSource,
                        Vector2f point,
                        CombatEntityAPI pointAnchor,
                        CombatEntityAPI empTargetEntity,
                        DamageType damageType,
                        float damAmount,
                        float empDamAmount,
                        float maxRange,
                        String impactSoundId,
                        float thickness,
                        Color fringe,
                        Color core)
                */
                engine.spawnEmpArc(
                        weapon.getShip(),
                        MathUtils.getPoint(
                                point.getLocation(),
                                100+200*(float)Math.random(),
                                VectorUtils.getFacing(weapon.getShip().getVelocity())
                        ),
                        point,
                        point,
                        DamageType.FRAGMENTATION,
                        0,
                        0,
                        500,
                        null,
                        3,
                        new Color(255,200,25,100),
                        new Color(255,50,0,50)
                );
            }
        }
    }
}