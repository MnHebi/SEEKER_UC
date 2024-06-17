package data.scripts.weapons;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_balisongPdEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce=false, disabled=false;
    private SpriteAPI sprite;
    private AutofireAIPlugin ai;
    private float time=0, X=0,Y=0, size, mult=1;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (!runOnce){
            runOnce=true;
            sprite=weapon.getSprite();
            X=sprite.getCenterX();
            Y=sprite.getCenterY();
            size=sprite.getWidth();
            ai=weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon);
            time+=(float)Math.random()*5;
        }
        
        if (engine.isPaused() || !weapon.getShip().isAlive()) {
            if(!disabled){
                disabled=true;
                disable(engine,weapon);
            }
            return;
        }
        
        if(weapon.isDisabled()){
            if(!disabled){
                disabled=true;
                disable(engine,weapon);
                mult=0;
            }
        } else {
            if(mult<1){
                mult=Math.min(1, mult+amount);
                sprite.setSize(mult*size, mult*size);
            }
            time+=5*amount;
        }
        
        float y = mult*10*(float)FastTrig.sin(time);
        float x = mult*25*(float)FastTrig.sin(time/2);
        
        sprite.setCenter(X+x, Y+y);
        
        if(weapon.isFiring()){
            weapon.ensureClonedSpec();
            for(int i=0; i<weapon.getSpec().getHardpointFireOffsets().size(); i++){
                weapon.getSpec().getHardpointFireOffsets().set(i, new Vector2f(-y,x));
                weapon.getSpec().getTurretFireOffsets().set(i, new Vector2f(-y,x));
                                
                float angle;
                Vector2f aim;
                
                if(ai.getTarget()!=null){
                    aim=ai.getTarget();
                } else {
                    aim=MathUtils.getPoint(weapon.getLocation(), weapon.getRange(), weapon.getCurrAngle());
                }
                
                Vector2f offset=new Vector2f(-y,x);
                VectorUtils.rotate(offset, weapon.getCurrAngle(), offset);
                Vector2f.add(offset, weapon.getLocation(), offset);
                
                //debug
//                engine.addHitParticle(offset, new Vector2f(), 5f, 1f, 0.1f, Color.yellow);
                
                angle=VectorUtils.getAngle(offset, aim);
                angle=MathUtils.getShortestRotation(weapon.getCurrAngle(),angle);
                
                weapon.getSpec().getHardpointAngleOffsets().set(i,angle);
                weapon.getSpec().getTurretAngleOffsets().set(i,angle);
            }
        }        
    }
    
    private void disable(CombatEngineAPI engine, WeaponAPI weapon){
        engine.spawnExplosion(weapon.getLocation(), new Vector2f(), Color.gray, 50, 1);
        engine.addHitParticle(weapon.getLocation(), new Vector2f(), 100, 1, 0.35f, Color.blue);
        engine.addHitParticle(weapon.getLocation(), new Vector2f(), 50, 1, 0.1f, Color.white);       
        weapon.getSprite().setSize(0, 0);
    }
}
