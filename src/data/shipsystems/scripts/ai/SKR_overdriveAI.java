package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.weapons.SKR_balisongEffect;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_overdriveAI implements ShipSystemAIScript {
    
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    
    private WeaponAPI left, right;
    private final String leftSlot = "LEFT", rightSlot = "RIGHT", systemSlot = "SYSTEM";
    private EveryFrameWeaponEffectPlugin effect;
    
    private boolean runOnce = false;
    private IntervalUtil timer = new IntervalUtil(1.5f,3f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        this.system = system;
        this.engine = Global.getCombatEngine();
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){

        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }

        if(!runOnce){
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case leftSlot:
                        left=w;
                        break;
                    case rightSlot:
                        right=w;
                        break;
                    case systemSlot:
                        if(w.getEffectPlugin()==null)return;
                        effect = w.getEffectPlugin();
                        break;
                    default:
                        break;
                }
            }
            runOnce=true;
        }
        
        timer.advance(amount);
        if (timer.intervalElapsed()) {  
                  
            float overcharge = ((SKR_balisongEffect)effect).getOvercharge();
            
//            //DEBUG
//            engine.addFloatingText(ship.getLocation(), ""+overcharge, 30, Color.yellow, ship, 1, 1);
            
            //evaluate need            
            float need = 0;
            
            if(left.isDisabled() || left.getAmmo()==0) need++;
            if(right.isDisabled() || right.getAmmo()==0) need++;
            need/=2;
            
            //evaluate threat
            
            float dangerFactor=0;

            for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, 1000f)) {
                switch (enemy.getHullSize()) {
                    case CAPITAL_SHIP:
                        dangerFactor+=1;
                        break;
                    case CRUISER:
                        dangerFactor+=0.5f;
                        break;
                    case DESTROYER:
                        dangerFactor+=0.4f;
                        break;
                    case FRIGATE:
                        dangerFactor+=0.3f;
                        break;
                    default:
                        dangerFactor+=0.5f;
                        break;
                }                
            }
            
            float flux = ship.getFluxTracker().getFluxLevel();
            
            if (!system.isActive()){
                if (overcharge==0 && flux <= 0.5f && (need == 1f || (need >= 0.5f && dangerFactor <=0.5f) || dangerFactor == 0f)){
                    ship.useSystem();
                    timer.setElapsed(-3);
                }
            } else if (flux>=0.8f || (overcharge==10 && dangerFactor>0) || dangerFactor >= (2-overcharge/10)){
                    ship.useSystem();
                    timer.setElapsed(-3);
            }                     
        }
    }
}
