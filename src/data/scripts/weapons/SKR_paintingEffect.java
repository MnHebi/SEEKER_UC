package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.FastTrig;

public class SKR_paintingEffect implements BeamEffectPlugin {
    
    private float time=0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {

        if (engine.isPaused()) {
            return;
        }
        
        time+=amount;
        beam.setWidth((float)(FastTrig.sin(time*60)+1)*4);
        
        if (beam.getBrightness()==1 && beam.didDamageThisFrame()){
            if (beam.getDamageTarget() instanceof ShipAPI ){
                ShipAPI theTarget = (ShipAPI) beam.getDamageTarget();
                ((SKR_painterEffect) beam.getWeapon().getEffectPlugin()).putTARGET(theTarget);
            }
        }
    }
}