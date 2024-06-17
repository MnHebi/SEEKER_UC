package data.campaign.abilities;

import com.fs.starfarer.api.Global;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;
import java.util.EnumSet;
import org.lwjgl.opengl.GL11;

public class SKR_neutrinoScanAbility extends BaseToggleAbility {

    public static final String COMMODITY_ID = Commodities.VOLATILES;
    public static final float COMMODITY_PER_DAY = 2f; //consumes more volatiles
    public static final float DETECTABILITY_PERCENT = 50f;

    @Override
    protected String getActivationText() {
        if (COMMODITY_ID != null && getFleet() != null && getFleet().getCargo().getCommodityQuantity(COMMODITY_ID) <= 0
                && !Global.getSettings().isDevMode()) {
            return null;
        }
        return txt("ENS_activate");
    }

    @Override
    protected String getDeactivationText() {
        return null;
    }

    @Override
    protected void activateImpl() {
    }

    @Override
    public boolean showProgressIndicator() {
        return false;
    }

    @Override
    public boolean showActiveIndicator() {
        return isActive();
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color bad = Misc.getNegativeHighlightColor();
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = txt("skill_off");
        if (turnedOn) {
            status = txt("skill_on");
        }

        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(gray);

        float pad = 10f;

        tooltip.addPara(txt("ENS_tt1"), pad);

        tooltip.addPara(txt("ENS_tt2"), pad);

        tooltip.addPara(txt("ENS_tt3"),
                pad, highlight,
                txt("ENS_tt3h"));

        if (COMMODITY_ID != null) {
            String unit = txt("skill_unit");
            if (COMMODITY_PER_DAY != 1) {
                unit = txt("skill_units");
            }
            CommoditySpecAPI commodity = getCommodity();
            unit += txt("skill_of") + commodity.getName().toLowerCase();

            tooltip.addPara(txt("ENS_tt4a") + unit + txt("ENS_tt4b"),
                    pad, highlight,
                    "" + (int) DETECTABILITY_PERCENT + txt("%"),
                    "" + Misc.getRoundedValueMaxOneAfterDecimal(COMMODITY_PER_DAY)
            );
        } else {
            tooltip.addPara(txt("ENS_tt5"),
                    pad, highlight,
                    "" + (int) DETECTABILITY_PERCENT + txt("%")
            );
        }

        if (getFleet() != null && getFleet().isInHyperspace()) {
            tooltip.addPara(txt("ENS_tt6"), bad, pad);
        } else {
            tooltip.addPara(txt("ENS_tt6"), pad);
        }

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return EnumSet.of(CampaignEngineLayers.ABOVE);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (data != null && !isActive() && getProgressFraction() <= 0f) {
            data = null;
        }
    }

    private float phaseAngle, scanLevel;
    private SKR_neutrinoScanData data = null;

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return;
        }

        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, txt("ENS_id"));

        float days = Global.getSector().getClock().convertToDays(amount);
        phaseAngle += days * 360f * 10f;
        phaseAngle = Misc.normalizeAngle(phaseAngle);
        
        //memory stuff
        String memKey = "$scan_"+fleet.getContainingLocation().getId();
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        if(mem.contains(memKey)){
            scanLevel = (float)mem.get(memKey);
        } else {
            scanLevel=0;
        }
        
        if (data == null) {
            data = new SKR_neutrinoScanData(this);
        }
        data.advance(days);

        if (COMMODITY_ID != null) {
            float cost = days * COMMODITY_PER_DAY;
            if (fleet.getCargo().getCommodityQuantity(COMMODITY_ID) > 0 || Global.getSettings().isDevMode()) {
                fleet.getCargo().removeCommodity(COMMODITY_ID, cost);
            } else {
                CommoditySpecAPI commodity = getCommodity();
                fleet.addFloatingText("Out of " + commodity.getName().toLowerCase(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate();
            }
        }

        if (fleet.isInHyperspace()) {
            deactivate();
        }
    }

    public CommoditySpecAPI getCommodity() {
        return Global.getSettings().getCommoditySpec(COMMODITY_ID);
    }

    @Override
    public boolean isUsable() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) {
            return false;
        }

        return isActive() || !fleet.isInHyperspace();
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

        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
    }

    public float getRingRadius() {
        return getFleet().getRadius() + 75f;
    }

    transient private SpriteAPI texture;

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {

        if (data == null) {
            return;
        }

        float progress = getProgressFraction();
        if (progress <= 0) {
            return;
        }
        if (getFleet() == null) {
            return;
        }
        if (!getFleet().isPlayerFleet()) {
            return;
        }

        float alphaMult = viewport.getAlphaMult() * progress;

        float bandWidthInTexture = 256;
        float bandIndex;

        float radStart = getRingRadius();
        float radEnd = radStart + 75f;

        float circ = (float) (Math.PI * 2f * (radStart + radEnd) / 2f);
        float pixelsPerSegment = circ / 360f;
        float segments = Math.round(circ / pixelsPerSegment);

        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360f);
        float spanRad = Math.abs(endRad - startRad);
        float anglePerSegment = spanRad / segments;

        Vector2f loc = getFleet().getLocation();
        float x = loc.x;
        float y = loc.y;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (texture == null) {
            texture = Global.getSettings().getSprite("abilities", "skr_neutrinoA");
        }
        texture.bindTexture();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        boolean outlineMode = false;
        if (outlineMode) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        }

        float thickness = (radEnd - radStart) * 1f;
        float radius = radStart;

        float texProgress = 0f;
        float texHeight = texture.getTextureHeight();
        float imageHeight = texture.getHeight();
        float texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness;

        texPerSegment *= 1f;

        float totalTex = Math.max(1f, Math.round(texPerSegment * segments));
        texPerSegment = totalTex / segments;

        float texWidth = texture.getTextureWidth();
        float imageWidth = texture.getWidth();

        Color color = new Color(25, 215, 255, 255);

        for (int iter = 0; iter < 2; iter++) {
            if (iter == 0) {
                bandIndex = 1;
            } else {
                bandIndex = 0;
                texProgress = segments / 2f * texPerSegment;
            }
            if (iter == 1) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            }

            float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
            float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;

            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (float i = 0; i < segments + 1; i++) {

                float segIndex = i % (int) segments;

                float phaseAngleRad;
                if (iter == 0) {
                    phaseAngleRad = (float) Math.toRadians(phaseAngle) + (segIndex * anglePerSegment * 29f);
                } else {
                    phaseAngleRad = (float) Math.toRadians(-phaseAngle) + (segIndex * anglePerSegment * 17f);
                }

                float angle = (float) Math.toDegrees(segIndex * anglePerSegment);

                //wavy animation in the detection ring
                float pulseSin = (float) Math.sin(phaseAngleRad);                
//                float pulseMax = thickness * 0.5f;
//                pulseMax = thickness * 0.2f;
                float pulseMax = 2 + 8*(1-scanLevel); //wavy animation decays over time

                float pulseAmount = pulseSin * pulseMax;
                float pulseInner = pulseAmount * 0.1f;

                float r = radius;

                float theta = anglePerSegment * segIndex;
                float cos = (float) Math.cos(theta);
                float sin = (float) Math.sin(theta);

                float rInner = r - pulseInner;

                float rOuter = r + thickness - pulseAmount;

                float grav = data.getDataAt(angle);
                if (grav > 750) {
                    grav = 750;
                }
                grav *= 250f / 750f;
                grav *= progress;
                rOuter += grav;
                
                //spikes get colored now
                float blend = grav/66f;
                Color spikeColor = Misc.interpolateColor(color, Color.RED, Math.min(Math.max(blend,0),1));
                
                float alpha = alphaMult;
                alpha *= 0.25f + Math.min(grav / 100, 0.75f);

                float x1 = cos * rInner;
                float y1 = sin * rInner;
                float x2 = cos * rOuter;
                float y2 = sin * rOuter;

                x2 += (float) (Math.cos(phaseAngleRad) * pixelsPerSegment * 0.33f);
                y2 += (float) (Math.sin(phaseAngleRad) * pixelsPerSegment * 0.33f);

                GL11.glColor4ub((byte) spikeColor.getRed(),
                        (byte) spikeColor.getGreen(),
                        (byte) spikeColor.getBlue(),
                        (byte) ((float) spikeColor.getAlpha() * alphaMult * alpha));

                GL11.glTexCoord2f(leftTX, texProgress);
                GL11.glVertex2f(x1, y1);
                GL11.glTexCoord2f(rightTX, texProgress);
                GL11.glVertex2f(x2, y2);

                texProgress += texPerSegment * 1f;
            }
            GL11.glEnd();
        }
        GL11.glPopMatrix();

        if (outlineMode) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    }
}
