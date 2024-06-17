package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.scripts.util.SKR_plagueEffect;

public class SKR_smoothRecoilEffect implements EveryFrameWeaponEffectPlugin {
        
    private boolean runOnce=false, hidden=false;
    private SpriteAPI barrel;
    private float barrelHeight=0, recoil=0, ammo=0;
    private final float maxRecoil=-8;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getShip().getOriginalOwner()<1 && !weapon.getSlot().isBuiltIn()){
                SKR_plagueEffect.ApplyPlague(weapon.getShip().getVariant());
            }
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                barrel=weapon.getBarrelSpriteAPI();
                if(weapon.getSlot().isTurret()){
                    barrelHeight=barrel.getHeight()/2;
                } else {                    
                    barrelHeight=barrel.getHeight()/4;
                }
            }
            return;
        }
        
        if(engine.isPaused() || weapon.getShip().getOriginalOwner()==-1){return;}
        
        if(!hidden){
            if(weapon.getChargeLevel()==1 && weapon.getAmmo()<ammo){
                recoil=Math.min(1, recoil+0.2f);
            } else {
                recoil=Math.max(0, recoil-(0.33f*amount));
            }
            barrel.setCenterY(barrelHeight-(recoil*maxRecoil));
            ammo=weapon.getAmmo();            
        }
    }
}