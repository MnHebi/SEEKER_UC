package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.SKR_txt.txt;

public class SKR_fastShieldStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getShieldUnfoldRateMult().modifyPercent(id, 2000*effectLevel);
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldUnfoldRateMult().unmodify(id);
    }
    
    private final String UNFOLDING = txt("unfolding");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(UNFOLDING, false);                        
        }
        return null;
    }
}
