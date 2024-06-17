package data.campaign.customstart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import org.magiclib.util.MagicCampaign;
import org.magiclib.util.MagicVariables;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class SKR_balisongStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(
            Arrays.asList(
                    new String[]{
                        "SKR_balisong_starter",}
            )
    );

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);
        ExerelinSetupData.getInstance().freeStart = true;

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);

        data.addScriptBeforeTimePass(
                new Script() {
            @Override
            public void run() {

                //pick random location
                SectorEntityToken location = null;
                for (Integer i = 0; i < 9; i++) {

                    List<String> themes = new ArrayList<>();
                    themes.add(Tags.THEME_REMNANT_SECONDARY);
                    themes.add(Tags.THEME_REMNANT_SUPPRESSED);
                    themes.add(Tags.THEME_REMNANT_RESURGENT);

                    List<String> notThemes = new ArrayList<>();
                    notThemes.add(MagicVariables.AVOID_COLONIZED_SYSTEM);
                    notThemes.add(MagicVariables.AVOID_BLACKHOLE_PULSAR);
                    notThemes.add("theme_hidden");

                    List<String> entities = new ArrayList<>();
                    entities.add(Tags.STATION);
                    entities.add(Tags.DEBRIS_FIELD);

                    SectorEntityToken token = MagicCampaign.findSuitableTarget(
                            null,
                            null,
                            "CLOSE",
                            themes,
                            notThemes,
                            entities,
                            false,
                            true,
                            false
                    );

                    if (token != null) {
                        location = token;
                        break;
                    }
                }

                //spawn location
                Global.getSector().getMemoryWithoutUpdate().set("$nex_startLocation", location.getId());

                //battle debris
                if(!location.hasTag(Tags.DEBRIS_FIELD)){
                    SectorEntityToken field = MagicCampaign.createDebrisField(
                            location.getId() + "debris",
                            200,
                            1,
                            30,
                            -1,
                            200,
                            0,
                            null,
                            -1,
                            1,
                            false,
                            -1,
                            location.getOrbitFocus(),
                            VectorUtils.getAngle(location.getOrbitFocus().getLocation(), location.getLocation()),
                            MathUtils.getDistance(location, location.getOrbitFocus()),
                            location.getOrbit().getOrbitalPeriod()
                    );

                    MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.FUEL, null, 198);
                    MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.SUPPLIES, null, 321);
                    MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.CREW, null, 152);
                    MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.COMMODITY, Commodities.HEAVY_MACHINERY, 48);
                    MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.COMMODITY, Commodities.METALS, 131);
                    MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.COMMODITY, Commodities.VOLATILES, 16);
                } else {
                    MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.FUEL, null, 198);
                    MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.SUPPLIES, null, 321);
                    MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.CREW, null, 152);
                    MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.COMMODITY, Commodities.HEAVY_MACHINERY, 48);
                    MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.COMMODITY, Commodities.METALS, 131);
                    MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.COMMODITY, Commodities.VOLATILES, 16);
                }
                
                //ships to recover
                List<String> toRecover = new ArrayList<>();
                {
                    toRecover.add("tarsus_d_Standard");
                    toRecover.add("dram_Light");
                    toRecover.add("brawler_Starting");
                }
                for (String s : toRecover) {
                    float radius = MathUtils.getRandomNumberInRange(25, 200);
                    MagicCampaign.createDerelict(
                            s,
                            ShipRecoverySpecial.ShipCondition.BATTERED,
                            false, -1,
                            true,
                            location,
                            MathUtils.getRandomNumberInRange(0, 360),
                            radius,
                            radius / 10
                    );
                }
                //ships to salvage                            
                List<String> toSalvage = new ArrayList<>();
                {
                    toSalvage.add("apogee_Starting");
                    toSalvage.add("brawler_Elite");
                    toSalvage.add("colossus_Standard");
                    toSalvage.add("dram_Light");
                    toSalvage.add("vigilance_Standard");
                    toSalvage.add("venture_Exploration");
                    toSalvage.add("brilliant_Standard");
                    toSalvage.add("fulgent_Support");
                    toSalvage.add("lumen_Standard");
                }
                for (String s : toSalvage) {
                    float radius = MathUtils.getRandomNumberInRange(25, 200);
                    MagicCampaign.createDerelict(
                            s,
                            ShipRecoverySpecial.ShipCondition.WRECKED,
                            false, -1,
                            Math.random() > 0.75f,
                            location,
                            MathUtils.getRandomNumberInRange(0, 360),
                            radius,
                            radius / 10
                    );
                }

                //relations
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.HEGEMONY, RepLevel.HOSTILE);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.LUDDIC_CHURCH, RepLevel.HOSTILE);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.PERSEAN, RepLevel.INHOSPITABLE);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.DIKTAT, RepLevel.INHOSPITABLE);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.INDEPENDENT, RepLevel.SUSPICIOUS);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.LUDDIC_PATH, RepLevel.VENGEFUL);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.REMNANTS, RepLevel.SUSPICIOUS);
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.DERELICT, RepLevel.NEUTRAL);

                //Prevent the MagicBounty from spawning
                Global.getSector().getMemoryWithoutUpdate().set("$SKR_balisong", true);
            }
        }
        );

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
