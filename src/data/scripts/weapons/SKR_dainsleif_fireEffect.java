//by Tartiflette,
//feel free to use it, credit is appreciated but not mandatory
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_plagueEffect;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class SKR_dainsleif_fireEffect implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=false;
            if(weapon.getShip().getOriginalOwner()<1 && !weapon.getSlot().isBuiltIn()){
                SKR_plagueEffect.ApplyPlague(weapon.getShip().getVariant());
            }
        }
        
        if(weapon.getChargeLevel()==1){
            if(MagicRender.screenCheck(1, weapon.getLocation())){
                MagicRender.battlespace(
                        Global.getSettings().getSprite("fx", "SKR_shockwave"),
                        weapon.getLocation(),
                        weapon.getShip().getVelocity(),
                        new Vector2f(48,48),
                        new Vector2f(720,720),
                        weapon.getCurrAngle()+90, 
                        0, 
                        new Color(200,128,150,200),
                        true,
                        0,
                        0.05f,
                        0.1f
                );
            }
        }
    }
}