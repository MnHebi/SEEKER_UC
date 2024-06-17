package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
//import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
//import com.fs.starfarer.api.util.Misc;
//import java.util.List;
//import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_burnAI implements ShipSystemAIScript {
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private FluxTrackerAPI flux;
//    private float nominalRange=0;
//    private boolean runOnce = false;
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

//        if(!runOnce){
//            runOnce=true;
//            //calculate the nominal range: the average range of all medium weapons
//            List<WeaponAPI> weapons=ship.getAllWeapons();
//            int i = 0;
//            for (WeaponAPI w : weapons) {
//                 if ( w.getSize() != WeaponAPI.WeaponSize.SMALL ) {
//                    nominalRange = nominalRange + w.getRange();
//                    i++;
//                }        
//            }
//            nominalRange = nominalRange/i;
////            nominalRange*=nominalRange;
//        }
        
        checkAgain.advance(amount);
        
        if (checkAgain.intervalElapsed()) {            
            if(system.isActive()&& flux.getFluxLevel()>0){
                ship.useSystem();
                return;
            }
            if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship) && flux.getFluxLevel()==0){
                ship.useSystem();
            }
//            //force the system activation if retreating
//            if (ship.isRetreating()){
//                if(flux.getFluxLevel()<0.7){
//                    if(!system.isActive()){ship.useSystem();}
//                } else if (flux.getFluxLevel()>0.9){                
//                    if(system.isActive()){ship.useSystem();}
//                }
//                return;
//            }
//                
//            if(!system.isActive()){
//                //SYSTEM IS OFF
//                
//                if(target==null){                    
//                    //activates if nobody is nearby
//                    if(
//                        (Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.DESTROYER, nominalRange, true)==null) 
//                        && 
//                        (flux.getFluxLevel()>0.1 || flux.getFluxLevel()<0.01))
//                    {   
//                        ship.useSystem();
////                        return;
//                    }
//                } else {
//                    //activates if target is out of arc and flux is low enough
//                    if(
//                            flux.getFluxLevel()<0.1
//                            
//                            &&
//                            
//                            (Math.abs(MathUtils.getShortestRotation(
//                                    ship.getFacing(),
//                                    VectorUtils.getAngle(
//                                            ship.getLocation(),
//                                            target.getLocation())
//                            ))>=45)
//                            || 
//                            !MathUtils.isWithinRange(ship, target, nominalRange)){
//                        ship.useSystem();
////                        return;
//                    }
//                }
//            } else {
//                //SYSTEM IS ON
//                
//                if(flux.getFluxLevel()>0.8){
//                    ship.useSystem();
//                    return;                    
//                }
//                
//                if(target==null){
//                    if(Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.DESTROYER, nominalRange*0.9f, true)!=null){                        
//                        ship.useSystem();
////                        return;
//                    }
//                } else {
//                    if(
//                            Math.abs(MathUtils.getShortestRotation(
//                                    ship.getFacing(),
//                                    VectorUtils.getAngle(
//                                            ship.getLocation(),
//                                            target.getLocation())
//                            ))<=45
//                            
//                            &&
//                                
//                            MathUtils.isWithinRange(ship, target, nominalRange*0.9f))
//                    {                    
//                        ship.useSystem();
////                        return;
//                    }
//                }
//            }
        }
    }
}
