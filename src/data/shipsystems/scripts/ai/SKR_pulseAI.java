package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_pulseAI implements ShipSystemAIScript{
    private ShipAPI ship;
    private final IntervalUtil timer = new IntervalUtil(0.4f,0.6f);
    private final float TRIGGER=4;
    
    private final Map<HullSize,Integer> WEIGHT = new HashMap<>();
    {
        WEIGHT.put(HullSize.CAPITAL_SHIP, 5);
        WEIGHT.put(HullSize.CRUISER, 4);
        WEIGHT.put(HullSize.DESTROYER, 3);
        WEIGHT.put(HullSize.FRIGATE, 2);
        WEIGHT.put(HullSize.FIGHTER, 1);
        WEIGHT.put(HullSize.DEFAULT, 2);
    }
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        if (Global.getCombatEngine().isPaused() || ship.getShipAI()==null) {
            return;
        }
        
        timer.advance(amount);
        if(timer.intervalElapsed()){
            if(AIUtils.canUseSystemThisFrame(ship)&&ship.getFluxTracker().getFluxLevel()<0.8f){
                int count=0;
                
                for(MissileAPI m : AIUtils.getNearbyEnemyMissiles(ship, 700)){
                    if(m.getDamageAmount()>350){
                        count++;
                    }
                }
                
                for(ShipAPI s : AIUtils.getNearbyEnemies(ship, 800)){
                    count+=WEIGHT.get(s.getHullSize());
                }
                
                for(ShipAPI s : AIUtils.getNearbyAllies(ship, 600)){
                    count-=WEIGHT.get(s.getHullSize());
                }
                
                if(count>=TRIGGER){
                    ship.useSystem();
                }
            }
        }
    }
}
