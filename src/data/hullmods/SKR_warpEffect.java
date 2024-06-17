package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import static data.scripts.SKR_modPlugin.bossArrivalSounds;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
import org.lwjgl.util.vector.Vector2f;

public class SKR_warpEffect extends BaseHullMod {
    
    private CombatEngineAPI engine;
    private float intensity=0;
    private final Integer PUSH = 100000;
    private boolean press=false;
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        
        if(engine!=Global.getCombatEngine()){
            engine = Global.getCombatEngine();
        }
        
        if(ship.getOriginalOwner()==-1 || ship.getFullTimeDeployed()<=0){
            return;
        }
        
        //arrival test
        if(Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && !press){
            press=true;
            arrivalEffect(ship,Global.getSettings().getModManager().isModEnabled("shaderLib"));
        } else {
            press=false;
        }
        
        //aura test
        if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
            intensity=Math.min(1, intensity + amount/5);
            warpZone(intensity, MathUtils.getPoint(ship.getLocation(), ship.getShipExplosionRadius(),ship.getFacing()), true);
        } else {
            intensity=0;
        }
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
    
    private void arrivalEffect(ShipAPI ship, boolean shader){
        //sound
        String sound = bossArrivalSounds.get(ship.getHullSpec().getBaseHullId());
        if(sound==null){
            sound = "SKR_keep_arrival";
        }
        
        Vector2f loc=ship.getLocation();
        
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
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "sweetener"
                    ),
                    MathUtils.getRandomPointInCone(
                            loc,
                            720,
                            ship.getFacing()+140,
                            ship.getFacing()+220
                    ),
                    MathUtils.getPoint(
                            new Vector2f(),
                            MathUtils.getRandomNumberInRange(128, 256),
                            ship.getFacing()
                    ),
                    new Vector2f(
                            MathUtils.getRandomNumberInRange(16, 32),
                            MathUtils.getRandomNumberInRange(512, 1024)
                    ),
                    new Vector2f(
                            0,
                            MathUtils.getRandomNumberInRange(-100, -300)
                    ),
                    ship.getFacing()-90,
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
        for(int i=1; i<50; i++){
            
            Vector2f point = MathUtils.getPoint(new Vector2f(),i*(i+5),ship.getFacing()+180);
            Vector2f vel = MathUtils.getPoint(new Vector2f(),5*i-4,ship.getFacing());
            
            //modules
            for(ShipAPI s : ship.getChildModulesCopy()){
                if(s.isAlive()){
                    s.addAfterimage(
                    new Color(0.5f,0.25f,1f,0.15f),
                    point.x,point.y,vel.x,vel.y,
                    0.1f,
                    0,0.1f,5.1f-(0.1f*i),
                    false,true,false
                    );
                }
            }
            //ship
            ship.addAfterimage(
                    new Color(0.5f,0.25f,1f,0.15f),
                    point.x,point.y,vel.x,vel.y,
                    0.1f,
                    0,0.1f,5.1f-(0.1f*i),
                    false,true,false
            );
        }
    }
    
    
    private void warpZone (float intensity, Vector2f location, boolean pushAway){
        /*
        if(pushAway){
            //push away ships in the danger zone
            for(ShipAPI s : CombatUtils.getShipsWithinRange(location, intensity*500)){
                Vector2f vel = s.getVelocity();
                Vector2f.add(vel, MathUtils.getPoint(new Vector2f(), intensity * engine.getElapsedInLastFrame()*PUSH/(1+MathUtils.getDistanceSquared(location, s.getLocation())), VectorUtils.getAngle(location, s.getLocation())), vel);
            }
        }
        */
        
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
//            Vector2f point = new Vector2f();
//            Vector2f.sub(location, offset, point);
//            float size = MathUtils.getRandomNumberInRange(3, 5+5*intensity);
//            float brightness = 0.25f+0.5f*intensity;
//            float duration = MathUtils.getRandomNumberInRange(0.5f, 1+intensity);
//            engine.addHitParticle(
//                    point,
//                    offset,
//                    size,
//                    brightness,
//                    duration,
//                    Color.cyan
//            );
            
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
}