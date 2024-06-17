package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;

public class SKR_shieldUpgrade extends BaseHullMod {
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        
        ShieldAPI shield = ship.getShield();
        if (shield == null) {
                ship.setShield(ShieldAPI.ShieldType.OMNI, 0.4f, 1f, 90);
        }
    }
    
    @Override
    public int getDisplaySortOrder() {
        return 2000;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }
    
    private final Color BAD=Misc.getNegativeHighlightColor();
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
	if(ship.getHullSpec().getBaseHullId().startsWith("SKR_siegfried")){
            //title
            tooltip.addSectionHeading(txt("TTIP_sieg0"), Alignment.MID, 15); //"WARNING"

            //total effect
            tooltip.addPara(
                    txt("TTIP_sieg1")
                    + txt("TTIP_sieg2")
                    + txt("TTIP_sieg3"),
                    10,
                    BAD,
                    txt("TTIP_sieg2"));
        }
    }
}
