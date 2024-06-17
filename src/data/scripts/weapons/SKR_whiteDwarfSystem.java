package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import org.lwjgl.util.vector.Vector2f;

public class SKR_whiteDwarfSystem implements EveryFrameWeaponEffectPlugin {
    
    private ShipAPI ship,moduleDeckL,moduleDeckR,moduleGunL,moduleGunR;
    private WeaponAPI gunL, gunR;
    private ShipSystemAPI system;
    private boolean bonus = false, runOnce=false,A=true,B=true,C=true,D=true,SHADER=false,active=false, arrival=false;
    private final IntervalUtil grumpy = new IntervalUtil(10,20);
    private final Map<ShipAPI.HullSize, Float> MULT = new HashMap<>();
    {
        MULT.put(ShipAPI.HullSize.DEFAULT, 1f);
        MULT.put(ShipAPI.HullSize.FIGHTER, 0.75f);
        MULT.put(ShipAPI.HullSize.FRIGATE, 0.5f);
        MULT.put(ShipAPI.HullSize.DESTROYER, 0.3f);
        MULT.put(ShipAPI.HullSize.CRUISER, 0.2f);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 0.1f);
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
                arrival=false;
            }
        
        if (engine.isPaused()) {return;}
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();
            SHADER=Global.getSettings().getModManager().isModEnabled("shaderLib");
            
            if(!ship.getChildModulesCopy().isEmpty()){
                for (ShipAPI m : ship.getChildModulesCopy()) {
                    switch(m.getStationSlot().getId()) {
                        case "DECK_L":
                            moduleDeckL = m;
                            break;
                        case "DECK_R":
                            moduleDeckR = m;
                            break;
                        case "GUN_L":
                            moduleGunL = m;
                            break;
                        case "GUN_R":
                            moduleGunR = m;
                            break;
                    }
                }
            }       
            
            for(WeaponAPI w : ship.getAllWeapons()){
                switch(w.getSlot().getId()){
                    case "WEAPON_L":
                        gunL=w;
                        break;
                    case "WEAPON_R":
                        gunR=w;
                        break;
                }
            }
            
            if(gunL==null){
                gunL.disable(true);
            }
            if(gunR==null){
                gunR.disable(true);
            }
            
            return;
        }
            
        if(A && moduleDeckL!=null && moduleDeckL.isAlive()){
            if(system.isActive()){
                applySystemEffect(moduleDeckL, system.getEffectLevel());
                applySystemVisual(moduleDeckL, system.getEffectLevel());
                if(moduleDeckL.getShield()!=null){
                    moduleDeckL.getShield().toggleOff();
                }
            } else if (bonus){
                unapplySystemEffect(moduleDeckL);
            }
        } else if(A){
            A=false;
            applyWeightLoss("moduleA");
        }
        
        if(B && moduleDeckR!=null && moduleDeckR.isAlive()){
            if(system.isActive()){
                applySystemEffect(moduleDeckR, system.getEffectLevel());
                applySystemVisual(moduleDeckR, system.getEffectLevel());
                if(moduleDeckR.getShield()!=null){
                    moduleDeckR.getShield().toggleOff();
                }
            } else if (bonus){
                unapplySystemEffect(moduleDeckR);
            }
        } else if(B){
            B=false;
            applyWeightLoss("moduleB");
        }
        
        
        if(C && moduleGunL!=null && moduleGunL.isAlive()){
            if(system.isActive()){
                applySystemEffect(moduleGunL, system.getEffectLevel());
                applySystemVisual(moduleGunL, system.getEffectLevel());
                if(moduleGunL.getShield()!=null){
                    moduleGunL.getShield().toggleOff();
                }
            } else if (bonus){
                unapplySystemEffect(moduleGunL);
            }
        } else if(C){
            C=false;
            gunL.disable(true);
            applyWeightLoss("moduleC");
        }
        
        if(D && moduleGunR!=null && moduleGunR.isAlive()){
            if(system.isActive()){
                applySystemEffect(moduleGunR, system.getEffectLevel());
                applySystemVisual(moduleGunR, system.getEffectLevel());
                if(moduleGunR.getShield()!=null){
                    moduleGunR.getShield().toggleOff();
                }
            } else if (bonus){
                unapplySystemEffect(moduleGunR);
            }
        } else if(D){
            D=false;
            gunR.disable(true);
            applyWeightLoss("moduleD");
        }
        
        bonus=system.isActive();
        if(bonus){
            applySystemVisual(ship, system.getEffectLevel());
        }
        
        if(system.isActive()){
            float level = system.getEffectLevel();
            active=true;
            weapon.getAnimation().setFrame(1);            
            weapon.getSprite().setColor(
                    new Color(
                            Math.min(1,Math.max(0, level+0.5f)),
                            level,
                            level,
                            Math.min(1,Math.max(0, level*2))
                )
            );
        } else if(active){
            active=false;
            weapon.getAnimation().setFrame(0);
        }
            
        /*
        //warp in effect
        if(!arrival 
//                && weapon.getShip().getOriginalOwner()>0
                ){
            arrival = true;
            grumpy.forceIntervalElapsed();
            //arrivalEffect(weapon.getShip());
        }
//        else {
//            //debug
//            if(Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)){
//                arrival=false;
//            }
//        }
        */
        
        
        //ambience sounds
        grumpy.advance(amount);
        if(grumpy.intervalElapsed()){
            Global.getSoundPlayer().playSound("SKR_rampage_grumpy", MathUtils.getRandomNumberInRange(0.95f, 1.05f), MathUtils.getRandomNumberInRange(0.5f, 0.75f), weapon.getShip().getLocation(), weapon.getShip().getVelocity());
        }
        
        
//        //debug
//        if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
//            bursting = 1;
//            //range equals BASE_RANGE + up to BASE_RANGE again depending on hull level
//            burstRange = 2*BASE_BURST_RANGE - BASE_BURST_RANGE*burstTrigger;
//            //glowy
//            weapon.getAnimation().setFrame(1);
//        }
        
    }
    
    private void applyWeightLoss(String id){
        ship.getMutableStats().getAcceleration().modifyPercent(id, 25);
        ship.getMutableStats().getDeceleration().modifyPercent(id, 25);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id, 25);
        ship.getMutableStats().getMaxTurnRate().modifyFlat(id, 3);        
    }
    
    private void applySystemEffect(ShipAPI module, float level){        
        module.getMutableStats().getArmorDamageTakenMult().modifyMult(ship.getId(), 1-0.9f*level);
        module.getMutableStats().getHullDamageTakenMult().modifyMult(ship.getId(), 1-0.9f*level);
    }
    
    private void unapplySystemEffect(ShipAPI module){
        module.getMutableStats().getArmorDamageTakenMult().unmodify(ship.getId());
        module.getMutableStats().getHullDamageTakenMult().unmodify(ship.getId());
    }
    
    private void applySystemVisual(ShipAPI theShip, float level){
        
        if(MagicRender.screenCheck(1, ship.getLocation())){
            if(level<1){
                theShip.setJitterUnder(
                        theShip,
                        new Color(0, 0.25f*level,0.25f+0.25f*level),
                        level,
                        3,
                        5+5*level
                );
                theShip.setJitter(
                        theShip,
                        new Color(0, 0.1f*level,0.1f+0.1f*level),
                        level/2,
                        2,
                        3+3*level
                );
            } else {
                theShip.setJitterUnder(
                        theShip,
                        new Color(0, 0.25f*level,0.25f+0.25f*level),
                        1,
                        4,
                        15
                );
                theShip.setJitter(
                        theShip,
                        new Color(0, 0.1f*level,0.1f+0.1f*level),
                        0.5f,
                        3,
                        10
                );
            }
        }
    }
    /*
    private void arrivalEffect(ShipAPI ship){
         //sound
//        Global.getSoundPlayer().playSound("SKR_whiteDwarf_arrival", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1, ship.getLocation(), ship.getVelocity());
        Global.getSoundPlayer().playUISound("SKR_whiteDwarf_arrival", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1);

        Vector2f relocation = ship.getLocation();
        relocation.scale(0.5f);

        //ripple
        if(SHADER){
            SKR_graphicLibEffects.CustomRippleDistortion(
                    ship.getLocation(), 
                    new Vector2f(ship.getVelocity()),
                    720,
                    30, 
                    false,
                    0,
                    360,
                    0,
                    0.1f,
                    0.6f,
                    0.3f, 
                    0.5f, 
                    0f
            );
        }

        //rays
        for(int i=0; i<24; i++){
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "sweetener"
                    ),
                    MathUtils.getRandomPointInCone(
                            ship.getLocation(),
                            720,
                            ship.getFacing()+140,
                            ship.getFacing()+220
                    ),
                    MathUtils.getPoint(
                            new Vector2f(),
                            MathUtils.getRandomNumberInRange(256, 360),
                            ship.getFacing()
                    ),
                    new Vector2f(
                            MathUtils.getRandomNumberInRange(16, 32),
                            MathUtils.getRandomNumberInRange(512, 1024)
                    ),
                    new Vector2f(
                            0,
                            MathUtils.getRandomNumberInRange(-400, -500)
                    ),
                    ship.getFacing()-90,
                    0,
                    Color.WHITE,
                    true,
                    0,
                    0.1f,
                    MathUtils.getRandomNumberInRange(
                            0.5f,
                            3
                    )
            );
        }
        //swooshes
        for(int i=0; i<12; i++){
            float size = MathUtils.getRandomNumberInRange(512, 1024);
            float growth = MathUtils.getRandomNumberInRange(256, 512);
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "SKR_drill_swoosh"
                    ),
                    MathUtils.getRandomPointInCircle(
                            ship.getLocation(),
                            512-size/2
                    ),
                    MathUtils.getRandomPointInCone(
                            new Vector2f(),
                            64,
                            ship.getFacing()+150,
                            ship.getFacing()+210
                    ),
//                    new Vector2f(),
                    new Vector2f(size,size),
                    new Vector2f(growth,growth),
//                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(
                            ship.getFacing()-60,
                            ship.getFacing()-120
                    ),
                    MathUtils.getRandomNumberInRange(-15,15),
//                    0,
                    Color.WHITE,
                    true,
                    0,
                    0.2f,
                    MathUtils.getRandomNumberInRange(
                            0.5f,
                            1.5f
                    )
            );
        }
        
        //negative cloud
        MagicRender.battlespace(
                Global.getSettings().getSprite(
                        "fx",
                        "skr_can_glow"
                ),
                ship.getLocation(),
                new Vector2f(ship.getVelocity()),
                new Vector2f(1024,1027),
                new Vector2f(512,512),
                (float)Math.random()*360,
                MathUtils.getRandomNumberInRange(-5, 5),
                new Color(1f,1f,1f,1f),
                true,
                0,0,0,0,0,
                0,
                0.1f,
                0.2f,
                CombatEngineLayers.ABOVE_SHIPS_LAYER
        );
        MagicRender.battlespace(
                Global.getSettings().getSprite(
                        "fx",
                        "skr_can_glow"
                ),
                ship.getLocation(),
                new Vector2f(ship.getVelocity()),
                new Vector2f(720,720),
                new Vector2f(512,512),
                (float)Math.random()*360,
                MathUtils.getRandomNumberInRange(-5, 5),
                new Color(1f,1f,1f,1f),
                0,0,0,0,0,
                0,
                0.1f,
                0.1f,
                CombatEngineLayers.ABOVE_SHIPS_LAYER,
                GL_ONE_MINUS_SRC_ALPHA,
                GL_ONE_MINUS_SRC_COLOR
        );
        
        //trail
        for(int i=0; i<30; i++){
            
            Vector2f point = MathUtils.getPoint(new Vector2f(),50*(i+1),ship.getFacing()+180);
            Vector2f vel = MathUtils.getPoint(new Vector2f(),25*(i+1),ship.getFacing());
            
            //modules
            for(ShipAPI s : ship.getChildModulesCopy()){
                if(s.isAlive()){
                    s.addAfterimage(
                    new Color(0.5f,0.25f,1f,0.15f),
                    point.x,
                    point.y,
                    vel.x,
                    vel.y,
                    0.1f,
                    0,
                    0.1f,
                    2.5f-(0.1f*i),
                    false, 
                    true,
                    false
                    );
                }
            }
            //ship
            ship.addAfterimage(
                    new Color(0.5f,0.25f,1f,0.15f),
                    point.x,
                    point.y,
                    vel.x,
                    vel.y,
                    0.1f,
                    0,
                    0.1f,
                    2.5f-(0.1f*i),
                    false, 
                    true,
                    false
            );
        }
    }
*/
}
