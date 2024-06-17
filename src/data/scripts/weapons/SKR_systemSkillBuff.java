package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class SKR_systemSkillBuff implements EveryFrameWeaponEffectPlugin {    
    
    private boolean runOnce=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        if(!runOnce){
            runOnce=true;
            //quick ammo buff for systems that have charges but no regen
            int SYSTEM_AMMO = weapon.getShip().getSystem().getAmmo();
            float ammo = weapon.getShip().getMutableStats().getSystemRangeBonus().getBonusMult();
            if(ammo!=1){
                SYSTEM_AMMO = Math.round(SYSTEM_AMMO*ammo);
                weapon.getShip().getSystem().setAmmo(SYSTEM_AMMO);
            }
        }
    }    
}