package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.mission.FleetSide;
import static data.scripts.SKR_modPlugin.bossArrivalSounds;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import org.lwjgl.util.vector.Vector2f;

public class SKR_warpDriveEffect implements EveryFrameWeaponEffectPlugin {    
    
    private boolean runOnce=false, ActiveWarp=true;
    private final Integer PUSH = 1000000, DISTANCE = 20000, TELEGRAPHING = 10;    
    private final String ID = "warpDrive";
    
    private ShipAPI target=null, ship=null;
    private Vector2f warpTo=null;
    private float timer=0;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if(!runOnce){
            runOnce=true;
            ship=weapon.getShip();
//            ship.turnOnTravelDrive(9999);
//            
//            //try to prevent modules from firing and launching fighters by activating a dummy travel drive
//            if(ship.isShipWithModules()){
//                for(ShipAPI m : ship.getChildModulesCopy()){
//                    m.turnOnTravelDrive(9999);
//                }
//            }
        }
        
        //retreating warp out check        
        if(ship.isAlive() && ship.isRetreating() && ship.getTravelDrive().isActive() && ship.getTravelDrive().getEffectLevel()==1){
            warpOut(ship);
        }
        
        if(engine.isPaused() || ship.getOriginalOwner()<0 || !ActiveWarp)return;
        
        //chose a warp mode    
        if(ship.getOwner()<1){
            //basic warp in for player side
            moveToLocation(ship, MathUtils.getPoint(ship.getLocation(), 3000, ship.getFacing()), ship.getFacing(), 300);
            ship.turnOnTravelDrive(1);
            warpVisualEffect(ship, Global.getSettings().getModManager().isModEnabled("shaderLib"), false);
            if(ship.isShipWithModules()){
                for(ShipAPI m : ship.getChildModulesCopy()){
                    m.turnOnTravelDrive(3);
                }
            }

            ActiveWarp=false;
        } else            
        {
            // advanced BOSS warp in
            if(warpTo!=null){
                //warp animation
                if(timer<1){
                    timer +=amount/(float)TELEGRAPHING;
                    warpZone(engine,timer,warpTo,true);
                } else {  
                    
                    warpIn(ship);
                    
                    //turn off warp script
                    ActiveWarp=false;
                    timer=0;
                }
            } else if (target!=null && target.isAlive()){
                //find the warp coordinates
                warpTo = findWarpLocation(engine, target);
            } else {
                //find a suitable target to warp to
                target = findSuitableTarget(engine, FleetSide.PLAYER);
                
                //move the boss away in the meantime
                moveToLocation(ship, new Vector2f(0,DISTANCE),180,0);
//                if(!ship.getTravelDrive().isOn()){
//                    ship.turnOnTravelDrive(9999);
//                }
                freeze(ship.getMutableStats());
                
                //try to prevent modules from firing and launching fighters by activating a dummy travel drive
//                if(ship.isShipWithModules()){
//                    for(ShipAPI m : ship.getChildModulesCopy()){
//                        if(!m.getTravelDrive().isOn()){
//                            m.turnOnTravelDrive(9999);
//                        }
//                    }
//                }
            }
        }
    }
    
    private void freeze(MutableShipStatsAPI stats){
        stats.getMaxSpeed().modifyMult(ID, 0);
        stats.getAcceleration().modifyMult(ID, 0);
        stats.getDeceleration().modifyMult(ID, 0);
        stats.getTurnAcceleration().modifyMult(ID, 0);
        stats.getMaxTurnRate().modifyMult(ID, 0);
        stats.getFluxCapacity().modifyMult(ID, 0);
        stats.getFluxDissipation().modifyMult(ID, 0);
    }
    private void unfreeze(MutableShipStatsAPI stats){
        stats.getMaxSpeed().unmodify(ID);
        stats.getAcceleration().unmodify(ID);
        stats.getDeceleration().unmodify(ID);
        stats.getTurnAcceleration().unmodify(ID);
        stats.getMaxTurnRate().unmodify(ID);
        stats.getFluxCapacity().unmodify(ID);
        stats.getFluxDissipation().unmodify(ID);
    }
    private void moveToLocation (ShipAPI ship, Vector2f loc, float azimut, float speed){
//        Vector2f shipLoc = ship.getLocation();
//        Vector2f shipVel = ship.getVelocity();
        ship.setFacing(azimut);
        ship.getLocation().set(loc);
        ship.getVelocity().set(MathUtils.getPoint(new Vector2f(), speed, azimut));
    }
    
    private void warpIn(ShipAPI ship){
        moveToLocation(ship, warpTo, VectorUtils.getAngle(ship.getLocation(), warpTo), 300);
        unfreeze(ship.getMutableStats());
        ship.turnOffTravelDrive();
        ship.turnOnTravelDrive(2);
//        ship.getTravelDrive().forceState(ShipSystemAPI.SystemState.OUT, 1);

        //land wings
        if(!ship.getAllWings().isEmpty()){            
            List <FighterWingAPI> wings = ship.getAllWings();            
            for(FighterWingAPI w : wings){
                if(!w.getWingMembers().isEmpty()){
                    for(ShipAPI f : w.getWingMembers()){
                        f.getWing().getSource().makeCurrentIntervalFast();
                        f.getWing().getSource().land(f);
                    }
                }
            }
        }

        warpVisualEffect(ship, Global.getSettings().getModManager().isModEnabled("shaderLib"), false);
        //release modules from their dummy travel drive
        if(ship.isShipWithModules()){
            for(ShipAPI m : ship.getChildModulesCopy()){
                m.turnOffTravelDrive();
                m.turnOnTravelDrive(3);
                //land wings
                if(!m.getAllWings().isEmpty()){            
                    List <FighterWingAPI> wings = m.getAllWings();            
                    for(FighterWingAPI w : wings){
                        if(!w.getWingMembers().isEmpty()){
                            for(ShipAPI f : w.getWingMembers()){
                                f.getWing().getSource().makeCurrentIntervalFast();
                                f.getWing().getSource().land(f);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void warpOut(ShipAPI ship){        
        warpVisualEffect(ship, Global.getSettings().getModManager().isModEnabled("shaderLib"), true);
        moveToLocation(ship, MathUtils.getPoint(ship.getLocation(), DISTANCE, ship.getFacing()), ship.getFacing(), 600);
    }
    
    /*
    Returns a random point further up from a target if visible
    Returns null while there are no such target in range
    */ 
    private Vector2f findWarpLocation(CombatEngineAPI engine, ShipAPI target){
        
        //if there are no valid target, just wait
        if (target == null){
            return null;
        }
        
        Integer shipOrFleet = 0;
        for(FleetMemberAPI m : engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy()){
            if(engine.getFleetManager(FleetSide.ENEMY).getShipFor(m).getParentStation()==null && !m.isFighterWing()){
                shipOrFleet++;
            }
        }
        
        if(shipOrFleet>1){
            //fleet deployment, check if the target is in visual range of scouts
            if (CombatUtils.isVisibleToSide(target, 1 /*enemy side*/)){
                // return a random point a bit further above the target
                return new Vector2f(target.getLocation().x + MathUtils.getRandomNumberInRange(-500, 500), target.getLocation().y + MathUtils.getRandomNumberInRange(2000, 3000));
            } else return null;            
        } else {
            //solo deployment, wait for the target to move further that the middle of the map
            if (target.getLocation().y >= 0 - engine.getTotalElapsedTime(false)*20){
                // return a random point a bit further above the target
                return new Vector2f(target.getLocation().x + MathUtils.getRandomNumberInRange(-500, 500), target.getLocation().y + MathUtils.getRandomNumberInRange(2000, 3000));
            } else return null;
        }
    }

    /*
    Returns the player ship if deployed on the requested side, or a random ship of the largest class, or null if there is nobody alive on the map
    */  
    private ShipAPI findSuitableTarget(CombatEngineAPI engine, FleetSide side){
        //Skip if there are no ship to find
        if(engine.getFleetManager(side).getDeployedCopy().size()<=0)return null;
        
        //if the player flagship is deployed, this is the priority target
        if(side==FleetSide.PLAYER && engine.getPlayerShip()!=null && engine.getPlayerShip().isAlive()){
            return engine.getPlayerShip();
        }
        
        //if the player ship is not deployed, find the biggest target around        
        Map <ShipAPI.HullSize, FleetMemberAPI> ships= new WeakHashMap<>();
        
        for (FleetMemberAPI m : engine.getFleetManager(side).getDeployedCopy()){
            //find a target for each ship size
            if(engine.getFleetManager(side).getShipFor(m).isAlive()){
                ShipAPI.HullSize s = m.getHullSpec().getHullSize();
                if(!ships.containsKey(s)){
                    ships.put(s, m);
                } else {
                    //if there is already a ship this size in the map, randomly replace it with the new one (for shuffling different attempts with the same fleet)
                    if(Math.random()>0.5f){
                        ships.put(s, m);
                    }
                }
            }
        }
        
        //return the largest target available
        if(ships.containsKey(ShipAPI.HullSize.CAPITAL_SHIP)) return engine.getFleetManager(side).getShipFor(ships.get(ShipAPI.HullSize.CAPITAL_SHIP));        
        if(ships.containsKey(ShipAPI.HullSize.CRUISER)) return engine.getFleetManager(side).getShipFor(ships.get(ShipAPI.HullSize.CRUISER));
        if(ships.containsKey(ShipAPI.HullSize.DESTROYER)) return engine.getFleetManager(side).getShipFor(ships.get(ShipAPI.HullSize.DESTROYER));
        if(ships.containsKey(ShipAPI.HullSize.FRIGATE)) return engine.getFleetManager(side).getShipFor(ships.get(ShipAPI.HullSize.FRIGATE));
        return null;
    }
    
    
    private void warpZone(CombatEngineAPI engine, float intensity, Vector2f location, boolean pushAway){
        if(pushAway){
            //push away ships in the danger zone
            for(ShipAPI s : CombatUtils.getShipsWithinRange(location, intensity*750)){
                Vector2f vel = s.getVelocity();
                Vector2f.add(
                        vel,
                        MathUtils.getPoint(
                                new Vector2f(),
                                engine.getElapsedInLastFrame() * Math.min(50, intensity * PUSH / ( 1 + MathUtils.getDistanceSquared(location, s.getLocation())) ),
                                VectorUtils.getAngle(
                                        location,
                                        s.getLocation()
                                )
                        ),
                        vel
                );
            }
        }
        
        //central glow
        if(Math.random()<0.1f+intensity*0.25){
            engine.addHitParticle(
                    location,
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(200, 300+500*intensity),
                    0.25f+0.25f*intensity,
                    MathUtils.getRandomNumberInRange(0.05f, 0.1f+0.1f*intensity),
                    new Color (
                            0.3f + 0.1f*MathUtils.getRandomNumberInRange(0, 0.5f+0.5f*intensity),
                            0.05f + 0.1f*MathUtils.getRandomNumberInRange(0, 0.5f+0.5f*intensity),
                            0.4f + 0.1f*MathUtils.getRandomNumberInRange(0, 0.5f+0.5f*intensity)
                    )
            );
        }
        
        //Particle SUCC
        if(Math.random()<0.25f+(intensity*0.5f)){
            
            Vector2f offset;
            if(pushAway){
                offset = MathUtils.getRandomPointInCircle(new Vector2f(), 50+150*intensity);
            } else {
                offset = MathUtils.getRandomPointInCircle(new Vector2f(), 200-150*intensity);
            }
            
            engine.addHitParticle(
                    Vector2f.sub(location, offset, new Vector2f()),
                    offset,
                    MathUtils.getRandomNumberInRange(3, 6+6*intensity),
                    0.5f+0.5f*intensity,
                    MathUtils.getRandomNumberInRange(0.5f, 1+intensity),
                    new Color (
                            0.1f + 0.1f*MathUtils.getRandomNumberInRange(0, 0.5f+0.5f*intensity),
                            0.2f + 0.1f*MathUtils.getRandomNumberInRange(0, 0.5f+0.5f*intensity),
                            0.6f + 0.1f*MathUtils.getRandomNumberInRange(0, 0.5f+0.5f*intensity)
                    )
            );
            
        }
        
        //FLARES
        if(Math.random()<0.1f+(intensity*0.15f)){
            Vector2f offset;
            if(pushAway){
                offset = MathUtils.getRandomPointInCircle(location, 200-175*intensity);
            } else {
                offset = MathUtils.getRandomPointInCircle(location, 25+175*intensity);
            }
            
            MagicLensFlare.createSharpFlare(
                    engine,
                    engine.getPlayerShip(),
                    offset,
                    MathUtils.getRandomNumberInRange(2, 3+3*intensity),
                    MathUtils.getRandomNumberInRange(50, 100+350*intensity),
                    0,
                    new Color(50,0,150),
                    Color.RED
            );
            
            engine.addHitParticle(
                    offset,
                    new Vector2f(),
                    MathUtils.getRandomNumberInRange(30, 80+80*intensity),
                    0.5f+0.5f*intensity,
                    MathUtils.getRandomNumberInRange(0.25f, 0.5f+0.5f*intensity),
                    new Color(100,0,175)
            );
            
            if(Math.random()<0.1f){
                Global.getSoundPlayer().playSound("SKR_arrival_ripple", 0.8f+0.4f*intensity, 0.5f+0.5f*intensity, location, new Vector2f());
            }
        }
        
        //AMBIENCE SOUND
        Global.getSoundPlayer().playLoop("SKR_arrival_shadow", engine.getPlayerShip(), 1, 1, location, new Vector2f());
    }
    
    
    private void warpVisualEffect(ShipAPI ship, boolean shader, boolean retreating) {
        //sound
        String sound = bossArrivalSounds.get(ship.getHullSpec().getBaseHullId());
        if(sound==null){
            sound = "SKR_keep_arrival";
        }
        
        Vector2f loc=new Vector2f(ship.getLocation());
//        if(retreating)loc = MathUtils.getPoint(ship.getLocation(), DISTANCE, ship.getFacing()-180);
        
        Global.getSoundPlayer().playSound(sound, 1, 1, loc, ship.getVelocity());
        Global.getSoundPlayer().playUISound(sound, 1, 0.25f);

        //ripple
        if(shader){
            SKR_graphicLibEffects.CustomRippleDistortion(
                    loc, 
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
            
            float facing;
            Vector2f randomLoc, size, growth;
            if(retreating){randomLoc = MathUtils.getRandomPointInCone(
                            loc,
                            720,
                            ship.getFacing()-40,
                            ship.getFacing()+40
                    );
                growth = new Vector2f(
                            MathUtils.getRandomNumberInRange(1, 4),
                            MathUtils.getRandomNumberInRange(512, 1024)
                    );
                size = new Vector2f(
                            MathUtils.getRandomNumberInRange(16, 32),
                            MathUtils.getRandomNumberInRange(128, 256)
                    );
                facing = ship.getFacing()+90;
            } else {
                randomLoc = MathUtils.getRandomPointInCone(
                            loc,
                            720,
                            ship.getFacing()+140,
                            ship.getFacing()+220
                    );
                growth = new Vector2f(
                            MathUtils.getRandomNumberInRange(-0.5f, -2.5f),
                            MathUtils.getRandomNumberInRange(-400, -500)
                    );
                size = new Vector2f(
                            MathUtils.getRandomNumberInRange(16, 32),
                            MathUtils.getRandomNumberInRange(512, 1024)
                    );
                facing = ship.getFacing()-90;
            }
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "sweetener"
                    ),
                    randomLoc,
                    MathUtils.getPoint(
                            new Vector2f(),
                            MathUtils.getRandomNumberInRange(256, 360),
                            ship.getFacing()
                    ),
                    size,
                    growth,
                    facing,
                    0,
                    Color.WHITE,
                    true,
                    0,
                    0.1f,
                    MathUtils.getRandomNumberInRange(
                            0.5f,
                            5
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
                            loc,
                            512-size/2
                    ),
                    MathUtils.getRandomPointInCone(
                            new Vector2f(),
                            64,
                            ship.getFacing()+150,
                            ship.getFacing()+210
                    ),
                    new Vector2f(size,size),
                    new Vector2f(growth,growth),
                    MathUtils.getRandomNumberInRange(
                            ship.getFacing()-60,
                            ship.getFacing()-120
                    ),
                    MathUtils.getRandomNumberInRange(-15,15),
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
                loc,
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
                loc,
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
                GL_ONE_MINUS_SRC_COLOR,
                GL_ONE_MINUS_SRC_ALPHA
        );
        
        //trail        
        if(!retreating){
            for(int i=1; i<50; i++){

                Vector2f point = MathUtils.getPoint(new Vector2f(),i*(i+5),ship.getFacing()+180);
                float duration = 5.1f-(0.1f*i);
                Vector2f vel = MathUtils.getPoint(new Vector2f(),5*i-4,ship.getFacing());

                //modules
                for(ShipAPI s : ship.getChildModulesCopy()){
                    if(s.isAlive()){
                        s.addAfterimage(
                        new Color(0.5f,0.25f,1f,0.15f),
                        point.x,point.y,vel.x,vel.y,
                        0.1f,
                        0,0.1f,duration,
                        false,true,false
                        );
                    }
                }
                //ship
                ship.addAfterimage(
                        new Color(0.5f,0.25f,1f,0.15f),
                        point.x,point.y,vel.x,vel.y,
                        0.1f,
                        0,0.1f,duration,
                        false,true,false
                );
            }
        }
    }
    
}