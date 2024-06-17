//By Tartiflette: this low impact script hide a deco weapon when the ship is destroyed

package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import java.util.HashMap;
import java.util.Map;

public class SKR_dissipatorEffect implements EveryFrameWeaponEffectPlugin{
    
    private boolean runOnce=false;
    private final String ID="SKR_capacitor";
    private final Map<WeaponSize, Integer> effect = new HashMap<>();
    {
        effect.put(WeaponSize.SMALL, 100);
        effect.put(WeaponSize.MEDIUM, 200);
        effect.put(WeaponSize.LARGE, 400);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if (engine.isPaused()) {return;}
        
        if (!runOnce){
            runOnce=true;
            weapon.getShip().getMutableStats().getFluxDissipation().modifyFlat(ID, effect.get(weapon.getSize()));
        }
    }
}