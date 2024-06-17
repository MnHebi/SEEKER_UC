package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SKR_empStats extends BaseShipSystemScript {
    
    private final float RANGE=750, DMG=100, EMP=500;
    private final String ACTIVE = txt("emp");
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        if(effectLevel==1 && !stats.getArmorDamageTakenMult().getMultMods().containsKey(id)){
            
            stats.getArmorDamageTakenMult().modifyMult(id, 1.5f);
            
            ShipAPI source = (ShipAPI)stats.getEntity();
            ShipAPI target = findTarget((ShipAPI)stats.getEntity());
            
            if(target==null){
                return;
            }
            
            List <WeaponSlotAPI> emitters = new ArrayList<>();
            for(WeaponSlotAPI s : source.getHullSpec().getAllWeaponSlotsCopy()){
                if(s.isSystemSlot()){
                    emitters.add(s);
                }
            }
            
            for (int i=0; i<emitters.size(); i++){
                if(Math.random()<target.getHardFluxLevel()){
                    Global.getCombatEngine().spawnEmpArcPierceShields(
                            source,
                            emitters.get(i).computePosition(source),
                            null,
                            target,
                            DamageType.ENERGY,
                            DMG,
                            EMP,
                            10000,
                            "system_emp_emitter_impact",
                            MathUtils.getRandomNumberInRange(2, 12),
                            new Color (
                                    MathUtils.getRandomNumberInRange(75, 100),
                                    MathUtils.getRandomNumberInRange(10, 30),
                                    MathUtils.getRandomNumberInRange(150, 200),
                                    200
                            ),
                            new Color (240,200,255,200)
                    );
                } else {
                    Global.getCombatEngine().spawnEmpArc(
                            source,
                            emitters.get(i).computePosition(source),
                            null,
                            target,
                            DamageType.ENERGY,
                            DMG,
                            EMP,
                            10000,
                            "system_emp_emitter_impact",
                            MathUtils.getRandomNumberInRange(2, 12),
                            new Color (
                                    MathUtils.getRandomNumberInRange(75, 100),
                                    MathUtils.getRandomNumberInRange(10, 30),
                                    MathUtils.getRandomNumberInRange(150, 200),
                                    200
                            ),
                            new Color (240,200,255,200)
                    );
                }
                
            }
        } 
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {        
        stats.getArmorDamageTakenMult().unmodify(id);
    }
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(ACTIVE, false);
        }
        return null;
    }

    private final String READY = txt("ready");
    private final String OUTOFRANGE = txt("range");
    private final String TARGET = txt("target");
    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI target = findTarget(ship);
        if (target != null && target != ship) {
            return READY;
        }
        
        if ((target == null || target == ship) && ship.getShipTarget() != null) {
            return OUTOFRANGE;
        }
        return TARGET;
    }
    
    protected ShipAPI findTarget(ShipAPI ship) {
        ShipAPI target = ship.getShipTarget();
        if(
                target!=null 
                && 
                (!target.isDrone()||!target.isFighter()) 
                && 
                MathUtils.isWithinRange(ship, target, RANGE)
                &&
                Math.abs(MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), target.getLocation())))<22.5
                ){
            return target;
        } else {
            return null;
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }
}