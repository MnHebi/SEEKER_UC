
package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.magiclib.util.MagicAnim;
import static data.scripts.util.SKR_txt.txt;

public class SKR_ramStats extends BaseShipSystemScript {

    private final String IN="IN";
    private final String ACTIVE="ACTIVE";
    private final String OUT="OUT";
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        switch (state){
            case IN:
                //slow down
                stats.getMaxSpeed().modifyMult(id+IN, 1-effectLevel);
                //aim the ship
                stats.getTurnAcceleration().modifyPercent(id, 100+900*effectLevel);
                stats.getMaxTurnRate().modifyPercent(id, 500*MagicAnim.smoothReturnNormalizeRange(effectLevel, 0, 1));
                //little damage
                stats.getArmorDamageTakenMult().modifyMult(id, 1-0.9f*effectLevel);
                stats.getHullDamageTakenMult().modifyMult(id, 1-0.9f*effectLevel);
                break;
            case ACTIVE:
                stats.getMaxSpeed().unmodify(id+IN);
                stats.getMaxTurnRate().unmodify(id+IN);
                stats.getTurnAcceleration().unmodify(id);
                //charge forward
                stats.getMaxSpeed().modifyFlat(id, 510);
                stats.getAcceleration().modifyPercent(id, 2000);
                //no turning
                stats.getMaxTurnRate().modifyMult(id, 0);
                //little damage
                stats.getArmorDamageTakenMult().modifyMult(id, 0.1f);
                stats.getHullDamageTakenMult().modifyMult(id, 0.1f);
                break;
            case OUT:
                //regain maneuverability
                stats.getMaxTurnRate().modifyMult(id, effectLevel);
                //end invulnerability
                stats.getArmorDamageTakenMult().unmodify(id);
                stats.getHullDamageTakenMult().unmodify(id);
                //slowdown
                stats.getMaxSpeed().unmodify(id);
                stats.getAcceleration().unmodify(id);
                stats.getDeceleration().modifyPercent(id, 1000*effectLevel);
                //little damage
                stats.getArmorDamageTakenMult().modifyMult(id, 1-0.9f*effectLevel);
                stats.getHullDamageTakenMult().modifyMult(id, 1-0.9f*effectLevel);
                break;
        }
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getMaxSpeed().unmodify(id+IN);
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id+IN);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
    }

    private final String ENGINE = txt("engine");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(ENGINE, false);
        }
        return null;
    }
}