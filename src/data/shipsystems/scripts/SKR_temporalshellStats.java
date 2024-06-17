package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;

public class SKR_temporalshellStats extends BaseShipSystemScript {
    private final float MAX_TIME_MULT = 3f;

    private final Color JITTER_COLOR = new Color(165,100,255,55);
    private final Color JITTER_UNDER_COLOR = new Color(165,0,90,128);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }

        float jitterLevel = effectLevel;
        float jitterRangeBonus = 0;
        float maxRangeBonus = 10f;
        if (state == State.IN) {
            jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
            if (jitterLevel > 1) {
                jitterLevel = 1f;
            }
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        } else if (state == State.ACTIVE) {
            jitterLevel = 1f;
            jitterRangeBonus = maxRangeBonus;
        } else if (state == State.OUT) {
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        }
        jitterLevel = (float) Math.sqrt(jitterLevel);
        effectLevel *= effectLevel;

        ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 10, 0f, 7f + jitterRangeBonus);

        float fluxTimeMult = MAX_TIME_MULT + MAX_TIME_MULT*ship.getFluxLevel(); //flux dependent time mult
        
        float shipTimeMult = 1f + (fluxTimeMult - 1f) * effectLevel;
        float playerTimeMult = 1f + ((fluxTimeMult/2) - 1f) * effectLevel;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, (1f / playerTimeMult));
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }

        ship.getEngineController().fadeToOtherColor(this, JITTER_UNDER_COLOR, Color.BLACK, effectLevel, 0.5f);
        ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);
    }


    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);
    }

    private final String TIME = txt("time");
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        
//        float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
//        float playerTimeMult = 1f + ((MAX_TIME_MULT/2) - 1f) * effectLevel;
        if (index == 0) {
            return new StatusData(TIME, false);
        } 
//        else if (index == 1) {
//            return new StatusData("Ship time = "+ Math.round(100 * shipTimeMult)+"%", false);
//        } else if (index == 2) {
//            return new StatusData("Player time = "+ Math.round(100 / playerTimeMult)+"%", false);
//        } else if (index == 3) {
//            return new StatusData("Global time = "+ Math.round(100 * Global.getCombatEngine().getTimeMult().computeMultMod())+"%", true);
//        }
        return null;
    }
}