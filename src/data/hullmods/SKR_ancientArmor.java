package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.SKR_txt.txt;

public class SKR_ancientArmor extends BaseHullMod {

    private final float HE_BONUS=25;
    private final float EMP_BONUS=35;
    private final float ENERGY_MALUS=25;
    private final float KE_MALUS=50;
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, 1-(HE_BONUS/100));
        stats.getEmpDamageTakenMult().modifyMult(id, 1-(EMP_BONUS/100));

        stats.getEnergyDamageTakenMult().modifyPercent(id, ENERGY_MALUS);
        stats.getKineticDamageTakenMult().modifyPercent(id, KE_MALUS);
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return txt("-")+EMP_BONUS+txt("%");
        if (index == 1) return txt("-")+HE_BONUS+txt("%");
        if (index == 2) return txt("+")+ENERGY_MALUS+txt("%");
        if (index == 3) return txt("+")+KE_MALUS+txt("%");  
        return null;
    }
}