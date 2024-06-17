//by Tartiflette, this script raise the damage recieved by a target when "painted" by a specific weapon
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicInterference;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_painterEffect implements EveryFrameWeaponEffectPlugin {

    private final String ID = "PAINTED";
    public Map< ShipAPI , Float > TARGETS = new HashMap<>();
    
    private final Vector2f SIZE = new Vector2f(28,19);
    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            
            //only affect non built-in
            if(weapon.getShip().getOriginalOwner()<0 && !weapon.getSlot().isBuiltIn()){
                MagicInterference.applyInterference(weapon.getShip().getVariant());
            }
        }
        
        if (engine.isPaused()) {
            return;
        }
        
        if (weapon.isFiring() && !TARGETS.isEmpty()) {
            for (Iterator<Map.Entry< ShipAPI , Float >> iter = TARGETS.entrySet().iterator(); iter.hasNext();) {                
                Map.Entry< ShipAPI , Float > entry = iter.next();
                if (entry.getValue()-amount<0){
                    unapplyDebuff(entry.getKey());
                    iter.remove();
                } else {
                    if(entry.getValue()==1){                        
                        applyDebuff(entry.getKey());
                    }
                    TARGETS.put(entry.getKey(), entry.getValue()-amount);
                    if(MagicRender.screenCheck(0.1f, entry.getKey().getLocation())){
                        createVisual(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }
    
    private void createVisual(ShipAPI ship, Float fade){ 
        
        /*
        *
        * @param sprite
        * SpriteAPI to render.
        * 
        * @param loc
        * Vector2f, center in world coordinates.
        * 
        * @param size
        * Vector2f(width, height) in pixels.
        * 
        * @param angle
        * float of the sprite's azimuth. 0 is pointing top.
        * 
        * @param color
        * Color() override, also used for fading.
        * 
        * @param additive
        * boolean for additive blending.
        */
        
        MagicRender.singleframe(
                Global.getSettings().getSprite("fx", "target_BL"),
                MathUtils.getPoint(
                        ship.getLocation(),
                        ship.getCollisionRadius(),
                        225),
                SIZE,
                0,
                new Color (1,1,1,fade),
                false
        );
        MagicRender.singleframe(
                Global.getSettings().getSprite("fx", "target_TL"),
                MathUtils.getPoint(
                        ship.getLocation(),
                        ship.getCollisionRadius(),
                        135),
                SIZE,
                0,
                new Color (1,1,1,fade),
                false
        );
        MagicRender.singleframe(
                Global.getSettings().getSprite("fx", "target_TL"),
                MathUtils.getPoint(
                        ship.getLocation(),
                        ship.getCollisionRadius(),
                        315),
                new Vector2f(-SIZE.x,-SIZE.y),
                0,
                new Color (1,1,1,fade),
                false
        );
        MagicRender.singleframe(
                Global.getSettings().getSprite("fx", "target_TL"),
                MathUtils.getPoint(
                        ship.getLocation(),
                        ship.getCollisionRadius(),
                        45),
                new Vector2f(-SIZE.x,SIZE.y),
                0,
                new Color (1,1,1,fade),
                false
        );
    }
    
    private void applyDebuff(ShipAPI ship){
//        ship.getMutableStats().getEnergyDamageTakenMult().modifyMult(ID, 1.2f);
//        ship.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult(ID, 1.1f);
//        ship.getMutableStats().getFragmentationDamageTakenMult().modifyMult(ID, 1.4f);
//        ship.getMutableStats().getKineticDamageTakenMult().modifyMult(ID, 1.3f);
//        ship.getMutableStats().getWeaponDamageTakenMult().modifyMult(ID, 2f);
//        ship.getMutableStats().getEngineDamageTakenMult().modifyMult(ID, 2f);
        
        //using percentage modifier for proper additive boost
        ship.getMutableStats().getEnergyDamageTakenMult().modifyPercent(ID, 20f);
        ship.getMutableStats().getHighExplosiveDamageTakenMult().modifyPercent(ID, 10f);
        ship.getMutableStats().getFragmentationDamageTakenMult().modifyPercent(ID, 40f);
        ship.getMutableStats().getKineticDamageTakenMult().modifyPercent(ID, 30f);
        ship.getMutableStats().getWeaponDamageTakenMult().modifyPercent(ID, 100f);
        ship.getMutableStats().getEngineDamageTakenMult().modifyPercent(ID, 100f);
    }
    
    private void unapplyDebuff(ShipAPI ship){
        ship.getMutableStats().getEnergyDamageTakenMult().unmodify(ID);
        ship.getMutableStats().getHighExplosiveDamageTakenMult().unmodify(ID);
        ship.getMutableStats().getFragmentationDamageTakenMult().unmodify(ID);
        ship.getMutableStats().getKineticDamageTakenMult().unmodify(ID);
        ship.getMutableStats().getWeaponDamageTakenMult().unmodify(ID);
        ship.getMutableStats().getEngineDamageTakenMult().unmodify(ID);
    }
    
    public void putTARGET(ShipAPI ship) {
        TARGETS.put(ship, 1f);
    }
}
