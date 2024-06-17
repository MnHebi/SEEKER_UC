/*
    By Tartiflette
 */
package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_bullhornEngine implements EveryFrameWeaponEffectPlugin {
    
    private boolean runOnce=false, active=false;
    private ShipSystemAPI SYSTEM;
    private AnimationAPI ANIMATION;
    private int MAX_FRAME;
    private int HEIGHT, WIDTH;
    private final IntervalUtil timer = new IntervalUtil(0.025f,0.05f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if(!runOnce){
            runOnce=true;
            timer.randomize();
            
            SYSTEM=weapon.getShip().getSystem();
            ANIMATION=weapon.getAnimation();
            MAX_FRAME=ANIMATION.getNumFrames()-1;
            
            ANIMATION.setFrame(1);
            HEIGHT=(int)weapon.getSprite().getHeight();
            WIDTH=(int)weapon.getSprite().getWidth();
            ANIMATION.setFrame(0);
            
            int SYSTEM_AMMO = 12;
            float ammo = weapon.getShip().getMutableStats().getSystemRangeBonus().getBonusMult();
            if(ammo!=1){
                SYSTEM_AMMO = Math.round(ammo * SYSTEM_AMMO);
                weapon.getShip().getSystem().setAmmo(SYSTEM_AMMO);
            }
        }    

        if(engine.isPaused()){
            return;
        }
        
        //screencheck
        if(!MagicRender.screenCheck(0.3f, weapon.getShip().getLocation())) return;
        
        if(SYSTEM.isActive()){
            active=true;
            //30FPS
            timer.advance(amount);
            if(timer.intervalElapsed()){
                //plume
                ANIMATION.setFrame(MathUtils.getRandomNumberInRange(1, MAX_FRAME));

                SpriteAPI sprite = weapon.getSprite();
                
                float height = HEIGHT*SYSTEM.getEffectLevel()+MathUtils.getRandomNumberInRange(-1, 1);
                float width = WIDTH/2+WIDTH/2*SYSTEM.getEffectLevel()+MathUtils.getRandomNumberInRange(-1, 1);
                sprite.setWidth(width);
                sprite.setHeight(height);
                sprite.setCenter(width/2, height*0.075f);

                sprite.setColor(
                        new Color(
                                128+(int)(127*SYSTEM.getEffectLevel()),
                                (int)(255*SYSTEM.getEffectLevel()),
                                (int)(255*SYSTEM.getEffectLevel())
                        )
                );
                
                //glow
                engine.addHitParticle(
                        weapon.getLocation(),
                        weapon.getShip().getVelocity(), 
                        30+30*SYSTEM.getEffectLevel()+MathUtils.getRandomNumberInRange(-3, 3),
                        1,
                        MathUtils.getRandomNumberInRange(0.1f, 0.15f),                        
                        new Color(
                                100+(int)(50*SYSTEM.getEffectLevel()),
                                (int)(120*SYSTEM.getEffectLevel()),
                                (int)(90*SYSTEM.getEffectLevel())
                        )
                );
            }
        } else if(active){
            active=false;
            ANIMATION.setFrame(0);
            Global.getSoundPlayer().playSound(
                    "SKR_boost_discard",
                    MathUtils.getRandomNumberInRange(0.95f, 1.05f),
                    0.35f,
                    weapon.getLocation(), 
                    weapon.getShip().getVelocity()
            );
            engine.spawnProjectile(
                    weapon.getShip(),
                    weapon,
                    "SKR_bullhornDiscard", 
                    weapon.getLocation(),
                    weapon.getCurrAngle()+MathUtils.getRandomNumberInRange(-10, 10),
                    (Vector2f)new Vector2f(weapon.getShip().getVelocity()).scale(MathUtils.getRandomNumberInRange(0.9f, 1.1f))
            );
        }
    }
}