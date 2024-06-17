package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
//import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_augurSystem implements EveryFrameWeaponEffectPlugin {
    
    private ShipAPI ship;
    private ShipSystemAPI system;
    private boolean runOnce=false,active=false;
    private IntervalUtil zap = new IntervalUtil(0.05f,0.3f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {return;}
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();
            
        }
            
        if(system.isActive()){
            
            float level = system.getEffectLevel();
            
            active=true;
            
            //glow
            weapon.getAnimation().setFrame(1);            
            weapon.getSprite().setColor(
                    new Color(
                            Math.min(1,Math.max(0, level+0.25f)),
                            level,
                            Math.min(1,Math.max(0, level+0.5f)),
                            Math.min(1,Math.max(0, level*2))
                )
            );
            
            //sparks
            zap.advance(amount);
            if(zap.intervalElapsed()){
                zap.setInterval(0.05f, 0.3f-0.1f*level);
//                
//                MagicRender.battlespace(
//                        Global.getSettings().getSprite("fx", "zap_0"+MathUtils.getRandomNumberInRange(0, 7)),
//                        MathUtils.getRandomPointInCircle(weapon.getLocation(), 20+15*level),
//                        MathUtils.getRandomPointInCircle(new Vector2f(), 10),
//                        new Vector2f(
//                                MathUtils.getRandomNumberInRange(6+20*level, 12+20*level),
//                                MathUtils.getRandomNumberInRange(6+20*level, 12+20*level)
//                        ),
//                        new Vector2f(
//                                MathUtils.getRandomNumberInRange(-5, 5),
//                                MathUtils.getRandomNumberInRange(-5, 5)
//                        ),
//                        MathUtils.getRandomNumberInRange(-180,180),
//                        MathUtils.getRandomNumberInRange(-45,45), 
//                        new Color(255,175,255,255),
//                        true,
//                        0, 
//                        MathUtils.getRandomNumberInRange(0.025f,0.1f),
//                        MathUtils.getRandomNumberInRange(0.05f,0.1f)
//                );

                if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "zap_0"+MathUtils.getRandomNumberInRange(0, 7)),
                            MathUtils.getRandomPointInCircle(weapon.getLocation(), 25+25*level),
                            MathUtils.getRandomPointInCircle(new Vector2f(), 50),
                            new Vector2f(
                                    MathUtils.getRandomNumberInRange(6+20*level, 12+20*level),
                                    MathUtils.getRandomNumberInRange(6+20*level, 12+20*level)
                            ),
                            new Vector2f(
                                    MathUtils.getRandomNumberInRange(-5, 5),
                                    MathUtils.getRandomNumberInRange(-5, 5)
                            ),
                            MathUtils.getRandomNumberInRange(-180,180),
                            MathUtils.getRandomNumberInRange(-45,45), 
                            new Color(255,175,255,255),
                            true,

                            2, //jitter range
                            5, //jitter tilt
                            2f, // flicker range
                            0f, //flicker median
                            0.1f, //max delay

                            0, 
                            MathUtils.getRandomNumberInRange(0.25f,0.5f),
                            MathUtils.getRandomNumberInRange(0.25f,0.5f),

                            CombatEngineLayers.ABOVE_SHIPS_LAYER);
                }
            }
            
        } else if(active){
            active=false;
            weapon.getAnimation().setFrame(0);
            zap.setInterval(0.05f, 0.3f);
        } else if(system.getCooldownRemaining()<=0){
            if(ship.getAI()!=null){
                //force AI targeting due to 0.95 weirdness
                zap.advance(amount);
                if(zap.intervalElapsed()){
                    if(ship.isAlive() && ship.getShipTarget()==null && !ship.getFluxTracker().isOverloadedOrVenting()){
                        ShipAPI target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, 800, false);
                        if(target!=null && target.isAlive()){
                            ship.setShipTarget(target);
                        }
                    }
                }
            }
        }
    }
}
