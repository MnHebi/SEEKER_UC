package data.campaign.customstart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
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
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class SKR_cultistStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(
            Arrays.asList(
                    new String[]{
                        "venture_Exploration",
                        "condor_Strike",
                        "cerberus_Starting",
                        "cerberus_Starting",
                        "hound_Starting",
                        "tarsus_Standard",
                        "phaeton_Standard",}
            )
    );

    private static final Logger LOG = Global.getLogger(SKR_cultistStart.class);
    private final boolean verbose = Global.getSettings().isDevMode();
    
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

                    //pick plaguebearer location
                    SectorEntityToken location = null;
                    
                    for (SectorEntityToken e : Global.getSector().getEntitiesWithTag("SKR_cataclysm")){
                        location = e.getStarSystem().getStar();
                        if(verbose){
                            LOG.info("Cataclysm found: "+location.getStarSystem().getNameWithTypeShort());
                        }
                        break;
                    }
                    
                    if(location==null){
                        if(verbose){
                            LOG.info("Cataclysm NOT found, finding backup system");
                        }
                        //backup generation although this means there is no plaguebearer in the game
                        for (Integer i = 1; i < 10; i++) {
                            List<String> themes = new ArrayList<>();
                            themes.add(Tags.THEME_DERELICT_PROBES);
                            themes.add(Tags.THEME_INTERESTING_MINOR);
                            themes.add(Tags.THEME_RUINS_SECONDARY);
                            List<String> notThemes = new ArrayList<>();
                            notThemes.add(MagicVariables.AVOID_OCCUPIED_SYSTEM);
                            notThemes.add(MagicVariables.AVOID_COLONIZED_SYSTEM);
                            notThemes.add(MagicVariables.AVOID_BLACKHOLE_PULSAR);
                            notThemes.add("theme_hidden");
                            List<String> entities = new ArrayList<>();
                            entities.add(Tags.DEBRIS_FIELD);
                            SectorEntityToken token = MagicCampaign.findSuitableTarget(
                                    null,
                                    null,
                                    "FAR",
                                    themes,
                                    notThemes,
                                    entities,
                                    false,
                                    true,
                                    false
                            );

                            if (token != null) {
                                location = token;
                                if(verbose){
                                    LOG.info("Attempt "+i+": found suitable backup location: "+ location.getFullName()+" in "+location.getStarSystem().getNameWithLowercaseTypeShort());
                                }
                                break;
                            } else {
                                if(verbose){
                                    LOG.info("Attempt "+i+": NO suitable location found.");
                                }
                            }
                        }
                        
                        if(verbose && location==null){
                            LOG.info("Cataclysm NOT found, backup NOT found. FAILED START, sorry for the crash.");
                        }
                    } else {
                        // this is supposedly the system with Cataclysm, lets pick an adjascent system instead.
                        float dist = 99999999;
                        StarSystemAPI picked=null;
                        for(StarSystemAPI s : Global.getSector().getStarSystems()){
                            if(s!=location.getStarSystem() && !s.hasBlackHole() && !s.hasPulsar()){
                                float thisDist = MathUtils.getDistanceSquared(location.getStarSystem().getLocation(), s.getLocation());
                                if(thisDist<dist){
                                    dist = MathUtils.getDistanceSquared(location.getLocationInHyperspace(), s.getLocation());
                                    picked = s;
                                }
                            }
                        }
                        if(picked!=null){
                            location=picked.getCenter();
                            if(verbose){
                                LOG.info("Picking nearest system: "+location.getStarSystem().getNameWithTypeShort());
                            }
                        } else if(verbose){
                            LOG.info("Picking nearest system: "+location.getStarSystem().getNameWithTypeShort());
                        }
                        //picked the closest star to Cataclysm's system, find a suitable location inside
                        if(location.getStarSystem().getEntitiesWithTag(Tags.DEBRIS_FIELD).size()>0){
                            location = location.getStarSystem().getEntitiesWithTag(Tags.DEBRIS_FIELD).get(MathUtils.getRandomNumberInRange(0, location.getStarSystem().getEntitiesWithTag(Tags.DEBRIS_FIELD).size()-1));
                            if(verbose){
                                LOG.info("Found suitable debris field in the "+location.getStarSystem().getNameWithTypeShort()+" : "+location.getFullName());
                            }
                        } else {
                            List<PlanetAPI> planets = new ArrayList<>();
                            if(location.getStarSystem().getPlanets().size()>0){
                                for(PlanetAPI p : location.getStarSystem().getPlanets()){
                                    if(!p.isStar())planets.add(p);
                                }
                            }
                            if(!planets.isEmpty()){
                                location = planets.get(MathUtils.getRandomNumberInRange(0, planets.size()-1));
                                if(verbose){
                                    LOG.info("NO debris field in the "+location.getStarSystem().getNameWithTypeShort()+", defaulting to the planet "+location.getFullName());
                                }
                            } else {
                                location = location.getStarSystem().getJumpPoints().get(MathUtils.getRandomNumberInRange(0, location.getStarSystem().getJumpPoints().size()-1));
                                if(verbose){
                                    LOG.info("NO debris field and NO planet in the "+location.getStarSystem().getNameWithTypeShort()+", defaulting to "+location.getFullName());
                                }
                            }
                        }
                    }
                    
                    if(verbose && location==null){
                        LOG.info("Somehow the location is NULL, wtf is going on?");
                    }
                    
                    //spawn location
                    Global.getSector().getMemoryWithoutUpdate().set("$nex_startLocation", location.getId());

                    //battle debris
                    if (location instanceof PlanetAPI || location instanceof JumpPointAPI) {
                        SectorEntityToken field = MagicCampaign.createDebrisField(
                                "cultistStartDebrisField",
                                200,
                                1,
                                720,
                                -1,
                                200,
                                0,
                                null,
                                null,
                                1,
                                false,
                                -1,
                                location,
                                0,
                                0,
                                360
                        );
                        
                        if(verbose){
                            LOG.info("Adding debris field "+field.getName());
                        }

                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.FUEL, null, 562);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.SUPPLIES, null, 321);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.COMMODITY, Commodities.HEAVY_MACHINERY, 32);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.COMMODITY, Commodities.METALS, 231);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.COMMODITY, Commodities.VOLATILES, 24);
                        
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.WEAPON, "SKR_pdCharger", 2);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.WEAPON, "SKR_pdGrenade", 1);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.WEAPON, "SKR_intercept", 1);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.WEAPON, "SKR_hvBlaster", 1);
                        MagicCampaign.addSalvage(field.getCargo(), field, MagicCampaign.lootType.WEAPON, "SKR_lightstreak", 1);
                    } else {
                        
                        if(verbose){
                            LOG.info("Adding loot to "+location.getName());
                        }
                        
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.FUEL, null, 562);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.SUPPLIES, null, 321);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.COMMODITY, Commodities.HEAVY_MACHINERY, 32);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.COMMODITY, Commodities.METALS, 231);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.COMMODITY, Commodities.VOLATILES, 24);
                        
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.WEAPON, "SKR_pdCharger", 2);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.WEAPON, "SKR_pdGrenade", 1);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.WEAPON, "SKR_intercept", 1);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.WEAPON, "SKR_hvBlaster", 1);
                        MagicCampaign.addSalvage(location.getCargo(), location, MagicCampaign.lootType.WEAPON, "SKR_lightstreak", 1);
                    }

                    //ships to recover
                    List<String> toRecover = new ArrayList<>();
                    {
                        toRecover.add("SKR_zealot_standard");
                        toRecover.add("SKR_cultist_standard");
                        toRecover.add("SKR_believer_standard");
                    }
                    for (String s : toRecover) {
                        float radius = MathUtils.getRandomNumberInRange(25, 200);
                        MagicCampaign.createDerelict(
                                s,
                                ShipRecoverySpecial.ShipCondition.AVERAGE,
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
                        toSalvage.add("rampart_Standard");
                        toSalvage.add("berserker_Assault");
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
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.HEGEMONY, RepLevel.SUSPICIOUS);
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.LUDDIC_CHURCH, RepLevel.NEUTRAL);
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.INDEPENDENT, RepLevel.FRIENDLY);
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.PERSEAN, RepLevel.FAVORABLE);
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.PIRATES, RepLevel.HOSTILE);
                }
            }
        );

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
