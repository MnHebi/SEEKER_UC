package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.magiclib.util.MagicLensFlare;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SKR_chain_hitEffect implements OnHitEffectPlugin {
    
    private final Color COLOR = new Color(250,55,200,255);
    
    private final Map<ShipAPI.HullSize, Integer> MULT = new HashMap<>();
    {
        MULT.put(ShipAPI.HullSize.DEFAULT, 1);
        MULT.put(ShipAPI.HullSize.FIGHTER, 1);
        MULT.put(ShipAPI.HullSize.FRIGATE, 2);
        MULT.put(ShipAPI.HullSize.DESTROYER, 3);
        MULT.put(ShipAPI.HullSize.CRUISER, 4);
        MULT.put(ShipAPI.HullSize.CAPITAL_SHIP, 5);
    }
    
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
                
        //sound
        Global.getSoundPlayer().playSound("SKR_chain_hit", 1, 1, point, target.getVelocity());
        
        //visual
        if(MagicRender.screenCheck(1, point)){
            engine.addSmoothParticle(
                    point,
                    new Vector2f(),
                    800,
                    1,
                    0.05f,
                    Color.WHITE
            );
            
//            for(int i=0; i<6; i++){
//                engine.addSmoothParticle(
//                        point,
//                        new Vector2f(),
//                        400,
//                        1,
//                        0.5f,
//                        Color.WHITE
//                );
//            }
            for(int i=0; i<20; i++){
                Vector2f loc = MathUtils.getRandomPointInCircle(new Vector2f(), 75);
                engine.addHitParticle(
                        new Vector2f(point.x+loc.x,point.y+loc.y),
                        loc,
                        5+5*(float)Math.random(),
                        1,
                        1+2*(float)Math.random(),
                        COLOR
                );
            }
            engine.addHitParticle(
                    point,
                    new Vector2f(),
                    500,
                    0.5f,
                    2f,
                    COLOR
            );
            
            for(int i=0; i<6; i++){
                MagicLensFlare.createSharpFlare(engine, projectile.getSource(), MathUtils.getRandomPointInCircle(point, 100), 5+5*(float)Math.random(), 250+250*(float)Math.random(), 0, COLOR, Color.white);
            }
            MagicLensFlare.createSharpFlare(engine, projectile.getSource(), point, 30, 600, 0, COLOR, Color.white);
        }

        //EMP
        
        ShipAPI source = projectile.getSource();
        CombatEntityAPI from=target;
//        CombatEntityAPI newTarget=target;
//        int mult=-30;
        List<CombatEntityAPI> nodes=new ArrayList<>();
        nodes.add(target);        
        
        List<ShipAPI> selCap=new ArrayList<>();
        List<ShipAPI> selCrui=new ArrayList<>();
        List<ShipAPI> selDest=new ArrayList<>();
        List<ShipAPI> selFrig=new ArrayList<>();
        List<ShipAPI> selChaff=new ArrayList<>();
        
        for(ShipAPI s : CombatUtils.getShipsWithinRange(point, 1000)){
            if(s.isAlive() && !s.isPhased() && s.getOriginalOwner()!=source.getOriginalOwner() && s!=target){
                if(s.isCapital()){
                    selCap.add(s);
                } else if(s.isCruiser()){
                    selCrui.add(s);
                } else if(s.isDestroyer()){
                    selDest.add(s);
                } else if(s.isFrigate()){
                    selFrig.add(s);
                } else {
                    selChaff.add(s);
                }
            }
        }
        if(selCap.size()>1){
            Collections.shuffle(selCap);
        }
        if(selCrui.size()>1){
            Collections.shuffle(selCrui);
        }
        if(selDest.size()>1){
            Collections.shuffle(selDest);
        }
        if(selFrig.size()>1){
            Collections.shuffle(selFrig);
        }
        if(selChaff.size()>1){
            Collections.shuffle(selChaff);
        }
        
        //FIRST IMPACT
        int num=1;
        if(target instanceof ShipAPI){
            num = MULT.get(((ShipAPI)target).getHullSize());
        }
        arcing(engine,num,target,point,target,source);
        arcing(engine,num,new SimpleEntity(point),projectile.getWeapon().getLocation(),source,source);
        
        //CHAIN
        if(!selCap.isEmpty()){
            for(ShipAPI s : selCap){
                //basic tree node calculation
                float dist=999999999;
                for(CombatEntityAPI n : nodes){                    
                    float nDist=MathUtils.getDistanceSquared(n.getLocation(), s.getLocation());                    
                    if(nDist<dist){
                        from=n;
                        dist=nDist;
                    }
                }          
                //frigates and up can become arching nodes
                nodes.add(s);            
                //arcs
                arcing(engine, MULT.get(HullSize.CAPITAL_SHIP), s, from.getLocation(), from, source);
            }
        }
        
        if(!selCrui.isEmpty()){
            for(ShipAPI s : selCrui){
                //basic tree node calculation
                float dist=999999999;
                for(CombatEntityAPI n : nodes){                    
                    float nDist=MathUtils.getDistanceSquared(n.getLocation(), s.getLocation());                    
                    if(nDist<dist){
                        from=n;
                        dist=nDist;
                    }
                }          
                //frigates and up can become arching nodes
                nodes.add(s);            
                //arcs
                arcing(engine, MULT.get(HullSize.CRUISER), s, from.getLocation(), from, source);
            }
        }
        
        if(!selDest.isEmpty()){
            for(ShipAPI s : selDest){
                //basic tree node calculation
                float dist=999999999;
                for(CombatEntityAPI n : nodes){                    
                    float nDist=MathUtils.getDistanceSquared(n.getLocation(), s.getLocation());                    
                    if(nDist<dist){
                        from=n;
                        dist=nDist;
                    }
                }          
                //frigates and up can become arching nodes
                nodes.add(s);            
                //arcs
                arcing(engine, MULT.get(HullSize.DESTROYER), s, from.getLocation(), from, source);
            }
        }
        
        if(!selFrig.isEmpty()){
            for(ShipAPI s : selFrig){
                //basic tree node calculation
                float dist=999999999;
                for(CombatEntityAPI n : nodes){                    
                    float nDist=MathUtils.getDistanceSquared(n.getLocation(), s.getLocation());                    
                    if(nDist<dist){
                        from=n;
                        dist=nDist;
                    }
                }                 
                //frigates and up can become arching nodes
                nodes.add(s);            
                //arcs
                arcing(engine, MULT.get(HullSize.FRIGATE), s, from.getLocation(), from, source);
            }
        }
        
        if(!selChaff.isEmpty()){
            for(ShipAPI s : selChaff){
                //basic tree node calculation
                float dist=999999999;
                for(CombatEntityAPI n : nodes){                    
                    float nDist=MathUtils.getDistanceSquared(n.getLocation(), s.getLocation());                    
                    if(nDist<dist){
                        from=n;
                        dist=nDist;
                    }
                }          
                //arcs
                arcing(engine, MULT.get(HullSize.FIGHTER), s, from.getLocation(), from, source);
            }
        }
    }
    
    private void arcing(CombatEngineAPI engine, Integer num, CombatEntityAPI target, Vector2f from, CombatEntityAPI anchor, ShipAPI source){
        for(int i=0; i<num; i++){
            Color color=new Color(255-(25*i),55,200);
            if(!(target instanceof ShipAPI) || Math.random()<((ShipAPI)target).getHardFluxLevel()+0.1f){
                engine.spawnEmpArcPierceShields(
                        source,
                        from,
                        anchor,
                        target,
                        DamageType.ENERGY,
                        500, 
                        1000,
                        10000,
                        "tachyon_lance_emp_impact",
                        i*3+2+2*num,
                        color,                                    
                        Color.WHITE
                );
            }else{
                engine.spawnEmpArc(
                        source,
                        from,
                        anchor,
                        target,
                        DamageType.ENERGY,
                        500, 
                        1000,
                        10000,
                        "tachyon_lance_emp_impact",
                        i*2+2+num,
                        color,                                    
                        Color.WHITE
                );
            }
            //debug
//            engine.addSmokeParticle(target.getLocation(), target.getVelocity(), target.getCollisionRadius()/4, 1, 1, Color.RED);
        }
        if(MagicRender.screenCheck(1, from)){
            MagicLensFlare.createSharpFlare(engine, source, from, 5+num, 350+35*num, 0, new Color(250,55,200), Color.white);
        }
    }
    
}
