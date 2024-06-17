//By Tartiflette: this low impact script hide a deco weapon when the ship is destroyed

package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;

public class SKR_keepSystemEffect implements EveryFrameWeaponEffectPlugin{
    
    private boolean runOnce=false, active=false, arrival=false, SHADER=false;
    private ShipSystemAPI system;
//    private ShipAPI ship;
    private WeaponAPI GUN;
    private final IntervalUtil cranky= new IntervalUtil(10,20);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon){
        
        if (engine.isPaused()) {return;}
                
        if(!runOnce){
            runOnce=true;
            system=weapon.getShip().getSystem();
            for(WeaponAPI w : weapon.getShip().getAllWeapons()){
                if(w.getSlot().getId().equals("WS0001")){
                    GUN=w;
                }
            }
            SHADER=Global.getSettings().getModManager().isModEnabled("shaderLib");
        }
        
        if(GUN.getChargeLevel()==1){
            //muzzle
            engine.addHitParticle(GUN.getLocation(), weapon.getShip().getVelocity(), 600, 0.5f, 0.33f, new Color(200,96,0,32));
            engine.addHitParticle(GUN.getLocation(), weapon.getShip().getVelocity(), 300, 2, 0.1f, Color.WHITE);

            for(int i=0; i<20; i++){

                Vector2f vel=MathUtils.getRandomPointInCone(new Vector2f(), i*5, GUN.getCurrAngle()-5, GUN.getCurrAngle()+5);
                Vector2f pos=new Vector2f(GUN.getLocation());
                Vector2f.add(pos, vel, pos);
                Vector2f.add(vel, GUN.getShip().getVelocity(), vel);
                int color = MathUtils.getRandomNumberInRange(64,255);

                engine.addSmokeParticle(
                        pos,
                        vel,
                        MathUtils.getRandomNumberInRange(50, 100),
                        1,
                        MathUtils.getRandomNumberInRange(2f, 5f),
                        new Color(color,color,color,32)
                );
            }

            for(int i=0; i<30; i++){
                engine.addHitParticle(
                        MathUtils.getRandomPointInCone(GUN.getLocation(), i*4, GUN.getCurrAngle()-5, GUN.getCurrAngle()+5),
                        GUN.getShip().getVelocity(),
                        MathUtils.getRandomNumberInRange(30, 150-6*i),
                        1,
                        0.1f+0.02f*i,
                        new Color(200, (int)(i*5)+20,20,128)
                );
            }      
            engine.spawnExplosion(GUN.getLocation(), GUN.getShip().getVelocity(), new Color (200,100,50,128), 350, 0.75f);
        }
        
        
        if(system.isActive()){
            float level = system.getEffectLevel();
            
            weapon.getAnimation().setFrame(1);            
            weapon.getSprite().setColor(new Color(Math.min(1,Math.max(0, level)), Math.min(1, Math.max(0, level-0.25f)),0.0f));
            
//            if(level==1){
//                for(WeaponAPI w : weapon.getShip().getDisabledWeapons()){
//                    if(!w.isPermanentlyDisabled()){
//                        w.repair();
//                    }
//                }
//            }
            if(!active){
                active=true;
                weapon.getShip().getMutableStats().getCombatWeaponRepairTimeMult().modifyMult("SKR_spasm", 0.1f);
                weapon.getShip().getMutableStats().getCombatEngineRepairTimeMult().modifyMult("SKR_spasm", 0.1f);
                for (FighterLaunchBayAPI bay : weapon.getShip().getLaunchBaysCopy()) {
                    if (bay.getWing() == null) continue;

                    bay.makeCurrentIntervalFast();

//                    FighterWingSpecAPI spec = bay.getWing().getSpec();
                    int rebuild = bay.getWing().getSpec().getNumFighters() - bay.getWing().getWingMembers().size();
                    if (rebuild > 0) {
                        bay.setFastReplacements(rebuild);
                    }
                }
            }
        } else if(active){
            active=false;
            weapon.getShip().getMutableStats().getCombatWeaponRepairTimeMult().unmodify("SKR_spasm");
            weapon.getShip().getMutableStats().getCombatEngineRepairTimeMult().unmodify("SKR_spasm"); 
            weapon.getAnimation().setFrame(0);
        }
        
        //ambience sounds
        cranky.advance(amount);
        if(cranky.intervalElapsed()){
            Global.getSoundPlayer().playSound("SKR_keep_cranky", MathUtils.getRandomNumberInRange(0.95f, 1.05f), MathUtils.getRandomNumberInRange(0.5f, 0.75f), weapon.getShip().getLocation(), weapon.getShip().getVelocity());
        }
        /*
        //warp in effect
        if(!arrival && weapon.getShip().getOriginalOwner()>0){
            arrival = true;
            arrivalEffect(weapon.getShip());
        }
        */
    }
    /*
    private void arrivalEffect(ShipAPI ship){
         //sound
//        Global.getSoundPlayer().playSound("SKR_keep_arrival", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1, ship.getLocation(), ship.getVelocity());
        Global.getSoundPlayer().playUISound("SKR_keep_arrival", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1);
        Vector2f relocation = ship.getLocation();
        relocation.scale(0.5f);

        //ripple
        if(SHADER){
            SKR_graphicLibEffects.CustomRippleDistortion(
                    ship.getLocation(), 
                    new Vector2f(ship.getVelocity()),
                    720,
                    25, 
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
        for(int i=0; i<15; i++){
            MagicRender.battlespace(
                    Global.getSettings().getSprite(
                            "fx",
                            "sweetener"
                    ),
                    MathUtils.getRandomPointInCone(
                            ship.getLocation(),
                            512,
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
                new Vector2f(512,512),
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
    }
*/
}