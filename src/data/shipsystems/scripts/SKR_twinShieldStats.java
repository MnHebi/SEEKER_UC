package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.SKR_txt.txt;

public class SKR_twinShieldStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getShieldDamageTakenMult().modifyMult(id, 1-(effectLevel/2));
//        stats.getShieldTurnRateMult().modifyMult(id, 4*effectLevel);
        stats.getShieldTurnRateMult().modifyPercent(id, 300*effectLevel);
    }
    
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getShieldTurnRateMult().unmodify(id);
    }
    
    
    private final String PLUS = txt("+");
    private final String MINUS = txt("-");
    private final String DAMAGE = txt("damage");
    private final String ARC = txt("arc");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(PLUS+(int)Math.round(50*effectLevel)+DAMAGE, false);
        }
        if (index == 1) {
            return new StatusData(MINUS+(int)Math.round(50*effectLevel)+ARC, false);
        }
        return null;
    }
}