package data.shipsystems.scripts;

//import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.combat.CombatEngineAPI;
//import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
//import com.fs.starfarer.api.combat.ShipAPI;
//import com.fs.starfarer.api.combat.ShipAPI.HullSize;
//import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
//import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
//import static data.scripts.SKR_modPlugin.bossArrivalSounds;
//import org.magiclib.util.MagicLensFlare;
//import org.magiclib.util.MagicRender;
//import data.scripts.util.SKR_graphicLibEffects;
//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.WeakHashMap;
//import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
//import org.lazywizard.lazylib.combat.CombatUtils;
//import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
//import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
//import org.lwjgl.util.vector.Vector2f;

public class SKR_warpDriveStats extends BaseShipSystemScript {
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        //currently a regular burn in
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, 300f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 300f * effectLevel);
            //stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
        }
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }
    
    /*      
    private CombatEngineAPI engine;
    private final Integer PUSH = 1000000, DISTANCE = 10000;
    
    
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        if(engine!=Global.getCombatEngine()){
            engine = Global.getCombatEngine();
        }
        
        if(engine.isPaused())return;
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        Vector2f location = null;
        
        //chose a warp mode
        if(ship.isRetreating()){
            // regular burn drive out
            if (state == ShipSystemStatsScript.State.OUT) {
                    stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            } else {
                    stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
                    stats.getAcceleration().modifyFlat(id, 600f * effectLevel);
                    //stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
            }
        } else        
        if(ship.getOwner()<1){
            // TODO basic warp in for player side
            //currently a regular burn in
            if (state == ShipSystemStatsScript.State.OUT) {
                    stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            } else {
                    stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
                    stats.getAcceleration().modifyFlat(id, 600f * effectLevel);
                    //stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
            }
            
        } else            
        {
            //advanced enemy warp in
            
            if(state == ShipSystemStatsScript.State.ACTIVE){
                //burn drive is active, look for a location to drop at
                location = findEnemyWarpLocation();

                if(location==null){
                    //no locked coordinates yet
                    stats.getMaxSpeed().modifyMult(id, 0);
                    stats.getAcceleration().modifyMult(id, 0);
                    stats.getMaxTurnRate().modifyMult(id, 0);
                    stats.getTurnAcceleration().modifyMult(id, 0);

                    ship.getVelocity().scale(0);
                    return;
                } else {
                    //time to drop!
                    ship.getTravelDrive().deactivate();
                    //align the ship
                    moveToLocation(ship, new Vector2f(location.x,location.y+20000));
                }
            }
            
            
            //system is active: maintain the ship out of sight, out of range.
            //target found, wait for it to come in range.
            //target found and in range: lock the coordinates, deactivate the system, start the warp animation during chargedown
            //chargedown expended: warp the ship in
        }    
        

        
//        if(stats.getAcceleration().getMultStatMod(id)==null && !ship.isRetreating()){
//            
//            //try to prevent modules from firing and launching fighters by activating a dummy travel drive
//            if(ship.isShipWithModules()){
//                for(ShipAPI m : ship.getChildModulesCopy()){
//                    m.turnOnTravelDrive();
//                }
//            }
//            
//            //move the ship 10k su away from the warp location, and use that position as a reference point without the need to store data
//            Vector2f location = findWarpLocation(ship.getOwner());
//            if(location==null){
//                if(effectLevel>0.75f){
//                    //failsafe and cheese-blocker: default to the middle of the map a couple of seconds before warping in if no valid location is found
//                    moveToLocation (ship,MathUtils.getPoint(new Vector2f(), DISTANCE, ship.getFacing()-180));
//                } else {
//                    // would be nice to increase the active time of the system while the enemy is burning in
//                    return;
//                }
//            }
//            moveToLocation (ship,MathUtils.getPoint(location, DISTANCE, ship.getFacing()-180));
//        }
//        
//        if(effectLevel>0.75)ship.getTravelDrive().deactivate();
//        
//        if(state!=State.ACTIVE && stats.getMaxSpeed().getMultStatMod(id)==null){
//            moveToLocation (ship,MathUtils.getPoint(ship.getLocation(), DISTANCE, ship.getFacing()));
//            arrivalEffect(ship,Global.getSettings().getModManager().isModEnabled("shaderLib"), ship.isRetreating()); 
//            stats.getMaxSpeed().modifyMult(id, 0.5f);
//        } else {            
//            stats.getAcceleration().modifyMult(id, 0);
//            stats.getTurnAcceleration().modifyMult(id, 0f);
//            stats.getMaxTurnRate().modifyMult(id, 0);
//            
//            if(ship.isRetreating()){
//                warpZone(effectLevel,ship.getLocation(),!ship.isRetreating());
//            } else {
//                warpZone(effectLevel,MathUtils.getPoint(ship.getLocation(), DISTANCE, ship.getFacing()),!ship.isRetreating());
//            }
//        }

    }
    
        
    private void arrivalEffect(ShipAPI ship, boolean shader, boolean retreating) {
        //sound
        String sound = bossArrivalSounds.get(ship.getHullSpec().getBaseHullId());
        if(sound==null){
            sound = "SKR_keep_arrival";
        }
        
        Vector2f loc=ship.getLocation();
        if(retreating)loc = MathUtils.getPoint(ship.getLocation(), DISTANCE, ship.getFacing()-180);
        
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
    
    private void warpZone (float intensity, Vector2f location, boolean pushAway){
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
    
    private void moveToLocation (ShipAPI ship, Vector2f loc){
        Vector2f shipLoc = ship.getLocation();
        Vector2f shipVel = ship.getVelocity();
        shipLoc.set(loc);
        shipVel.set(MathUtils.getPoint(new Vector2f(), 50, ship.getFacing()));
    }
    
    
    ShipAPI target = null;
    
    private Vector2f findEnemyWarpLocation(){
        
        //check for a good target to warp near to
        if(target==null || !engine.isEntityInPlay(target) || !target.isAlive()){
            target = findSuitableTarget(FleetSide.PLAYER);
        }
        
        //if there are no valid target, just wait
        if (target == null){
            return null;
        }
        
        if(engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy().size()>1){
            //fleet deployment, wait for scouts to find the player flagship (if deployed) or a big ship
            
            if(target==null){
                //find a suitable target if possible
                target = findSuitableTarget(FleetSide.PLAYER);
                if(target == null) return null;
            }
            
            //check if the target is in visual range
            if (CombatUtils.isVisibleToSide(target, 1 //enemy side//)){
                // return a random point a bit further above the target
                return new Vector2f(target.getLocation().x + MathUtils.getRandomNumberInRange(-500, 500), target.getLocation().y + MathUtils.getRandomNumberInRange(2000, 3000));
            } else return null;
            
        } else {
            //solo deployment, wait for the enemy flagship to move further that the middle of the map (if deployed) or a big ship that does
            
            if(target==null){
                //find a suitable target if possible
                target = findSuitableTarget(FleetSide.PLAYER);
                if(target == null) return null;
            }
            
            if (target.getLocation().y >= 0){
                // return a random point a bit further above the target
                return new Vector2f(target.getLocation().x + MathUtils.getRandomNumberInRange(-500, 500), target.getLocation().y + MathUtils.getRandomNumberInRange(2000, 3000));
            } else return null;
        }
    }

    private ShipAPI findSuitableTarget(FleetSide side){
        //Skip if there are no ship to find
        if(engine.getFleetManager(side).getDeployedCopy().size()<=0)return null;
        
        //if the player flagship is deployed, this is the priority target
        if(side==FleetSide.PLAYER && engine.getPlayerShip()!=null && engine.getPlayerShip().isAlive()){
            return engine.getPlayerShip();
        }
        
        //if the player ship is not deployed, find the biggest target around        
        Map <HullSize, FleetMemberAPI> ships= new WeakHashMap<>();
        
        for (FleetMemberAPI m : engine.getFleetManager(side).getDeployedCopy()){
            //find a target for each ship size
            if(engine.getFleetManager(side).getShipFor(m).isAlive()){
                HullSize s = m.getHullSpec().getHullSize();
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
        if(ships.containsKey(HullSize.CAPITAL_SHIP)) return engine.getFleetManager(side).getShipFor(ships.get(HullSize.CAPITAL_SHIP));        
        if(ships.containsKey(HullSize.CRUISER)) return engine.getFleetManager(side).getShipFor(ships.get(HullSize.CRUISER));
        if(ships.containsKey(HullSize.DESTROYER)) return engine.getFleetManager(side).getShipFor(ships.get(HullSize.DESTROYER));
        if(ships.containsKey(HullSize.FRIGATE)) return engine.getFleetManager(side).getShipFor(ships.get(HullSize.FRIGATE));
        return null;
    }
    */
}
