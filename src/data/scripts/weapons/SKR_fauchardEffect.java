package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;

public class SKR_fauchardEffect implements BeamEffectPlugin {

    private final IntervalUtil fireInterval = new IntervalUtil(0.2f, 0.3f);
    private final IntervalUtil effectInterval = new IntervalUtil(0.05f, 0.2f);
    private boolean wasZero = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        
        CombatEntityAPI target = beam.getDamageTarget();
        
        effectInterval.advance(amount);
        if(effectInterval.intervalElapsed()){
            //glow
            if(MagicRender.screenCheck(1, beam.getFrom())){
                engine.addHitParticle(
                        beam.getFrom(),
                        beam.getSource().getVelocity(),
                        beam.getBrightness()*100+(float)Math.random()*100f,
                        0.5f,
                        (float)Math.random()*0.25f,
                        beam.getFringeColor()
                );
                engine.addHitParticle(
                        MathUtils.getRandomPointInCircle(beam.getFrom(),10),
                        beam.getSource().getVelocity(),
                        beam.getBrightness()*50+(float)Math.random()*50f,
                        0.5f,
                        (float)Math.random()*0.25f,
                        Color.WHITE
                );
            }
            
            //impact
            if(target!=null){
                if(MagicRender.screenCheck(1, beam.getTo())){
                    
                    engine.addHitParticle(
                            beam.getTo(),
                            new Vector2f(),
                            beam.getBrightness()*100+(float)Math.random()*200f,
                            1f,
                            (float)Math.random()*0.3f,
                            beam.getFringeColor()
                    );
                    
                    engine.addHitParticle(
                            MathUtils.getRandomPointInCircle(beam.getTo(),30),
                            new Vector2f(),
                            beam.getBrightness()*25+(float)Math.random()*25f,
                            1f,
                            (float)Math.random()*0.1f,
                            Color.WHITE
                    );
                    
                    MagicRender.battlespace(
                            Global.getSettings().getSprite("fx", "SKR_plasma_sparks"),
                            MathUtils.getRandomPointInCircle(beam.getTo(), 20),
                            new Vector2f(MathUtils.getRandomPointInCone(new Vector2f(), 25, beam.getWeapon().getCurrAngle()+150, beam.getWeapon().getCurrAngle()+210)),
                            new Vector2f(32,32),
                            new Vector2f(512,512),
                            beam.getWeapon().getCurrAngle()+MathUtils.getRandomNumberInRange(75f, 115f),
                            0,
                            beam.getFringeColor(),
                            true,
                            0f,
                            0.05f,
                            0.1f);
                }
            }
        }
        
        //arcs
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            float dur = beam.getDamage().getDpsDuration();
            // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
            if (!wasZero) {
                dur = 0;
            }
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            fireInterval.advance(dur);
            if (fireInterval.intervalElapsed()) {
                ShipAPI ship = (ShipAPI) target;
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
                float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
                pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);

                boolean piercedShield = hitShield && (float) Math.random() < pierceChance;
                //piercedShield = true;

                if (!hitShield || piercedShield) {
                    Vector2f dir = Vector2f.sub(beam.getTo(), beam.getFrom(), new Vector2f());
                    if (dir.lengthSquared() > 0) {
                        dir.normalise();
                    }
                    dir.scale((float)Math.random()*200);
                    Vector2f point = Vector2f.sub(beam.getTo(), dir, new Vector2f());
                    float emp = beam.getWeapon().getDamage().getFluxComponent() * 0.5f;
                    float dam = beam.getWeapon().getDamage().getDamage() * 0.25f;
                    engine.spawnEmpArcPierceShields(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam, // damage
                            emp, // emp 
                            100000f, // max range 
                            "tachyon_lance_emp_impact",
                            beam.getWidth() + 5f,
                            beam.getFringeColor(),
                            beam.getCoreColor()
                    );
                }
            }
        }
    }
}
