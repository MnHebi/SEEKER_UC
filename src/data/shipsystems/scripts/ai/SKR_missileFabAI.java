package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_missileFabAI implements ShipSystemAIScript
{
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    
    private final List<WeaponAPI> WEAPONS = new ArrayList<>();  
    private boolean runOnce = false;
    private float delay=0;
    private final IntervalUtil checkAgain = new IntervalUtil (1f+delay,1.5f+delay);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine)
    {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
    {        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }        

        if(!runOnce){
            WEAPONS.clear();
            runOnce=true;
            for (WeaponAPI w : ship.getAllWeapons()) {
                //list all missiles weapons
                if (w.getSize() == WeaponAPI.WeaponSize.MEDIUM){
                    WEAPONS.add(w);
                }        
            }
        }
        
        checkAgain.advance(amount);
        
        if (checkAgain.intervalElapsed() && target!=null) {
            
            //evaluate need
            
            float need = 0;
            float i = 0;
            for (WeaponAPI w : WEAPONS){
                
                float ammo = w.getAmmo();
                float maxAmmo = w.getMaxAmmo();
                
                float ratio = ammo/maxAmmo;
                
                i++;
                
                need += (ratio-1)*(ratio-1);
//                need += (float)(( FastTrig.cos(( ratio + 2 )*MathUtils.FPI)/4) + 1 );
                
            }
            need = need/i;
            
            //evaluate threat
            
            float dangerFactor=0;

            for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, 2000f)) {
                if (enemy.getHullSize()==ShipAPI.HullSize.CAPITAL_SHIP){
                    dangerFactor+= Math.max(0,2-(MathUtils.getDistanceSquared(enemy, ship)/1400000));
                } else if (enemy.getHullSize()==ShipAPI.HullSize.CRUISER){
                    dangerFactor+= Math.max(0,2-(MathUtils.getDistanceSquared(enemy, ship)/1000000))/2;
                } else if (enemy.getHullSize()==ShipAPI.HullSize.DESTROYER){
                    dangerFactor+= Math.max(0,2-(MathUtils.getDistanceSquared(enemy, ship)/800000))/4;
                } else if (enemy.getHullSize()==ShipAPI.HullSize.FRIGATE){
                    dangerFactor+= Math.max(0,2-(MathUtils.getDistanceSquared(enemy, ship)/400000))/6;
                } else {
                    dangerFactor+= Math.max(0,2-(MathUtils.getDistanceSquared(enemy, ship)/200000))/8;
                }                
            }
            
            if (!system.isActive()){
                if (ship.getFluxTracker().getFluxLevel() <= 0.75f && (need == 1f || (need >= 0.5f && 2f*need >= dangerFactor) || (need >= 0.25f && dangerFactor == 0f))){
                    ship.useSystem();
                }
            } else {
                 if (ship.getFluxTracker().getCurrFlux()>=0.9f || need == 0f || 3f*need <= dangerFactor || (dangerFactor >= 3f && need <= 0.9f)){
                    ship.useSystem();
                }
            }         
        }
    }
}
