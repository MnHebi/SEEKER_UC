package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.magiclib.util.MagicAnim;
import static data.scripts.util.SKR_txt.txt;
//import org.lwjgl.util.vector.Vector2f;

public class SKR_alcubiereStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

//        stats.getAcceleration().modifyMult(id, 0);
//        stats.getDeceleration().modifyMult(id, 0);
        stats.getTurnAcceleration().modifyMult(id, 20f);
        stats.getMaxTurnRate().modifyFlat(id, MagicAnim.smoothReturnNormalizeRange(effectLevel, 0, 1)*180f);
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
//        stats.getAcceleration().unmodify(id);
//        stats.getDeceleration().unmodify(id);
        
        //clamp mobility
        float turning = stats.getMaxTurnRate().getBaseValue();
        stats.getEntity().setAngularVelocity(Math.min(turning, Math.max(-turning, stats.getEntity().getAngularVelocity())));
//        Vector2f velocity = stats.getEntity().getVelocity();
//        velocity.set(0,0);
    }
	
    private final String TRANSLATION = txt("translation");
    private final String REPULSION = txt("repulsion");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(TRANSLATION, false);
        } else if (index == 1) {
            return new StatusData(REPULSION, false);
        }
        return null;
    }
}
