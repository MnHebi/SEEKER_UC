//By Tartiflette, missile fabricator script
package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_missileFabEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false;
    private ShipSystemAPI theSystem;    
    private final Map<WeaponAPI, Float> LAUNCHERS = new WeakHashMap<>();   
    private float systemExpertise=1;
//    private boolean reloaded=false;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (!runOnce){
            LAUNCHERS.clear();
            runOnce=true;
            theSystem = weapon.getShip().getSystem();
            //List all reloadable weapons
            for (WeaponAPI w : weapon.getShip().getAllWeapons()){
                if (w.usesAmmo() && w.getType() == WeaponAPI.WeaponType.MISSILE){
                    LAUNCHERS.put(w, 0f);
                }
            }
            systemExpertise=weapon.getShip().getMutableStats().getSystemRangeBonus().getBonusMult();
            
        }                

        if (engine.isPaused() || LAUNCHERS.isEmpty()) {
            return;
        }

        if (theSystem.isActive()){
//            reloaded=true;
            for (Iterator<Map.Entry< WeaponAPI , Float >> iter = LAUNCHERS.entrySet().iterator(); iter.hasNext();) {     
                //dig through the list of weapons
                Map.Entry< WeaponAPI , Float > entry = iter.next(); 

                if (entry.getKey().getAmmo()==entry.getKey().getMaxAmmo()){
                    //reset the loader if the weapon is already fully loaded
                    LAUNCHERS.put(entry.getKey(), 0f);
                } else {
                    //lock the weapon at full cooldown to prevent player spamming
                    entry.getKey().setRemainingCooldownTo(30f);
                    //build 3% of the total ammo per second, plus system expertise bonus
                    float build = entry.getValue()+(systemExpertise*amount*entry.getKey().getMaxAmmo()/33);                    
                    if (build>=1){
                        //add one ammo if it's built
                        LAUNCHERS.put(entry.getKey(), build-1);
                        entry.getKey().setAmmo(entry.getKey().getAmmo()+1);
                    } else {
                        //store the progression otherwise
                        LAUNCHERS.put(entry.getKey(), build);
                    }
                    if(MagicRender.screenCheck(0.5f, weapon.getLocation())){
                        //add some smoke to visualise the effect
                        float grey = MathUtils.getRandomNumberInRange(0.1f, 0.3f);
                        float life = MathUtils.getRandomNumberInRange(1f, 5f);
                        engine.addSmokeParticle(
                                MathUtils.getRandomPointInCircle(
                                        entry.getKey().getLocation(),
                                        5
                                ), 
                                new Vector2f (
                                        weapon.getShip().getVelocity().x/2+MathUtils.getRandomNumberInRange(-5f, 5f),
                                        weapon.getShip().getVelocity().y/2+MathUtils.getRandomNumberInRange(-5f, 5f)
                                ),  
                                MathUtils.getRandomNumberInRange(10, 25), 
                                0.5f,
                                life, 
                                new Color(grey,grey,grey,MathUtils.getRandomNumberInRange(0.05f, 0.2f) )
                        );
                    }
                }
            }
        }
    }
}
