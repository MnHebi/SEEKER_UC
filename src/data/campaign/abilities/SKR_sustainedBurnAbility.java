package data.campaign.abilities;

import com.fs.starfarer.api.Global;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;

public class SKR_sustainedBurnAbility extends BaseToggleAbility {

    private final float DETECTABILITY_PERCENT = 100f;
    private final float MAX_BURN_PERCENT = 100f;
    private final float ACCELERATION_MULT = 0.2f;
    private final float TERRAIN_EFFECT_MULT = 0.5f;

    @Override
    protected String getActivationText() {
        return super.getActivationText();
    }

    @Override
    protected void activateImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        if (!fleet.getMemoryWithoutUpdate().is("$sb_active", true)) {
            fleet.setVelocity(0, 0);
        }
    }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        if (level > 0 && fleet.getCargo().getFuel() <= 0
                && fleet.getContainingLocation() != null && fleet.getContainingLocation().isHyperspace()) {
            deactivate();
            return;
        }

        fleet.getMemoryWithoutUpdate().set("$sb_active", true, 0.3f);
        //prevent asteroids impacts
	fleet.getMemoryWithoutUpdate().set("$asteroidImpactTimeout", true, 0.3f);

        if (level > 0 && level < 1 && amount > 0) {
            float activateSeconds = getActivationDays() * Global.getSector().getClock().getSecondsPerDay();
            float speed = fleet.getVelocity().length();
            float acc = Math.max(speed, 200f) / activateSeconds + fleet.getAcceleration();
            float ds = acc * amount;
            if (ds > speed) {
                ds = speed;
            }
            Vector2f dv = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(fleet.getVelocity()));
            dv.scale(ds);
            fleet.setVelocity(fleet.getVelocity().x - dv.x, fleet.getVelocity().y - dv.y);
            return;
        }

        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, txt("ESB_id"));
        fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).modifyMult(getModId(), TERRAIN_EFFECT_MULT, txt("ESB_id"));

        int burnModifier = 0;
        float burnMult = 1f;

        float b = fleet.getStats().getDynamic().getValue(Stats.SUSTAINED_BURN_BONUS, 0f);
        burnModifier = (int) ((b) * level);

        fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(getModId(), burnModifier, txt("ESB_id"));
        fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), burnMult, txt("ESB_id"));
        fleet.getStats().getFleetwideMaxBurnMod().modifyPercent(getModId(), MAX_BURN_PERCENT, txt("ESB_id"));

        float accImpact = 0f;
        float burn = Misc.getBurnLevelForSpeed(fleet.getVelocity().length());
        if (burn > 1) {
            float dir = Misc.getDesiredMoveDir(fleet);
            float velDir = Misc.getAngleInDegrees(fleet.getVelocity());
            float diff = Misc.getAngleDiff(dir, velDir);
            float pad = 120f;
            diff -= pad;
            if (diff < 0) {
                diff = 0;
            }
            accImpact = 1f - 0.5f * Math.min(1f, (diff / (180f - pad)));
        }

        fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f - (1f - ACCELERATION_MULT) * accImpact);

        for (FleetMemberViewAPI view : fleet.getViews()) {
            view.getContrailColor().shift(getModId(), view.getEngineColor().getBase(), 1f, 1f, 0.5f * level);
            view.getEngineGlowSizeMult().shift(getModId(), 1.5f, 1f, 1f, 1f * level);
            view.getEngineHeightMult().shift(getModId(), 3f, 1f, 1f, 1f * level);
            view.getEngineWidthMult().shift(getModId(), 2f, 1f, 1f, 1f * level);
        }

        if (level <= 0) {
            cleanupImpl();
        }
    }

    @Override
    protected void deactivateImpl() {
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
        fleet.getStats().getAccelerationMult().unmodify(getModId());
        fleet.getCommanderStats().getDynamic().getStat(Stats.NAVIGATION_PENALTY_MULT).unmodify(getModId());
    }

    @Override
    public boolean showProgressIndicator() {
        return super.showProgressIndicator();
    }

    @Override
    public boolean showActiveIndicator() {
        return isActive();
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = txt("skill_off");
        if (turnedOn) {
            status = txt("skill_on");
        }

        LabelAPI title = tooltip.addTitle(spec.getName());
        title.highlightLast(status);
        title.setHighlightColor(gray);

        float pad = 10f;

        tooltip.addPara(txt("ESB_tt1"), pad);

        tooltip.addPara(txt("ESB_tt2"), pad,
                highlight,
                "" + (int) Math.round(MAX_BURN_PERCENT) + txt("%"),
                "" + (int) (DETECTABILITY_PERCENT) + txt("%")
        );
        
        tooltip.addPara(txt("ESB_tt3"), pad,
                highlight,
                "" + (int) (TERRAIN_EFFECT_MULT*100) + txt("%"),
                txt("ESB_tt3h")
        );

        tooltip.addPara(txt("ESB_tt4"), pad);

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
    }

    @Override
    public void fleetJoinedBattle(BattleAPI battle) {
        if (!battle.isPlayerInvolved()) {
            deactivate();
        }
    }
}
