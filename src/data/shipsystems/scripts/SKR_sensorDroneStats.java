package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SKR_sensorDroneStats extends BaseShipSystemScript {

    private final float SENSOR_RANGE_PERCENT = 25f;
    private final float WEAPON_RANGE_PERCENT = 15f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float weaponRangePercent = WEAPON_RANGE_PERCENT * effectLevel;
        stats.getSightRadiusMod().modifyPercent(id, sensorRangePercent);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().unmodify(id);
        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float weaponRangePercent = WEAPON_RANGE_PERCENT * effectLevel;
        if (index == 0) {
            return new StatusData("sensor range +" + (int) sensorRangePercent + "%", false);
        } else if (index == 1) {
            return new StatusData("weapon range +" + (int) weaponRangePercent + "%", false);
        }
        return null;
    }
}
