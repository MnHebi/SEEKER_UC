package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class SKR_spasmAI implements ShipSystemAIScript{
    private ShipAPI ship;
    private final IntervalUtil timer = new IntervalUtil(0.5f,2f);
    private final List<WeaponAPI> weapons = new ArrayList<>();
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine){
        this.ship = ship;
        for(WeaponAPI w : ship.getAllWeapons()){
            if(w!=null&&!w.isDecorative()&&!w.isPermanentlyDisabled()){
                weapons.add(w);
            }
        }
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){
        if (Global.getCombatEngine().isPaused() || ship.getShipAI()==null) {
            return;
        }
        timer.advance(amount);
        if(timer.intervalElapsed()){
            if(ship.getEngineController().isFlamedOut()){
                ship.useSystem();
            } else
            if(ship.getDisabledWeapons().size()>weapons.size()/3){
                ship.useSystem();
            } else {
                int trigger=0;
                for(FighterWingAPI w : ship.getAllWings()){
                    if(w.getWingMembers().size()<=1){
                        trigger++;
                    }
                }
                if(trigger>=3){
                    ship.useSystem();
                }
            }
        }
    }
}
