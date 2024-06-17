package data.campaign.abilities;

import com.fs.starfarer.api.Global;
import java.awt.Color;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SKR_remoteSurveyAbility extends BaseDurationAbility {

    public static final float SURVEY_RANGE = 999999f;
    public static final float DETECTABILITY_RANGE_BONUS = 5000f;
    public static final float ACCELERATION_MULT = 4f;
    
    private final float JUMP_POINT_DISTANCE=0.2f;

    protected boolean performed = false;

    @Override
    protected void activateImpl() {
        if (entity.isInCurrentLocation()) {
            SectorEntityToken.VisibilityLevel visibilityLevel = entity.getVisibilityLevelToPlayerFleet();
            if (visibilityLevel != SectorEntityToken.VisibilityLevel.NONE) {
                Global.getSector().addPing(entity, Pings.REMOTE_SURVEY);
            }
            performed = false;
        }
    }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), 1-level, txt("ERS_id"));
        fleet.getStats().getDetectedRangeMod().modifyFlat(getModId(), DETECTABILITY_RANGE_BONUS * level, txt("ERS_id"));
        fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (ACCELERATION_MULT - 1f) * level);

        if (!performed && level >= 1f) {

            boolean ruins = false;
            StarSystemAPI system;

            if (fleet.isInHyperspace()) {
                //hyperspace survey
                JumpPointAPI point = Misc.findNearestJumpPoint(fleet);
                system = point.getDestinations().get(0).getDestination().getStarSystem();
            } else {
                //system survey
                system = fleet.getStarSystem();
            }

            if (system != null) {
                for (SectorEntityToken e : system.getAllEntities()) {
                    //planet survey
                    if (e instanceof PlanetAPI) {
                        PlanetAPI planet = (PlanetAPI) e;
                        if(planet.getMarket()!=null && planet.getMarket().getSurveyLevel()!=null){
                            MarketAPI market = planet.getMarket();
                            MarketAPI.SurveyLevel surveyLevel = market.getSurveyLevel();
                            if ((surveyLevel != MarketAPI.SurveyLevel.SEEN && surveyLevel != MarketAPI.SurveyLevel.NONE)) {
                                continue;
                            }
                            Misc.setPreliminarySurveyed(market, null, true);
                        }
                        //ruins check
                        if (planet.hasTag(Tags.ORBITAL_JUNK)) {
                            ruins = true;
                        }
                    } else if (e instanceof CampaignFleetAPI
                            || e.hasDiscoveryXP()
                            || e.hasSalvageXP()
                            || e.hasTag(Tags.DEBRIS_FIELD)
                            || e.hasTag(Tags.WRECK)
                            || e.hasTag(Tags.STATION)
                            || e.hasTag(Tags.SALVAGEABLE)
                            || e.hasTag(Tags.CORONAL_TAP)
                            || e.hasTag(Tags.CRYOSLEEPER)) {
                        ruins = true;
                    }
                }
            }
            performed = true;

            //ruins message
            if (ruins) {
                fleet.addFloatingText(txt("ERS_ping"), Color.yellow, 2f);
            }
        } 
//        else if(performed && level <= 1f) {
//            deactivate();
//        }
    }

    @Override
    public boolean isUsable() {
        if (!super.isUsable()) {
            return false;
        }
        
        if (getFleet() == null) {
            return false;
        }

        CampaignFleetAPI fleet = getFleet();
        
        //always usable if already active so that it gets disabled properly
        if(fleet.getStats().hasMod(getModId()))return true;
        
        if (fleet.isInHyperspaceTransition()) {
            return false;
        }
        if (fleet.isInHyperspace()) {
            JumpPointAPI point = Misc.findNearestJumpPoint(fleet);
            if (Misc.getDistanceToPlayerLY(point) < JUMP_POINT_DISTANCE) {
                return true;
            }
        }

        return !getSurveyableInRange().isEmpty();
    }

    private List<PlanetAPI> getAllPlanetsInRange() {
        List<PlanetAPI> result = new ArrayList<>();

        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return result;
        }
        if (fleet.isInHyperspace()) {
            return result;
        }

        for (PlanetAPI planet : fleet.getContainingLocation().getPlanets()) {
            if (planet.isStar()) {
                continue;
            }
            if (planet.getMarket() == null) {
                continue;
            }

            float dist = Misc.getDistance(fleet.getLocation(), planet.getLocation());
            if (dist <= SURVEY_RANGE) {
                result.add(planet);
            }
        }
        return result;
    }

    private List<PlanetAPI> getSurveyableInRange() {
        List<PlanetAPI> result = getAllPlanetsInRange();

        Iterator<PlanetAPI> iter = result.iterator();
        while (iter.hasNext()) {
            PlanetAPI curr = iter.next();
            MarketAPI.SurveyLevel surveyLevel = curr.getMarket().getSurveyLevel();
            if (surveyLevel != MarketAPI.SurveyLevel.SEEN && surveyLevel != MarketAPI.SurveyLevel.NONE) {
                iter.remove();
            }
        }
        return result;
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

        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        fleet.getStats().getAccelerationMult().unmodify(getModId());
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {

        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle(spec.getName());

        float pad = 10f;

        tooltip.addPara(txt("ERS_tt1"),
                pad, highlight,
                txt("ERS_tt1h"));
        
        tooltip.addPara(txt("ERS_tt2"),
                pad, highlight,
                txt("ERS_tt2h"));
        
        tooltip.addPara(txt("ERS_tt3"),
                pad, highlight,
                "" + (int) DETECTABILITY_RANGE_BONUS
        );

        if (fleet.isInHyperspace()) {
            JumpPointAPI point = Misc.findNearestJumpPoint(fleet);
            if (Misc.getDistanceToPlayerLY(point) < JUMP_POINT_DISTANCE) {
                String name = point.getDestinations().get(0).getDestination().getStarSystem().getName();
                tooltip.addPara(txt("ERS_tt4a") + name + txt("ERS_tt4b"), pad);
            } else {
                tooltip.addPara(txt("ERS_tt4c"), bad, pad);
            }
        } else {
            List<PlanetAPI> planets = getSurveyableInRange();
            if (planets.isEmpty()) {
                if (getAllPlanetsInRange().isEmpty()) {
                    tooltip.addPara(txt("ERS_tt5a"), bad, pad);
                } else {
                    tooltip.addPara(txt("ERS_tt5b"), bad, pad);
                }
            } else {
                tooltip.addPara(txt("ERS_tt6a"), pad);

                float currPad = 3f;
                String indent = txt("ERS_tt6b");
                for (PlanetAPI planet : planets) {
                    LabelAPI label = tooltip.addPara(indent + planet.getName() + txt("ERS_tt6c"),
                            currPad, planet.getSpec().getIconColor(),
                            planet.getTypeNameWithWorld().toLowerCase());
                    currPad = 0f;
                }
            }
            tooltip.addPara(txt("ERS_tt7"), gray, pad);
            addIncompatibleToTooltip(tooltip, expanded);
        }
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

}
