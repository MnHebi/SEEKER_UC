package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

//By Tartiflette

public class SKR_marksmanEffect implements EveryFrameWeaponEffectPlugin {
    
    private final String ID="Reloading Ammo"; 
    private final String RELOAD = txt("reload");
    private final float SUPER_DURATION=4;
    private final float SUPER_BOOST=200;
    private final EnumSet WEAPONS = EnumSet.of(WeaponAPI.WeaponType.ENERGY);
    private final Vector2f cursorUiSize = new Vector2f(48,48);
    private final Map<String,Float> AGGRESSIVENESS = new HashMap<>();
    
    {
        AGGRESSIVENESS.put("timid", 0f);
        AGGRESSIVENESS.put("cautious", 0.25f);
        AGGRESSIVENESS.put("steady", 0.5f);
        AGGRESSIVENESS.put("aggressive", 0.75f);
        AGGRESSIVENESS.put("reckless", 1f);
    }
    
    private float charge=0, superWindow=-1, superShot=0;
    private int AIuse=0;
    private boolean runOnce = false, playerShip=false;
    private ShipAPI ship;
    private ShipSystemAPI system;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) {return;}
        
        if (!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            system=ship.getSystem();
            
            cursorUiSize.scale(Global.getSettings().getScreenScaleMult());
        }
        
        if(system.isActive()){
                        
            charge = system.getEffectLevel();
            
            //compute supershot window
            if(superWindow<=0){
                float crWindow = ship.getCurrentCR();
                
//                float captainWindow;
//                if (ship == engine.getPlayerShip() && ship.getAI()==null){
//                    captainWindow = Global.getSector().getPlayerPerson().getStats().getLevel()/50;
//                } else if (ship.getCaptain()!=null) {
//                    captainWindow = ship.getCaptain().getStats().getLevel()/20;
//                } else {
//                    captainWindow=0;
//                }
                //at max CR + max level, the window is 0.15s wide in both directions 
//                superWindow = (crWindow + captainWindow)/10;

                //due to AI issues, the crWindown is no longer captain dependent
                superWindow = (crWindow)/10;
                superWindow*=ship.getMutableStats().getSystemRangeBonus().getBonusMult();
                
                if(ship==engine.getPlayerShip()){
                    playerShip=true;
                } else {
                    playerShip=false;
                }
            }
            
            //AI use of the system
            if(ship.getAIFlags()!=null){
                //compute AI action
                if(AIuse==0){
                    
                    if(ship==engine.getPlayerShip() || ship.getFluxTracker().getFluxLevel()>0.8f){
                        //player ship won't use the system in auto pilot to prevent cheesing, neither a ship at high flux
                        AIuse=-1;
                    } else {
                        float cr = ship.getCurrentCR();
                        float aggressiveness=AGGRESSIVENESS.get("steady");
                        float level=0.2f*ship.getMutableStats().getSystemRangeBonus().getBonusMult();
                        if(ship.getCaptain()!=null && ship.getCaptain().getPersonalityAPI()!=null){
                            aggressiveness = AGGRESSIVENESS.get(ship.getCaptain().getPersonalityAPI().getId());
                            if(ship.getCaptain().getStats().getLevel()>0){
                                level+= ((float)ship.getCaptain().getStats().getLevel())/20;
                            } 
                            
                        }
                        
                        //mainly the personality dictates how aggressively the AI will use the system
                        if(Math.random()<aggressiveness){
                            //AI attempts to use the system
                            //the success is mostly dependent on the CR level and captain skill
                            if(Math.random()< cr/2 + level && Math.random()>0.05){
                                //level 10 captain: 0.35+0.5
                                //success
                                AIuse=2;
                            } else {
                                //failure
                                AIuse=1;
                            }
                        } else {
                            //ignore
                            AIuse=-1;
                        }
                    }
                }
                
                //trigger the system when desired
                if(AIuse==2 && charge>0.5f && charge<1){
                    system.deactivate(); 
                } else if (AIuse==1 && charge>0.33f && charge<1){
                    system.deactivate(); 
                }
            }
            
            //regular base effect
            //reloads ammo and self deactivates
            if(charge==1){
                system.deactivate();                
                //reset
                charge=0;
                superWindow=-1;
                AIuse=0;                
                //release weapon
                ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);                
                return;
            }
            
            //prevent main gun firing
            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyFlat(ID, 10000);
            
            //draw chargebar
            
            if(playerShip){
                drawChargeBar(charge,superWindow,false);
            }
            
        } else if(charge>0){
            
            //apply early deactivation effect
            
            //overload or death
            //cancel the special effect
            if(!ship.isAlive() || ship.getFluxTracker().isOverloadedOrVenting()){
                //reset
                charge=0;
                superWindow=-1;
                AIuse=0;
                //release weapon
                ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);
                return;
            }
            
            if(charge>0.5f-superWindow/2 && charge<0.5f+superWindow/2 ){
                //perfect reload
                //activating super shot mode
                superShot = SUPER_DURATION;
                
                //success sound
                Global.getSoundPlayer().playSound("SKR_reload_perfect", 1, 1, weapon.getLocation(), ship.getVelocity());
                
                //flux bonus
                ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux()*0.8f);
                
                //reset
                charge=0;
                superWindow=-1;
                AIuse=0;
                //release weapon
                ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);
                weapon.setRemainingCooldownTo(0.5f);
                
            } else {
                
                //bad reload
                //flux penalty
                ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux()+ship.getMutableStats().getFluxCapacity().getModifiedValue()*0.2f);
                
                //ammo penalty
                weapon.setAmmo(0);
                                
                //failure sound
                Global.getSoundPlayer().playSound("SKR_reload_failed", 1, 1, weapon.getLocation(), ship.getVelocity());
                
                //reset
                charge=0;
                superWindow=-1;
                AIuse=0;
                //release weapon
                ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID);
            }
        }
        
        //supershot mode
        if(superShot>0){
            superShot-=amount;
            
            if(superShot>0){
                
                //weapon used its super shot
                if(weapon.getChargeLevel()==1){      
                    
                    //sound
                    Global.getSoundPlayer().playSound("SKR_executioner_super", 1, 1, weapon.getLocation(), ship.getVelocity());

                    //visual feedback
                    if(MagicRender.screenCheck(1, weapon.getLocation())){ 
                        superMuzzle(weapon,superShot);
                    }
                     
                    //blowback
                    Vector2f vel = ship.getVelocity();
                    Vector2f.add(
                            vel,
                            MathUtils.getPoint(
                                    new Vector2f(),
                                    50+100*Math.min(1, superShot),
                                    ship.getFacing()+180
                            ),
                            vel
                    );
                    
                    cancelSuperShot();
                    superShot=-1;             
                    return;
                }
                
                //weapon in super shot mode
                enableSuperShot(Math.min(1, superShot), weapon);
                if(playerShip){
                    drawChargeBar(superShot/SUPER_DURATION,0,true);
                }
                
            } else {
                //super shot wasted, remove effects
                superShot=-1;              
                cancelSuperShot();
                return;
            }
            
            //death / overload check
            if(!ship.isAlive() || ship.getFluxTracker().isOverloadedOrVenting()){
                superShot=-1;              
                cancelSuperShot();
            }
        }
    }

    private void drawChargeBar (float charge, float window, boolean superShot){
        if(!superShot){
            
            float level=charge*2;
            
            Color theColor = new Color(0.25f,0.25f,0.25f);
            
            if(level>1-window && level<1+window){
                theColor=Color.BLUE;
            }
            
            level = level - 2*Math.max(0, level-1);
            
            MagicUI.drawInterfaceStatusBar(ship, Math.max(level, 1-window), theColor, Color.RED, level, RELOAD, (int)(charge*100/2));
            
            if(window!=0){
                //cursor UI
                Vector2f cursor = ship.getMouseTarget();
                Vector2f displaySize = new Vector2f(cursorUiSize);
                displaySize.scale(Global.getCombatEngine().getViewport().getViewMult());
                
                MagicRender.singleframe(
                        Global.getSettings().getSprite("fx", "skr_marksman_ui2"),
                        cursor, displaySize,
                        0, Color.WHITE, false);

                MagicRender.singleframe(
                        Global.getSettings().getSprite("fx", "skr_marksman_ui1"),
                        cursor, displaySize,
                        MagicAnim.offsetToRange(charge, -120, 175), Color.WHITE, false);
                
                MagicRender.singleframe(
                        Global.getSettings().getSprite("fx", "skr_marksman_ui0"),
                        cursor, displaySize,
                        (27.5f - (window*150)), Color.WHITE, false);
                
                MagicRender.singleframe(
                        Global.getSettings().getSprite("fx", "skr_marksman_ui0"),
                        cursor, displaySize,
                        (27.5f + (window*150)), Color.WHITE, false);
            }
        } else {
            float level = 1-charge;
            MagicUI.drawInterfaceStatusBar(ship, Math.max(level, 0.5f), new Color(Math.max(0, level-0.5f),Math.min(1, 2-level*2),0), Color.yellow, level, "SUPER", (int)(2-charge/2));
        }
    }
    
    private void enableSuperShot(float effect, WeaponAPI weapon){
        //boost main gun stats
        ship.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(ID, effect*SUPER_BOOST);
        ship.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(ID, effect*SUPER_BOOST);
        ship.getMutableStats().getProjectileSpeedMult().modifyPercent(ID, effect*SUPER_BOOST);

        //lock other weapons
        ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyFlat(ID, 10000);
        ship.getMutableStats().getMissileWeaponFluxCostMod().modifyFlat(ID, 10000);
        
        //add visual feedback
        ship.setWeaponGlow(effect, Color.orange, WEAPONS);
        
        if(MagicRender.screenCheck(1, weapon.getLocation())){        

            if(Math.random()>0.75f){
                float rand = MathUtils.getRandomNumberInRange(0.25f, 0.75f);   

                Vector2f vel = new Vector2f(ship.getVelocity());
                vel.scale(MathUtils.getRandomNumberInRange(0.33f,0.66f));
                Vector2f.add(vel, MathUtils.getRandomPointInCone(new Vector2f(), 30, ship.getFacing()+120, ship.getFacing()+240), vel);

                Global.getCombatEngine().addSmokeParticle(
                        weapon.getLocation(),
                        vel,
                        MathUtils.getRandomNumberInRange(10+10*effect, 20+20*effect),
                        1,
                        effect * (1.5f-rand),
                        new Color(rand,rand,rand,effect*MathUtils.getRandomNumberInRange(0.2f, 0.5f))
                );
            }
        }
    }
    
    private void cancelSuperShot(){
        //reset main gun stats
        ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(ID);
        ship.getMutableStats().getEnergyWeaponDamageMult().unmodify(ID);
        ship.getMutableStats().getProjectileSpeedMult().unmodify(ID);
        ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(ID); 

        //release other weapons
        ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(ID);
        ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(ID);
        
        //remove visual feedback
        ship.setWeaponGlow(0, Color.BLACK, WEAPONS);
    }
    
    private void superMuzzle(WeaponAPI weapon, float level){
        float angle = weapon.getCurrAngle();
        Vector2f loc = MathUtils.getPoint(
                weapon.getLocation(),
                weapon.getSpec().getHardpointFireOffsets().get(0).x,
                angle
        );
        
        CombatEngineAPI engine = Global.getCombatEngine();
        engine.spawnExplosion(
                loc,
                MathUtils.getPoint(
                        new Vector2f(),
                        30,
                        angle
                ),
                Color.DARK_GRAY,
                50+50*level,
                1+1.5f*level
        );
        
        engine.addSmoothParticle(loc, new Vector2f(), 30+90*level, 1, 0.1f, Color.WHITE);
        engine.addSmoothParticle(loc, new Vector2f(), 30+90*level, 1, 0.2f, Color.ORANGE);
        engine.addSmoothParticle(loc, new Vector2f(), 30+90*level, 1, 0.3f, Color.RED);
        
        for(int i=0; i<10; i++){
            engine.addHitParticle(
                    loc, 
                    MathUtils.getPoint(
                            new Vector2f(),
                            (MathUtils.getRandomNumberInRange(25, 100)*level),
                            angle+MathUtils.getRandomNumberInRange(-2.5f, 2.5f)
                    ), 
                    MathUtils.getRandomNumberInRange(3, 5+2*level),
                    1, 
                    MathUtils.getRandomNumberInRange(0.25f, 1f), 
                    Color.RED
            );
        }
    }
}