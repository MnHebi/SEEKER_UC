package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_ramAI implements ShipSystemAIScript {
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private FluxTrackerAPI flux;
    private final float RAM_RANGE=1000;
    private final IntervalUtil checkAgain = new IntervalUtil (1f,3f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        
        this.ship = ship;
        this.system = system;
        this.flux = ship.getFluxTracker();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){  
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }   
        
        if (flux.isOverloadedOrVenting())return;
        
        checkAgain.advance(amount);
        
        if (checkAgain.intervalElapsed()) {
            //ignore if the system is active
            if(system.isActive())return;
            //ignore if the flux is high to avoid suicide burns
            if(flux.getFluxLevel()>0.66f)return;
            
            //force the system activation if retreating
            if (ship.isRetreating() && AIUtils.canUseSystemThisFrame(ship)){
                ship.useSystem();                
                return;
            }
            
//            if(AIUtils.canUseSystemThisFrame(ship) && flux.getFluxLevel()==0){
//                ship.useSystem();
//            }
                
            if(target==null){                    
                //activates if nobody is nearby
                if(Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.DESTROYER, RAM_RANGE, true)!=null) {
                    ship.useSystem();
                }
            } else 
                //activates if target is within arc and in dash range
                if(
                    (Math.abs(MathUtils.getShortestRotation(
                            ship.getFacing(),
                            VectorUtils.getAngle(
                                    ship.getLocation(),
                                    target.getLocation())
                    ))<=90)
                    &&
                        MathUtils.isWithinRange(ship, target, RAM_RANGE)
                    && 
                        AIUtils.canUseSystemThisFrame(ship)
                    ){
                ship.useSystem();
            }            
        }
    }
}
