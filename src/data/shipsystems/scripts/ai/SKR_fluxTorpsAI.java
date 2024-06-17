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

public class SKR_fluxTorpsAI implements ShipSystemAIScript{
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private IntervalUtil timer = new IntervalUtil(0.5f,1.5f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = Global.getCombatEngine();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }    
        
        timer.advance(amount);
        if (timer.intervalElapsed()) {  
            
            if(AIUtils.canUseSystemThisFrame(ship)){
                if(ship.getFluxTracker().getFluxLevel()>0.8f){                    
                    ship.useSystem();  
                } else if(
                        ship.getFluxTracker().getFluxLevel()>0.25f && 
                        ship.getShipTarget()!=null && 
                        ship.getShipTarget() instanceof ShipAPI
                        ){
                    if(((ShipAPI)ship.getShipTarget()).getFluxTracker().getCurrFlux()>0.8f){
                        ship.useSystem();
                    }
                }                    
            }                 
        }
    }
}
