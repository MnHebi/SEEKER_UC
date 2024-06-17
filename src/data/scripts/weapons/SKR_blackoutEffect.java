//by Tartiflette,
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicInterference;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SKR_blackoutEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false;
    private Vector2f barrel = new Vector2f();
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            
            //only affect non built-in
            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){
                MagicInterference.applyInterference(weapon.getShip().getVariant());
            }
        }
        
        if(weapon.getChargeLevel()==1 && weapon.getAmmo()==0){
            
            barrel=MathUtils.getPoint(new Vector2f(), 1000, weapon.getCurrAngle());
            barrel.scale(amount*3);
            Vector2f.add(barrel, weapon.getLocation(), barrel);
            
            engine.spawnProjectile(weapon.getShip(), weapon, "SKR_blackoutEMP", barrel, weapon.getCurrAngle(), weapon.getShip().getVelocity());
                        
            Global.getSoundPlayer().playSound("system_emp_emitter_activate", 1.2f, 1, weapon.getLocation(), weapon.getShip().getVelocity());
            
            engine.addHitParticle(barrel, weapon.getShip().getVelocity(), 150, 0.5f, 0.5f, new Color(255,150,255,200));
            engine.addHitParticle(barrel, weapon.getShip().getVelocity(), 70, 0.5f, 0.15f, Color.WHITE);
            
            for(int i=0; i<3; i++){
                engine.spawnEmpArc(
                        weapon.getShip(),
                        weapon.getLocation(),
                        weapon.getShip(),
                        new SimpleEntity(
                            MathUtils.getRandomPointInCone(
                                    weapon.getLocation(), 
                                    50+100*(float)Math.random(), 
                                    weapon.getCurrAngle()-10,
                                    weapon.getCurrAngle()+10)
                        ),
                        DamageType.KINETIC,
                        0,
                        0,
                        500, 
                        null, 
                        5,
                        new Color(255,150,255,150),
                        new Color(255,175,255,200)
                );
            }            
        }
    }
}