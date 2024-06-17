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

public class SKR_fastShieldAI implements ShipSystemAIScript {
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private float maxArc;
    private boolean runOnce = false, noNeed=false;
    private final IntervalUtil checkAgain = new IntervalUtil (1f,1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = Global.getCombatEngine();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {        

        if (engine.isPaused() || ship.getShipAI()==null || noNeed) {
            return;
        }        

        if(!runOnce){
            runOnce=true;
            if (ship.getShield()!=null){
                maxArc = ship.getShield().getArc();
            } else {
                noNeed=true;
                return;
            }
        }
        
        checkAgain.advance(amount);
        
        if (checkAgain.intervalElapsed()) {            
            if (!system.isActive() && ship.getShield().isOn() && ship.getShield().getActiveArc() < maxArc/2 && AIUtils.canUseSystemThisFrame(ship)){
                    ship.useSystem();                
            }         
        }
    }
}
