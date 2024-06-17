package data.campaign.abilities;

import com.fs.starfarer.api.Global;
import java.awt.Color;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.CRRecoveryBuff;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;
import java.util.ArrayList;
import java.util.List;

public class SKR_emergencyBurnAbility extends BaseDurationAbility {

    private final float SENSOR_RANGE_MULT = 0.5f;
    private final float DETECTABILITY_PERCENT = 50f;
    private final float MAX_BURN_MOD = 8f;
    private final float CR_COST_MULT = 0.25f;
    private final float FUEL_USE_MULT = 1f;
    private final float ACCELERATION_MULT = 4f;
    private final float ACTIVATION_DAMAGE_PROB = 0.33f;

    @Override
    protected void activateImpl() {

        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);
        if (crCostFleetMult > 0) {
            for (FleetMemberAPI member : getNonReadyShips()) {
                if ((float) Math.random() < ACTIVATION_DAMAGE_PROB) {
                    Misc.applyDamage(member, null, Misc.FleetMemberDamageLevel.LOW, false, null, null,
                            true, null, member.getShipName() + txt("EEB_damage"));
                }
            }
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                float crLoss = member.getDeployCost() * CR_COST_MULT * crCostFleetMult;
                member.getRepairTracker().applyCREvent(-crLoss, txt("EEB_id"));
            }
        }

        float cost = computeFuelCost();
        fleet.getCargo().removeFuel(cost);
    }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        fleet.getStats().getSensorRangeMod().modifyMult(getModId(), 1f + (SENSOR_RANGE_MULT - 1f) * level, txt("EEB_id"));
        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, txt("EEB_id"));
        fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(getModId(), (int) (MAX_BURN_MOD * level), txt("EEB_id"));
        fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (ACCELERATION_MULT - 1f) * level);
        fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyMult(getModId(), 0f);
//        fleet.getCommanderStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(getModId(), 0f);
//        fleet.getStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(getModId(), 0f);
//        fleet.getStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyMult(getModId(), 0f);
        
        for(FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
            if(m.isFighterWing())continue;
            m.getStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(getModId(), 0f);
        }

        for (FleetMemberViewAPI view : fleet.getViews()) {
            view.getContrailColor().shift(getModId(), new Color(250, 150, 100, 255), 1f, 1f, .75f);
            view.getEngineGlowSizeMult().shift(getModId(), 2f, 1f, 1f, 1f);
            view.getEngineHeightMult().shift(getModId(), 5f, 1f, 1f, 1f);
            view.getEngineWidthMult().shift(getModId(), 3f, 1f, 1f, 1f);
        }

        String buffId = getModId();
        float buffDur = 0.1f;
        boolean needsSync = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (level <= 0) {
                member.getBuffManager().removeBuff(buffId);
                needsSync = true;
            } else {
                BuffManagerAPI.Buff test = member.getBuffManager().getBuff(buffId);
                if (test instanceof CRRecoveryBuff) {
                    CRRecoveryBuff buff = (CRRecoveryBuff) test;
                    buff.setDur(buffDur);
                } else {
                    member.getBuffManager().addBuff(new CRRecoveryBuff(buffId, 0f, buffDur));
                    needsSync = true;
                }
            }
        }
        if (needsSync) {
            fleet.forceSync();
        }
    }

    @Override
    protected void deactivateImpl() {
        cleanupImpl();
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        fleet.getStats().getSensorRangeMod().unmodify(getModId());
        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
        fleet.getStats().getAccelerationMult().unmodify(getModId());
        fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify(getModId());
//        fleet.getCommanderStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).unmodify(getModId());
//        fleet.getStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify(getModId());
//        fleet.getStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).unmodify(getModId());
        
        for(FleetMemberAPI m : fleet.getMembersWithFightersCopy()){
            if(m.isFighterWing())continue;
            m.getStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).unmodify(getModId());
        }
    }

    private List<FleetMemberAPI> getNonReadyShips() {
        List<FleetMemberAPI> result = new ArrayList<>();
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return result;
        }

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            float crLoss = getCRCost(member, fleet);
            if (Math.round(member.getRepairTracker().getCR() * 100) < Math.round(crLoss * 100)) {
                result.add(member);
            }
        }
        return result;
    }

    private float getCRCost(FleetMemberAPI member, CampaignFleetAPI fleet) {
        float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);
        float crLoss = member.getDeployCost() * CR_COST_MULT * crCostFleetMult;
        return Math.round(crLoss * 100f) / 100f;
    }

    private float computeFuelCost() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return 0f;
        }

        float cost = fleet.getLogistics().getFuelCostPerLightYear() * FUEL_USE_MULT;
        return cost;
    }

    private float computeSupplyCost() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return 0f;
        }

        float crCostFleetMult = fleet.getStats().getDynamic().getValue(Stats.EMERGENCY_BURN_CR_MULT);

        float cost = 0f;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            cost += member.getDeploymentPointsCost() * CR_COST_MULT * crCostFleetMult;
        }
        return cost;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();
        Color fuel = Global.getSettings().getColor("progressBarFuelColor");
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle(spec.getName());

        float pad = 10f;

        float fuelCost = computeFuelCost();
        float supplyCost = computeSupplyCost();

        tooltip.addPara(txt("EEB_tt1"),
                pad,
                highlight,
                "" + (int) MAX_BURN_MOD,
                "" + (int) ((1f - SENSOR_RANGE_MULT) * 100f) + txt("%"),
                "" + (int) (DETECTABILITY_PERCENT) + txt("%")
        );
        
        tooltip.addPara(txt("EEB_tt2"),
                pad,
                highlight,
                txt("EEB_tt2h")
        );
        
        
        if (supplyCost > 0) {
            tooltip.addPara(txt("EEB_tt3a"), pad,
                    highlight,
                    Misc.getRoundedValueMaxOneAfterDecimal(fuelCost),
                    Misc.getRoundedValueMaxOneAfterDecimal(supplyCost));
        } else {
            tooltip.addPara(txt("EEB_tt3b"), pad,
                    highlight,
                    Misc.getRoundedValueMaxOneAfterDecimal(fuelCost));
        }

        if (fuelCost > fleet.getCargo().getFuel()) {
            tooltip.addPara(txt("EEB_tt4"), bad, pad);
        }

        List<FleetMemberAPI> nonReady = getNonReadyShips();
        if (!nonReady.isEmpty()) {
            tooltip.addPara(txt("EEB_tt5"), pad,
                    Misc.getNegativeHighlightColor(),
                    txt("EEB_tt5h")
            );
            int j = 0;
            int max = 7;
            float initPad = 5f;
            for (FleetMemberAPI member : nonReady) {
                if (j >= max) {
                    if (nonReady.size() > max + 1) {
                        tooltip.addToGrid(0, j++, txt("EEB_tt6a"), "", bad);
                        break;
                    }
                }

                String str = "";
                if (!member.isFighterWing()) {
                    str += member.getShipName() + txt("EEB_tt6b");
                    str += member.getHullSpec().getHullNameWithDashClass();
                } else {
                    str += member.getVariant().getFullDesignationWithHullName();
                }

                tooltip.addPara(BaseIntelPlugin.INDENT + str, initPad);
                initPad = 0f;
            }
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
        if (engagedInHostilities) {
            deactivate();
        }
    }

    @Override
    public void fleetOpenedMarket(MarketAPI market) {
        deactivate();
    }

    protected boolean showAlarm() {
        return !getNonReadyShips().isEmpty() && !isOnCooldown() && !isActiveOrInProgress() && isUsable();
    }

    @Override
    public boolean isUsable() {
        return super.isUsable()
                && getFleet() != null
                && (getFleet().isAIMode() || computeFuelCost() <= getFleet().getCargo().getFuel());
    }

    @Override
    public float getCooldownFraction() {
        if (showAlarm()) {
            return 0f;
        }
        return super.getCooldownFraction();
    }

    @Override
    public boolean showCooldownIndicator() {
        return super.showCooldownIndicator();
    }

    @Override
    public boolean isOnCooldown() {
        return super.getCooldownFraction() < 1f;
    }

    @Override
    public Color getCooldownColor() {
        if (showAlarm()) {
            Color color = Misc.getNegativeHighlightColor();
            return Misc.scaleAlpha(color, Global.getSector().getCampaignUI().getSharedFader().getBrightness() * 0.5f);
        }
        return super.getCooldownColor();
    }

    @Override
    public boolean isCooldownRenderingAdditive() {
        if (showAlarm()) {
            return true;
        }
        return false;
    }
}
