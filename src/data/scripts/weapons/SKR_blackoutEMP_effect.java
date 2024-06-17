package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class SKR_blackoutEMP_effect implements OnHitEffectPlugin {

    private final Color CORE_COLOR = new Color(255,175,255,255);
    private final Color FRINGE_COLOR = new Color(255,150,255,200);
    private Integer arcs = 4;
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        //ignore non ship targets
        if(target instanceof ShipAPI){
            
            //sound
            Global.getSoundPlayer().playSound("system_emp_emitter_impact", 1f, 1, point, target.getVelocity());
            
            ShipAPI theTarget=(ShipAPI)target;
        
            boolean module = false;
            if(theTarget.getParentStation()!=null){
                module=true;
            }
            //check for templar hull because their special shield isn't recognized as such
            if (theTarget.getHullSpec().getHullId().startsWith("tem_")){  
                arcs=(int)(arcs*0.5f);                    
            }

            if(shieldHit){
                //shield hit: EMP with reduced number of arcs dependent on hard flux
                Float hardflux = theTarget.getFluxTracker().getHardFlux()/theTarget.getFluxTracker().getMaxFlux();
                for(int i=0; i<arcs*hardflux; i++){
                    engine.spawnEmpArcPierceShields(
                            projectile.getSource(),
                            point,
                            target,
                            target,
                            DamageType.KINETIC,
                            0,
                            projectile.getEmpAmount()/2,
                            250, 
                            null, 
                            5,
                            FRINGE_COLOR,
                            CORE_COLOR
                    );
                    engine.addFloatingDamageText(point, projectile.getEmpAmount()/2, Color.CYAN, target, projectile.getSource());
                }
            } else        
            if(!module){
                //hull hit
                for(int i=0; i<arcs; i++){                
                    engine.spawnEmpArc(
                            projectile.getSource(),
                            point,
                            target,
                            target,
                            DamageType.KINETIC,
                            0,
                            projectile.getEmpAmount()/2,
                            250, 
                            null, 
                            10,
                            FRINGE_COLOR,
                            CORE_COLOR
                    );
                    engine.addFloatingDamageText(point, projectile.getEmpAmount()/2, Color.CYAN, target, projectile.getSource());
                }
            } else 
            if(module){    
                //module hit: half the arcs to the module for full EMP, half the arcs to the parent ship for half EMP
                for(int i=0; i<arcs/2; i++){                
                    engine.spawnEmpArc(
                            projectile.getSource(),
                            point,
                            target,
                            target,
                            DamageType.KINETIC,
                            0,
                            projectile.getEmpAmount()/2,
                            250, 
                            null, 
                            10,
                            FRINGE_COLOR,
                            CORE_COLOR
                    );
                    engine.addFloatingDamageText(point, projectile.getEmpAmount()/2, Color.CYAN, target, projectile.getSource());
                }   
                for(int i=0; i<arcs/2; i++){                
                    engine.spawnEmpArc(
                            projectile.getSource(),
                            point,
                            theTarget.getParentStation(),
                            theTarget.getParentStation(),
                            DamageType.KINETIC,
                            0,
                            projectile.getEmpAmount()/4,
                            500, 
                            null, 
                            5,
                            FRINGE_COLOR,
                            CORE_COLOR
                    );
                    engine.addFloatingDamageText(point, projectile.getEmpAmount()/4, Color.CYAN, target, projectile.getSource());
                }
            }
        }
    }
}

