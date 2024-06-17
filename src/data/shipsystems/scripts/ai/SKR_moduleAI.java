
package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_moduleAI implements ShipSystemAIScript{
    
    private ShipAPI ship, parent;
    private CombatEngineAPI engine;
    private ShipSystemAPI system, parentSystem;
    private FluxTrackerAPI flux;

    private boolean runOnce=false, venting=false;
    private IntervalUtil timer = new IntervalUtil(0.1f, 0.3f);
    
    private final String ID = "SlaveModuleVent";

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
        this.system = system;
        this.flux = ship.getFluxTracker();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        
        if(engine.isPaused() || !ship.isAlive()) return;
        
        if(!runOnce){
            runOnce=true;
            
            ship.getMutableStats().getVentRateMult().modifyMult(ID, 0);
            if(ship.getParentStation()!=null){
                parent=ship.getParentStation();
                parentSystem=parent.getSystem();
            }
        }
        
        if(parent==null || !parent.isAlive())return;
        
        timer.advance(amount);
        if(timer.intervalElapsed()){         
            //overloaded or holdFire
            if(parent.getFluxTracker().isOverloaded() || parent.isHoldFire()){
                
                if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship))ship.useSystem();
                
            } else if(parentSystem.isActive()){
                
                if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship))ship.useSystem();
                
            } else if(parent.getFluxTracker().isVenting()){
                //venting
                if(flux.getFluxLevel()>0.05){
                    ship.getMutableStats().getVentRateMult().modifyMult(ID, 1);
                    ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
                    venting=true;
                } else {   
                    //cancel vent
                    if(venting && !flux.isVenting()){
                        venting=false;                      
                        ship.getMutableStats().getVentRateMult().modifyMult(ID, 0);
                    }
                    if(!system.isActive() && AIUtils.canUseSystemThisFrame(ship))ship.useSystem();
                    
                }
            } else {
                if(system.isActive())ship.useSystem(); 
//                if(venting){
//                    venting=false;
//                    flux.stopVenting();
//                    ship.getMutableStats().getVentRateMult().modifyMult(ID, 0);
//                }
            }
        } else if(venting && !flux.isVenting()){
            //instant check for venting
            venting=false;
            ship.getMutableStats().getVentRateMult().modifyMult(ID, 0);
            if(parent.getFluxTracker().isOverloadedOrVenting() || parent.isHoldFire()){
                if(AIUtils.canUseSystemThisFrame(ship))ship.useSystem(); 
            }
        }
    }
}