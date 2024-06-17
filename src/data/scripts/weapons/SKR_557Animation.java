//By Tartiflette
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class SKR_557Animation implements EveryFrameWeaponEffectPlugin{
           
    private boolean runOnce=false;
    private boolean hidden=false;
    private AnimationAPI theAnim;
    private int frame, lastAmmo;
    
    @Override
    public void advance (float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            if(weapon.getSlot().isHidden()){
                hidden=true;
            } else {
                theAnim=weapon.getAnimation();
            }
            
            //get extra ammo from exanded mags
            if(weapon.getShip().getVariant().getHullMods().contains("magazines")){
                weapon.ensureClonedSpec();
                weapon.setMaxAmmo(35);
                weapon.setAmmo(35);
            }
            
            lastAmmo=weapon.getAmmo();
            return;
        }
        
        if(weapon.getAmmo()>lastAmmo){
            Global.getSoundPlayer().playSound("SKR_557_reload", 1, 1, weapon.getLocation(), weapon.getShip().getVelocity());
        }
        lastAmmo=weapon.getAmmo();
        
        if(engine.isPaused()||hidden){return;}
        
        if(weapon.getChargeLevel()!=0){
            frame=Math.round(weapon.getChargeLevel()*4);
            if(frame==4)frame=0;
            theAnim.setFrame(frame);
        } else if(frame!=0){
            frame=0;
            theAnim.setFrame(0);
        }
        
        
    }
}
