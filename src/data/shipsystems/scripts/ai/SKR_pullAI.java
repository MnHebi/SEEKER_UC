package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_pullAI implements ShipSystemAIScript {
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private final IntervalUtil checkAgain = new IntervalUtil (1f,3f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){        
        this.ship = ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){  
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }   

        checkAgain.advance(amount);
        if(checkAgain.intervalElapsed()){
            if(ship.getShipTarget()!=null && AIUtils.canUseSystemThisFrame(ship)){
                ship.useSystem();
            }
        }
    }
}
