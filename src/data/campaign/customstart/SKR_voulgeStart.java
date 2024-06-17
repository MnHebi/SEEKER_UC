package data.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicCampaign;
import org.magiclib.util.MagicVariables;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static data.scripts.util.SKR_txt.txt;
import exerelin.campaign.intel.colony.ColonyExpeditionIntel;
import exerelin.utilities.NexUtilsMarket;
import java.util.Iterator;
import org.lazywizard.lazylib.MathUtils;

public class SKR_voulgeStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(
            Arrays.asList(
                    new String[]{
                        "SKR_voulge_outdated",
                        "crig_Standard",
                        "shepherd_Starting",
                        "shepherd_Frontier",
                    }
            )
    );

    private StarSystemAPI home=null;
    
    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
                
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);
        
        ExerelinSetupData.getInstance().freeStart = true;

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);

        data.addScriptBeforeTimePass(
//        data.addScript(
            new Script() {
                @Override
                public void run() {                    
                    
                    //pick random location
                    SectorEntityToken location = null;
                    
                    //the system we are looking for is a bit specific so let's try to avoid testing the same one multiple times
                    List<StarSystemAPI> skipped=new ArrayList<>();
                    
                    for (Integer i = 0; i < 25; i++) {

                        List<String> seekThemes = new ArrayList<>();
                        seekThemes.add(Tags.THEME_RUINS);
                        
                        List<String> avoidThemes = new ArrayList<>();
                        avoidThemes.add(Tags.THEME_UNSAFE);
                        avoidThemes.add(MagicVariables.AVOID_BLACKHOLE_PULSAR);
                        avoidThemes.add(MagicVariables.AVOID_COLONIZED_SYSTEM);
                        avoidThemes.add(MagicVariables.AVOID_OCCUPIED_SYSTEM);
                        avoidThemes.add("skip");
                        
                        List<String> desiredEntity = new ArrayList<>();
                        desiredEntity.add(Tags.PLANET);

                        //look for a safe system with ruins
                        SectorEntityToken token = MagicCampaign.findSuitableTarget(
                                null,
                                null,
                                "CLOSE",
                                seekThemes,
                                avoidThemes,
                                desiredEntity,
                                false,
                                false,
                                false
                        );

                        if (token != null) {                            
                            StarSystemAPI tokenSystem = token.getStarSystem();                            
                            //found a system, now check for habitable planets but not too good either
                            for(PlanetAPI p : tokenSystem.getPlanets()){
                                if(p.hasCondition(Conditions.FARMLAND_POOR) || p.hasCondition(Conditions.FARMLAND_ADEQUATE)){
                                    location = p;
                                    break;
                                }
                            }
                        
                            if(location==null){    
                                //no suitable planet in this system, lets skip it next time
                                token.getStarSystem().addTag("skip");      
                            } else {                  
                                //found a planet, remove the now uneeded tag and break the loop
                                for(StarSystemAPI s : skipped){
                                    s.removeTag("skip");
                                }
                                break;
                            }
                        }
                    }
                    
                    //spawn location deliberate null crash if the game cannot find a starting location
                    Global.getSector().getMemoryWithoutUpdate().set("$nex_startLocation", location.getId());                    
                    home=location.getStarSystem();
                    
                    //try to prevent scavengers from intruding
                    for (Iterator<EveryFrameScript> iter = home.getScripts().iterator(); iter.hasNext();) { 
                        EveryFrameScript script = iter.next();
                        if(script instanceof RuinsFleetRouteManager){
                            iter.remove();
                        }
                    }
                    
                    /*
                    //setup the colony                    
                    MarketAPI sourceMarket = location.getMarket();
                    sourceMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    
                    MarketAPI market = Global.getFactory().createMarket("SKR_voulgeColony", txt("voulge_ColonyName"), 4);
                    
                    market.setPrimaryEntity(location);
                    market.setFactionId(Factions.PLAYER);
                    market.setPlayerOwned(true);
                    market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    
                    List <String> conditions = new ArrayList<>();
                    for( MarketConditionAPI c : sourceMarket.getConditions()){
                        conditions.add(c.getId());
                    }                    
                    if(conditions.contains(Conditions.RUINS_VAST)){
                        conditions.remove(Conditions.RUINS_VAST);
                    }
                    if(conditions.contains(Conditions.RUINS_SCATTERED)){
                        conditions.remove(Conditions.RUINS_SCATTERED);
                    }
                    if(!conditions.contains(Conditions.RUINS_WIDESPREAD)&&!conditions.contains(Conditions.RUINS_EXTENSIVE)){
                        market.removeCondition(Conditions.RUINS_WIDESPREAD);
                    }
                    for (String i : conditions){
                        market.addCondition(i);
                    }
                    
                    List <String> industries = new ArrayList<>();
                    industries.add(Industries.FARMING);
                    industries.add(Industries.SPACEPORT);
                    industries.add(Industries.HEAVYINDUSTRY);
                    industries.add(Industries.POPULATION);
                    industries.add(Industries.PATROLHQ);
                    industries.add(Industries.WAYSTATION);
                    industries.add(Industries.TECHMINING);
                    for (String i : industries) {
                        market.addIndustry(i);
                    }
                    
                    market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
                    ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
                    market.addSubmarket(Submarkets.SUBMARKET_BLACK);
                    market.addSubmarket(Submarkets.SUBMARKET_OPEN);
                    
                    market.setHasWaystation(true);
                    market.setHasSpaceport(true);
                    */
                    
                    //setup the colony                    
                    MarketAPI market = location.getMarket();
                    market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    
//                    MarketAPI market = Global.getFactory().createMarket("SKR_voulgeColony", txt("voulge_ColonyName"), 4);
                    
                    market.setPrimaryEntity(location);
                    market.setFactionId(Factions.PLAYER);
                    market.setPlayerOwned(true);
                    market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                    
                    List <String> conditions = new ArrayList<>();
                    for( MarketConditionAPI c : market.getConditions()){
                        conditions.add(c.getId());
                    }                    
                    if(conditions.contains(Conditions.RUINS_VAST)){
                        market.removeCondition(Conditions.RUINS_VAST);
                    }
                    if(conditions.contains(Conditions.RUINS_SCATTERED)){
                        market.removeCondition(Conditions.RUINS_SCATTERED);
                    }
                    if(!conditions.contains(Conditions.RUINS_WIDESPREAD)&&!conditions.contains(Conditions.RUINS_EXTENSIVE)){
                        market.addCondition(Conditions.RUINS_WIDESPREAD);
                    }
                    
                    List <String> industries = new ArrayList<>();
                    industries.add(Industries.FARMING);
                    //industries.add(Industries.SPACEPORT);
                    industries.add(Industries.HEAVYINDUSTRY);
                    industries.add(Industries.POPULATION);
                    //industries.add(Industries.PATROLHQ);
                    industries.add(Industries.WAYSTATION);
                    industries.add(Industries.TECHMINING);
                    for (String i : industries) {
                        market.addIndustry(i);
                    }
                    
//                    market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
//                    ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
//                    market.addSubmarket(Submarkets.SUBMARKET_BLACK);
//                    market.addSubmarket(Submarkets.SUBMARKET_OPEN);
                    
                    //market.setHasWaystation(true);
                    //market.setHasSpaceport(true);
                    
                    ColonyExpeditionIntel.createColonyStatic(
                            market,
                            (PlanetAPI)location,
                            Global.getSector().getPlayerFaction(),
                            false,
                            true
                    );
                    
                    market.setName(txt("voulge_ColonyName"));
                    location.setName(txt("voulge_ColonyName"));
                    market.setSize(4);
                    market.removeCondition(Conditions.POPULATION_3);
                    market.addCondition(Conditions.POPULATION_4);
                    
                    //setup player faction default
                    FactionAPI PLAYER = Global.getSector().getPlayerFaction();
                    PLAYER.setDisplayNameOverride(txt("voulge_FactionName"));
                    PLAYER.setDisplayNameWithArticleOverride(txt("voulge_FactionNameArticle"));
                    PLAYER.setDisplayIsOrAreOverride(txt("voulge_FactionIsOrAre"));
                    PLAYER.setPersonNamePrefixAOrAnOverride(txt("voulge_FactionAnOrA"));
                    PLAYER.setShipNamePrefixOverride(txt("voulge_FactionPrefix"));   
                    PLAYER.addKnownShip("SKR_voulge",false);
                    //show faction setup dialog
                    Misc.isPlayerFactionSetUp();
                    
                    //add a few salvageable derelicts around to speed up the start                    
                    List<String>wrecks=new ArrayList<>();
                    wrecks.add("buffalo_d_Standard");
                    wrecks.add("phaeton_Standard");
                    wrecks.add("ox_Standard");
                    wrecks.add("lasher_d_CS");
                    wrecks.add("enforcer_d_Strike");
                    
                    List<SectorEntityToken> debris = location.getStarSystem().getEntitiesWithTag(Tags.DEBRIS_FIELD);
                    if(debris.size()<wrecks.size()){
                        debris.add(
                                MagicCampaign.createDebrisField(
                                    "voulge_start_debris",
                                    100,
                                    1,
                                    -1,
                                    0,
                                    null,
                                    -1,
                                    null,
                                    -1,
                                    1,
                                    false,
                                    null,
                                    location,
                                    MathUtils.getRandomNumberInRange(-180, 180),
                                    150,
                                    90
                                )
                        );
                    }
                    
                    for(Integer i=0; i<wrecks.size();i++){
                        SectorEntityToken place = debris.get(Math.min(i, debris.size()-1));                        
                        SectorEntityToken wreck = MagicCampaign.createDerelict(
                                wrecks.get(i),
                                ShipRecoverySpecial.ShipCondition.BATTERED,
                                true,
                                100,
                                true,
                                place,
                                MathUtils.getRandomNumberInRange(-180, 180),
                                MathUtils.getRandomNumberInRange(30, 60),
                                MathUtils.getRandomNumberInRange(30, 60)
                        );
                        wreck.setFacing(MathUtils.getRandomNumberInRange(-180, 180));
                    }
                }
            }
        );
        data.addScript(
            new Script() {
                @Override
                public void run() {   
                    //survey all because it just makes sense
                    Misc.setAllPlanetsSurveyed(home, false);
                    
                    //try to prevent scavengers from intruding
                    for (Iterator<EveryFrameScript> iter = home.getScripts().iterator(); iter.hasNext();) { 
                        EveryFrameScript script = iter.next();
                        if(script instanceof RuinsFleetRouteManager){
                            iter.remove();
                        }
                    }
//                    for(EveryFrameScript script : home.getScripts()){
//                        if(script instanceof RuinsFleetRouteManager){
//                            home.removeScript(script);
//                            break;
//                        }
//                    }
        
                    //remove all foreign fleets
                    for(CampaignFleetAPI fleet : home.getFleets()){
                        if(fleet!=Global.getSector().getPlayerFleet() || !fleet.getFaction().getId().equals(Factions.NEUTRAL)){
                            fleet.setLocation(10000, 10000);
                            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                                fleet.removeFleetMemberWithDestructionFlash(member);
                            }
                        }
                    }
                }
            }
        );
        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
