package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
//import com.fs.starfarer.api.impl.campaign.rulecmd.Nex_FleetRequest;
//import com.fs.starfarer.api.impl.campaign.rulecmd.Nex_FleetRequest.FleetType;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import data.campaign.bosses.SKR_cataclysmLoot;
import data.campaign.bosses.SKR_rampageLoot;
import data.campaign.bosses.SKR_safeguardLoot;
import data.campaign.bosses.SKR_whiteDwarfLoot;
import data.campaign.ids.SKR_ids;
import data.scripts.util.MagicCampaign;
import static data.scripts.util.SKR_txt.txt;
import data.scripts.world.systems.SKR_plagueA;
import data.scripts.world.systems.SKR_plagueB;
import data.scripts.world.systems.SKR_plagueC;
import data.scripts.world.systems.SKR_plagueD;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_seekerGen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        
        List <Integer> directions = new ArrayList<>();
        directions.add(45);
        directions.add(-45);
        directions.add(135);
        directions.add(-135);
        Collections.shuffle(directions);
        
        new SKR_plagueA().generate(sector,directions.get(0));    
        new SKR_plagueB().generate(sector,directions.get(1));    
        new SKR_plagueC().generate(sector,directions.get(2));   
        new SKR_plagueD().generate(sector,directions.get(3));    
        
        initFactionRelationships(sector);
    }
    
    public static void initFactionRelationships(SectorAPI sector){
        FactionAPI plague = sector.getFaction("plague");
        for(FactionAPI f : sector.getAllFactions()){
            plague.setRelationship(f.getId(), RepLevel.INHOSPITABLE);
        }
        plague.setRelationship(Factions.REMNANTS, RepLevel.FRIENDLY);
        plague.setRelationship(Factions.DERELICT, RepLevel.FRIENDLY);
        plague.setRelationship(Factions.NEUTRAL, RepLevel.NEUTRAL);
    }
    
    private static final Logger LOG = Global.getLogger(SKR_seekerGen.class);
    
    public static void addExplorationContent(){
        
        spawnNova();
        
        spawnSafeguard();
        spawnRampage();
        spawnWhiteDwarf();
        spawnCataclysm();
        
        spawnDemeter();
        spawnTitanic();
        spawnVoulge();
    }
    
    ////////////////////
    //                //
    //      NOVA      //
    //                //
    ////////////////////
    
    private static void spawnNova(){
                
        //find all Nexus
        List<CampaignFleetAPI> stations = new ArrayList<>();
        
        for(StarSystemAPI system : Global.getSector().getStarSystems()){
            if(system.hasTag(Tags.THEME_REMNANT)){
                for(CampaignFleetAPI fleet : system.getFleets()){
                    if (fleet.isStationMode() ){
                        if( fleet.getFlagship().getVariant().getHullVariantId().equals("remnant_station2_Standard")){
                            stations.add(fleet);
                        }
                    }
                }
            }
        }
                
//        for(LocationAPI location : Global.getSector().getStarSystems()){
//            if(!location.isHyperspace() && !location.getFleets().isEmpty()){
//                for(CampaignFleetAPI fleet : location.getFleets()){
//                    if (fleet.isStationMode() ){
//                        if( fleet.getFlagship().getVariant().getHullVariantId().equals("remnant_station2_Standard")){
//                            stations.add(fleet);
//                        }
//                    }
//                }
//            }
//        }
        
        if(stations.isEmpty()){
            LOG.info("No suitable system found to spawn NOVA.");
        } else {
            //try to avoid HMI systems
            List<String> blacklist = new ArrayList<>();
            {
                blacklist.add(Tags.THEME_HIDDEN);
                blacklist.add("theme_plaguebearers");
		blacklist.add("theme_domres");
		blacklist.add("theme_hmi_nightmare");
		blacklist.add("theme_hmi_mess_remnant");
		blacklist.add("theme_messrem");
		blacklist.add("theme_domresboss");
		blacklist.add("theme_domres");
            }
            
            //pick closest to the core
            float distance=999999999;
            CampaignFleetAPI selected=null;
            CampaignFleetAPI backup=stations.get(0);
            for(CampaignFleetAPI station : stations){
                for(String theme : blacklist){
                    if(!station.getStarSystem().hasTag(theme)){
                        //try to avoid blackholes and pulsars but keep as a backup
                        if(station.getStarSystem().getStar().getSpec().isPulsar()||station.getStarSystem().getStar().getSpec().isBlackHole()){
                            float dist=MathUtils.getDistanceSquared(station.getStarSystem().getLocation(), new Vector2f());
                            if(dist<distance){
                                distance=dist;
                                backup=station;
                            }
                        } else {
                            float dist=MathUtils.getDistanceSquared(station.getStarSystem().getLocation(), new Vector2f());
                            if(dist<distance){
                                distance=dist;
                                selected=station;
                            }
                        }
                    }
                }
            }
            //switch to backup if needed
            if(selected==null)selected=backup;
            
            LOG.info("Adding NOVA boss fleet in "+selected.getStarSystem().getName());
            
            //add cool derelict to salvage
            SectorEntityToken onyx = MagicCampaign.createDerelict(
                    "SKR_onyx_night",
                    ShipRecoverySpecial.ShipCondition.WRECKED,
                    true,
                    -1,
                    true,
                    (SectorEntityToken)selected,
                    MathUtils.getRandomNumberInRange(0, 360),                 
                    MathUtils.getRandomNumberInRange(50, 150),                 
                    MathUtils.getRandomNumberInRange(30, 50)
            );
            onyx.addTag(Tags.NEUTRINO_LOW);
            
            MagicCampaign.addSalvage(null, onyx, MagicCampaign.lootType.WEAPON, "SKR_blackout", 2 );
            
            //add NOVA fleet 
            
        /**        
        createFleet(
            @Nullable String fleetName,
            @Nullable String fleetFaction,
            @Nullable String fleetType,
            @Nullable String flagshipName,
            @Nullable String flagshipVariant,
            boolean flagshipRecovery,
            boolean flagshipAutofit,
            @Nullable PersonAPI captain,
            @Nullable Map<String, Integer> supportFleet,
            boolean supportAutofit,
            int minFP,
            @Nullable String reinforcementFaction,
            @Nullable Float qualityOverride,
            @Nullable SectorEntityToken spawnLocation,
            @Nullable FleetAssignment assignment,
            @Nullable SectorEntityToken assignmentTarget,
            boolean isImportant,
            boolean transponderOn,
            @Nullable String variantsPath
        )
        */
            CampaignFleetAPI nova = MagicCampaign.createFleet(
                    txt("nova_fleet"),
                    Factions.REMNANTS,
                    FleetTypes.PERSON_BOUNTY_FLEET,
                    txt("nova_boss"),
                    "SKR_nova_falseOmega",
                    false,
                    true,
                    null, //generic remnant captain
                    null, //no preset suport fleet
                    true,
                    200,
                    null, //same reinforcement faction as the fleet
                    2f,
                    selected,
                    FleetAssignment.DEFEND_LOCATION,
                    onyx,
                    false,
                    true,
                    null
            );
            nova.addTag(Tags.NEUTRINO);
            nova.addTag(Tags.NEUTRINO_HIGH);
            nova.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
            nova.getFlagship().getVariant().addTag(Tags.SHIP_UNIQUE_SIGNATURE);
            nova.getFlagship().getVariant().addTag(Tags.VARIANT_UNBOARDABLE);
            Global.getSector().getMemoryWithoutUpdate().set("$SKR_nova", false);
            
//            selected.getStarSystem().getTags().add("theme_plaguebearers");
        }
    }
        
    ////////////////////
    //                //
    //   SAFEGUARD    //
    //                //
    ////////////////////
    
    private static void spawnSafeguard(){
        
        List<String> themesPlagueA = new ArrayList<>();
        themesPlagueA.add(Tags.THEME_DERELICT_PROBES);
        themesPlagueA.add(Tags.THEME_DERELICT_SURVEY_SHIP);
        
        List<String> notThemesPlagueA = new ArrayList<>();
        notThemesPlagueA.add(SKR_ids.THEME_PLAGUEBEARER);
        notThemesPlagueA.add(Tags.THEME_HIDDEN);
        notThemesPlagueA.add("theme_already_occupied");
        notThemesPlagueA.add("theme_already_colonized");
        notThemesPlagueA.add("no_pulsar_blackhole");
        
        List<String> entitiesPlagueA = new ArrayList<>();
        entitiesPlagueA.add(Tags.GAS_GIANT);
                
        SectorEntityToken targetPlagueA = MagicCampaign.findSuitableTarget(
                null,
                null,
                "CLOSE",
                themesPlagueA,
                notThemesPlagueA,
                entitiesPlagueA,
                true,
                true,
                true
        );
        
        if(targetPlagueA==null){
            LOG.info("NO SYSTEM AVAILABLE FOR SAFEGUARD");
            return;
        } else {
            LOG.info("Adding SAFEGUARD boss fleet in "+targetPlagueA.getStarSystem().getName());
        } 
                
        /**
        * Creates a debris field with generic commodity loot to salvage 
        * 
        * @param id
        * field ID
        * @param radius
        * field radius in su (clamped to 1000)
        * @param density
        * field visual density
        * @param duration
        * field duration in days (set to a negative value for a permanent field
        * @param glowDuration
        * time in days with glowing debris
        * @param salvageXp
        * XP awarded for salvaging (<0 to use the default)
        * @param defenderProbability
        * chance of an enemy fleet guarding the debris field (<0 to ignore)
        * @param defenderFaction
        * defender's faction
        * @param defenderFP
        * defender fleet's size in Fleet Points
        * @param detectionMult
        * detection distance multiplier
        * @param discoverable
        * awards XP when found
        * @param discoveryXp 
        * XP awarded when found (<0 to use the default)
        * @param orbitCenter
        * entity orbited
        * @param orbitStartAngle
        * orbit starting angle
        * @param orbitRadius
        * orbit radius
        * @param orbitDays
        * orbit period
        * @return 
        */
        SectorEntityToken fieldA = MagicCampaign.createDebrisField(
                "plagueA_debris1",
                250,
                2f,
                10000,
                0,
                250,
                -1,
                null, 
                -1, 
                0.2f, 
                true,
                null,
                targetPlagueA, 
                90, 
                500,
                70
        );
        
        /**
        * Adds specific loot to a salvageable entity such as a debris field or fleet once defeated
        * 
        * @param carrier
        * entity carrying the loot
        * @param type
        * MagicSystem.lootType, type of loot added
        * @param lootID
        * specific ID of the loot found
        * @param amount 
        * max amount salvaged, this number may be lower if the carrier is a fleet that gets defeated
        */
        MagicCampaign.addSalvage(null, fieldA, MagicCampaign.lootType.SUPPLIES, null, 231);
                
        PersonAPI plagueA = MagicCampaign.createCaptain(
                true,
                Commodities.GAMMA_CORE,
                "",//txt("plague_A_boss"),
                txt("plague_A_boss"),
                "SKR_plagueA",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.RECKLESS,
                4,
                2,
                OfficerManagerEvent.SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_NO_DEFENSE,
                null //skills
        );
        
        /**        
        createFleet(
            @Nullable String fleetName,
            @Nullable String fleetFaction,
            @Nullable String fleetType,
            @Nullable String flagshipName,
            @Nullable String flagshipVariant,
            boolean flagshipRecovery,
            boolean flagshipAutofit,
            @Nullable PersonAPI captain,
            @Nullable Map<String, Integer> supportFleet,
            boolean supportAutofit,
            int minFP,
            @Nullable String reinforcementFaction,
            @Nullable Float qualityOverride,
            @Nullable SectorEntityToken spawnLocation,
            @Nullable FleetAssignment assignment,
            @Nullable SectorEntityToken assignmentTarget,
            boolean isImportant,
            boolean transponderOn,
            @Nullable String variantsPath
        )
        */
	CampaignFleetAPI safeguard = MagicCampaign.createFleet(
                txt("plague_A_fleet"),
                "plague",
                null,
                txt("plague_A_boss"),
                "SKR_keep_safeguard",
                false,
                false,
                plagueA,
                null,
                true,
                200,
                Factions.DERELICT,
                0.5f,
                null,
                FleetAssignment.PATROL_SYSTEM,
                targetPlagueA,
                false,
                true,
                null
        );
        safeguard.setDiscoverable(true);
        safeguard.addTag(Tags.NEUTRINO);
        safeguard.addTag(Tags.NEUTRINO_HIGH);
        safeguard.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
        safeguard.getFlagship().getVariant().addTag(Tags.SHIP_UNIQUE_SIGNATURE);
        safeguard.getFlagship().getVariant().addTag(Tags.VARIANT_UNBOARDABLE);
        Global.getSector().getMemoryWithoutUpdate().set("$SKR_safeguard_boss", true);
        safeguard.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
        safeguard.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        safeguard.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        
        safeguard.addEventListener(new SKR_safeguardLoot());
        
        targetPlagueA.getStarSystem().getTags().add(Tags.THEME_UNSAFE);
        targetPlagueA.getStarSystem().getTags().add(SKR_ids.THEME_PLAGUEBEARER);
    }
    
    ////////////////////
    //                //
    //    RAMPAGE     //
    //                //
    ////////////////////
    
    private static void spawnRampage(){
        
        List<String> themesPlagueB = new ArrayList<>();
        themesPlagueB.add(Tags.THEME_RUINS);
        
        List<String> notThemesPlagueB = new ArrayList<>();
        notThemesPlagueB.add(SKR_ids.THEME_PLAGUEBEARER);
        notThemesPlagueB.add(Tags.THEME_HIDDEN);
        notThemesPlagueB.add("theme_already_occupied");
        notThemesPlagueB.add("theme_already_colonized");
        notThemesPlagueB.add("no_pulsar_blackhole");
                
        SectorEntityToken targetPlagueB = MagicCampaign.findSuitableTarget(
                null,
                null,
                "CLOSE",
                themesPlagueB,
                notThemesPlagueB,
                null,
                true,
                true,
                true
        );
        
        if(targetPlagueB==null){
            LOG.info("NO SYSTEM AVAILABLE FOR RAMPAGE");
            return;
        } else {
            LOG.info("Adding RAMPAGE boss fleet in "+targetPlagueB.getStarSystem().getName());
        }        
        
        PersonAPI plagueB = MagicCampaign.createCaptain(
                true,
                Commodities.ALPHA_CORE,
                "",//txt("plague_B_boss"),
                txt("plague_B_boss"),
                "SKR_plagueB",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.AGGRESSIVE,
                10,
                10,
                OfficerManagerEvent.SkillPickPreference.NO_ENERGY_YES_BALLISTIC_NO_MISSILE_YES_DEFENSE,
                null
        );
        
	CampaignFleetAPI rampage = MagicCampaign.createFleet(
                txt("plague_B_fleet"),
                "plague",
                null,
                txt("plague_B_boss"),
                "SKR_rampage_01",
                false,
                false,
                plagueB, //officer
                null, //no escort fleet
                false,
                -1, //no support fleet
                null, //no support faction
                null, //no quallity override
                null,
                FleetAssignment.PATROL_SYSTEM,
                targetPlagueB,
                false,
                true,
                null
        );
        rampage.setDiscoverable(true);
        rampage.addTag(Tags.NEUTRINO);
        rampage.addTag(Tags.NEUTRINO_HIGH);
        rampage.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
        rampage.getFlagship().getVariant().addTag(Tags.SHIP_UNIQUE_SIGNATURE);
        rampage.getFlagship().getVariant().addTag(Tags.VARIANT_UNBOARDABLE);
        Global.getSector().getMemoryWithoutUpdate().set("$SKR_rampage_boss", true);
        //rampage.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
        rampage.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        rampage.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        
        rampage.addEventListener(new SKR_rampageLoot());
        
        //remove the ruins tag to prevent scavengers
        targetPlagueB.getStarSystem().getTags().remove(Tags.THEME_RUINS);
        targetPlagueB.getStarSystem().getTags().remove(Tags.THEME_RUINS_MAIN);
        targetPlagueB.getStarSystem().getTags().remove(Tags.THEME_RUINS_SECONDARY);
        targetPlagueB.getStarSystem().getTags().add(Tags.THEME_UNSAFE);
        targetPlagueB.getStarSystem().getTags().add(SKR_ids.THEME_PLAGUEBEARER);
    }
        
    ////////////////////
    //                //
    //   WHITE DWARF  //
    //                //
    ////////////////////
    
    private static void spawnWhiteDwarf(){
        
        List<String> themesPlagueC = new ArrayList<>();
        themesPlagueC.add(Tags.THEME_REMNANT_SECONDARY);
        themesPlagueC.add(Tags.THEME_REMNANT_RESURGENT);
        
        List<String> notThemesPlagueC = new ArrayList<>();
        notThemesPlagueC.add(SKR_ids.THEME_PLAGUEBEARER);
        notThemesPlagueC.add(Tags.THEME_HIDDEN);
        notThemesPlagueC.add("theme_already_colonized");
        notThemesPlagueC.add("no_pulsar_blackhole");
        
        List<String> entitiesPlagueC = new ArrayList<>();
        entitiesPlagueC.add(Tags.STATION);
        entitiesPlagueC.add(Tags.DEBRIS_FIELD);
        entitiesPlagueC.add(Tags.WRECK);
        entitiesPlagueC.add(Tags.SALVAGEABLE);
        
        SectorEntityToken targetPlagueC=null;
        for(Integer i=0;i<5;i++){
            targetPlagueC = MagicCampaign.findSuitableTarget(
                    null,
                    null,
                    "FAR",
                    themesPlagueC,
                    notThemesPlagueC,
                    entitiesPlagueC,
                    false,
                    true,
                    true
            );
            if(targetPlagueC!=null && targetPlagueC.getStarSystem().isProcgen()){
                break;
            }
        }
        
        
        if(targetPlagueC==null){
            LOG.info("NO SYSTEM AVAILABLE FOR WHITE DWARF");
            return;
        } else {
            LOG.info("Adding WHITE DWARF boss fleet in "+targetPlagueC.getStarSystem().getName());
        }        
               
        PersonAPI plagueC = MagicCampaign.createCaptain(
                true,
                Commodities.ALPHA_CORE,
                "",//txt("plague_C_boss"),
                txt("plague_C_boss"),
                "SKR_plagueC",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.AGGRESSIVE,
                12,
                6,
                OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_YES_DEFENSE,
                null
        );
        
//        Map<String,Integer> fleet = new HashMap<>();
//        fleet.put("brilliant_Standard", 1);
//        fleet.put("fulgent_Assault", 1);
//        fleet.put("fulgent_Support", 1);
//        fleet.put("scintilla_Strike", 1);
//        fleet.put("scintilla_Support", 1);

        CampaignFleetAPI whitedwarf = MagicCampaign.createFleet(
                txt("plague_C_fleet"),
                "plague",
                null,
                txt("plague_C_boss"),
                "SKR_whiteDwarf_1",
                false,
                false,
                plagueC,
                null,
                true,
                200,
                Factions.REMNANTS,
                1f,
                null,
                FleetAssignment.PATROL_SYSTEM,
                targetPlagueC,
                false,
                true,
                null
        );
        whitedwarf.setDiscoverable(true);       
        whitedwarf.addTag(Tags.NEUTRINO);
        whitedwarf.addTag(Tags.NEUTRINO_HIGH); 
        whitedwarf.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
        whitedwarf.getFlagship().getVariant().addTag(Tags.SHIP_UNIQUE_SIGNATURE);
        whitedwarf.getFlagship().getVariant().addTag(Tags.VARIANT_UNBOARDABLE);
        Global.getSector().getMemoryWithoutUpdate().set("$SKR_whitedwarf_boss", true);
        whitedwarf.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);
        whitedwarf.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        whitedwarf.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        
        //add escort      
        for(Integer i=0; i<5; i++){            
            
            List<String>flagships=new ArrayList<>();
            {
                flagships.add("fulgent_Assault");
                flagships.add("scintilla_Strike");
                flagships.add("brilliant_Standard");
                flagships.add("radiant_Standard");
            }
            Integer size =MathUtils.getRandomNumberInRange(1, flagships.size());
            
            PersonAPI hackedCore = MagicCampaign.createCaptain(
                    true,
                    Commodities.ALPHA_CORE,
                    "",//txt("plague_D_boss"),
                    txt("plague_AIcore"),
                    "SKR_AIcore",
                    FullName.Gender.ANY,
                    "remnant",
                    Ranks.SPACE_CAPTAIN,
                    Ranks.POST_PATROL_COMMANDER,
                    Personalities.AGGRESSIVE,
                    MathUtils.getRandomNumberInRange(2,size*2),
                    MathUtils.getRandomNumberInRange(0,size),
                    OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_YES_DEFENSE,
                    null //defined skills
            );
            
            CampaignFleetAPI hackedFleet = MagicCampaign.createFleet(
                    txt("plague_AIFleet"),
                    "remnant",
                    FleetTypes.PATROL_MEDIUM,
                    txt("plague_blank"),
                    flagships.get(size-1),
                    hackedCore,
                    null,
                    size*35,
                    "remnant",
                    1f,
                    null,
                    FleetAssignment.ORBIT_AGGRESSIVE,
                    whitedwarf,
                    false,
                    true
            );            
        }
                
        
        whitedwarf.addEventListener(new SKR_whiteDwarfLoot());
        
        targetPlagueC.getStarSystem().getTags().add(Tags.THEME_UNSAFE);
        targetPlagueC.getStarSystem().getTags().add(SKR_ids.THEME_PLAGUEBEARER);
    }
    
    ////////////////////
    //                //
    //   CATACLYSM    //
    //                //
    ////////////////////
    
    private static void spawnCataclysm(){
                
        List<String> themesPlagueD = new ArrayList<>();
        themesPlagueD.add(Tags.THEME_INTERESTING);
        themesPlagueD.add(Tags.THEME_RUINS_MAIN);
        
        List<String> notThemesPlagueD = new ArrayList<>();
        notThemesPlagueD.add(SKR_ids.THEME_PLAGUEBEARER);
        notThemesPlagueD.add(Tags.THEME_HIDDEN);
        notThemesPlagueD.add("theme_already_occupied");
        notThemesPlagueD.add("theme_already_colonized");
        notThemesPlagueD.add("no_pulsar_blackhole");
                
        List<String> entitiesPlagueD = new ArrayList<>();
        entitiesPlagueD.add(Tags.GATE);
                
        SectorEntityToken targetPlagueD = MagicCampaign.findSuitableTarget(
                null,
                null,
                "FAR",
                themesPlagueD,
                notThemesPlagueD,
                entitiesPlagueD,
                true,
                true,
                true
        );
        
        if(targetPlagueD==null){
            LOG.info("NO SYSTEM AVAILABLE FOR CATACLYSM");
            return;
        } else {
            LOG.info("Adding CATACLYSM boss fleet in "+targetPlagueD.getStarSystem().getName());
        }
        
        Map<String,Integer>retinue=new HashMap<>();
        
        retinue.put("SKR_believer_assault", 1);
        retinue.put("SKR_believer_standard", 1);
        retinue.put("SKR_believer_support", 1);
        
        retinue.put("SKR_follower_assault", 1);
        retinue.put("SKR_follower_standard", 1);
        retinue.put("SKR_follower_support", 1);
        
        retinue.put("SKR_devotee_assault", 1);
        retinue.put("SKR_devotee_standard", 1);
        retinue.put("SKR_devotee_support", 1);
        
        retinue.put("SKR_doctrinaire_assault", 1);
        retinue.put("SKR_doctrinaire_standard", 1);
        retinue.put("SKR_doctrinaire_support", 1);
        
        retinue.put("SKR_fanatic_assault", 1);
        retinue.put("SKR_fanatic_standard", 1);
        retinue.put("SKR_fanatic_support", 1);
        
        retinue.put("SKR_cultist_assault", 1);
        retinue.put("SKR_cultist_standard", 1);
        retinue.put("SKR_cultist_support", 1);
        
        retinue.put("SKR_guru_assault", 1);
        //retinue.put("SKR_guru_standard", 1);
        //retinue.put("SKR_guru_support", 1);
        
        retinue.put("SKR_zealot_assault", 1);
//        retinue.put("SKR_zealot_standard", 1);
//        retinue.put("SKR_zealot_support", 1);
        
        PersonAPI plagueD = MagicCampaign.createCaptain(
                true,
                Commodities.ALPHA_CORE,
                "",//txt("plague_D_boss"),
                txt("plague_D_boss"),
                "SKR_plagueD",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.AGGRESSIVE,
                14,
                14,
                OfficerManagerEvent.SkillPickPreference.YES_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE,
                null //defined skills
        );
        
	CampaignFleetAPI cataclysm = MagicCampaign.createFleet(
                txt("plague_D_fleet"),
                "plague",
                null,
                txt("plague_D_boss"),
                "SKR_cataclysm_1",
                false,
                false,
                plagueD,
                retinue,
                false,
                0,
                null,
                2f,
                null,
                FleetAssignment.PATROL_SYSTEM,
                targetPlagueD,
                false,
                true,
                null
        );
        cataclysm.setDiscoverable(true);
        cataclysm.addTag(Tags.NEUTRINO);
        cataclysm.addTag(Tags.NEUTRINO_HIGH);
        cataclysm.addTag("SKR_cataclysm");
        cataclysm.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
        cataclysm.getFlagship().getVariant().addTag(Tags.SHIP_UNIQUE_SIGNATURE);
        cataclysm.getFlagship().getVariant().addTag(Tags.VARIANT_UNBOARDABLE);
        Global.getSector().getMemoryWithoutUpdate().set("$SKR_cataclysm_boss", true);
        cataclysm.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        cataclysm.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        
        cataclysm.addEventListener(new SKR_cataclysmLoot());
        
        
        //add roaming fleets        
        for(Integer i=0; i<15; i++){            
            
            List<String>flagships=new ArrayList<>();
            {
                flagships.add("SKR_cultist_assault");
                flagships.add("SKR_cultist_assault");
                flagships.add("SKR_doctrinaire_assault");
                flagships.add("SKR_zealot_assault");
                flagships.add("SKR_guru_assault");
            }
            Integer size =MathUtils.getRandomNumberInRange(1, flagships.size());
            
            PersonAPI cultist = MagicCampaign.createCaptain(
                    false,
                    null,
                    "",//txt("plague_D_boss"),
                    txt("plague_cultist"),
                    "SKR_cultist",
                    FullName.Gender.ANY,
                    "plague",
                    Ranks.SPACE_CAPTAIN,
                    Ranks.POST_PATROL_COMMANDER,
                    Personalities.AGGRESSIVE,
                    MathUtils.getRandomNumberInRange(2,Math.round(size*1.5f)),
                    MathUtils.getRandomNumberInRange(0,size),
                    OfficerManagerEvent.SkillPickPreference.YES_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE,
                    null //defined skills
            );
            
            List <FleetAssignment> assignment = new ArrayList<>();
            {
                assignment.add(FleetAssignment.PATROL_SYSTEM);
                assignment.add(FleetAssignment.FOLLOW);
                assignment.add(FleetAssignment.ORBIT_AGGRESSIVE);
            }            
            CampaignFleetAPI posse = MagicCampaign.createFleet(
                    txt("plague_cultistFleet"),
                    "plague",
                    FleetTypes.PATROL_MEDIUM,
                    txt("plague_blank"),
                    flagships.get(size-1),
                    cultist,
                    null,
                    size*40,
                    "plague",
                    2f,
                    null,
                    assignment.get(MathUtils.getRandomNumberInRange(0, assignment.size()-1)),
                    cataclysm,
                    false,
                    false
            );            
        }
        
        targetPlagueD.getStarSystem().getTags().add(Tags.THEME_UNSAFE);
        targetPlagueD.getStarSystem().getTags().add(SKR_ids.THEME_PLAGUEBEARER);
                
    }
    
    ////////////////////
    //                //
    //    DEMETER     //
    //                //
    ////////////////////
    
    private static void spawnDemeter(){
        
        List<String> marketDemeter = new ArrayList<>();
        marketDemeter.add("eochu_bres");
                
        List<String> factionDemeter = new ArrayList<>();
        factionDemeter.add("tritachyon");
        
        SectorEntityToken targetDemeter = MagicCampaign.findSuitableTarget(
                marketDemeter,
                factionDemeter,
                "CORE",
                null,
                null,
                null,
                false,
                false,
                false
        );
        
        if(targetDemeter==null){
            LOG.info("NO SYSTEM AVAILABLE FOR DEMETER");
            return;
        } else {
            LOG.info("Adding DEMETER fleet in "+targetDemeter.getStarSystem().getName());
        }        
        
        PersonAPI demeterCaptain = MagicCampaign.createCaptain(
                false,
                null,
                txt("demeterCaptainFN"),
                txt("demeterCaptainLN"),
                "SKR_demeter",
                FullName.Gender.MALE,
                "tritachyon",
                Ranks.SPACE_CHIEF,
                Ranks.POST_ENTREPRENEUR,
                Personalities.CAUTIOUS,
                4,
                -1,
                OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_NO_MISSILE_YES_DEFENSE,
                null
        );
        
        /**        
        createFleet(
            @Nullable String fleetName,
            @Nullable String fleetFaction,
            @Nullable String fleetType,
            @Nullable String flagshipName,
            @Nullable String flagshipVariant,
            boolean flagshipRecovery,
            boolean flagshipAutofit,
            @Nullable PersonAPI captain,
            @Nullable Map<String, Integer> supportFleet,
            boolean supportAutofit,
            int minFP,
            @Nullable String reinforcementFaction,
            @Nullable Float qualityOverride,
            @Nullable SectorEntityToken spawnLocation,
            @Nullable FleetAssignment assignment,
            @Nullable SectorEntityToken assignmentTarget,
            boolean isImportant,
            boolean transponderOn,
            @Nullable String variantsPath
        )
        */
	CampaignFleetAPI demeter = MagicCampaign.createFleet(
                txt("demeterFleet"),
                "independent",
                FleetTypes.FOOD_RELIEF_FLEET,
                txt("demeterShip"),
                "CIV_demeter_standard",
                true,
                false,
                demeterCaptain,
                null,
                false,
                100,
                "tritachyon",
                2f,
                null,
                FleetAssignment.ORBIT_PASSIVE,
                targetDemeter,
                false,
                true,
                null
        );
        demeter.setDiscoverable(false);
        demeter.addTag(Tags.NEUTRINO);
        demeter.getFlagship().getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
        Global.getSector().getMemoryWithoutUpdate().set("$SKR_demeter", false);
    }
    
    ////////////////////
    //                //
    //    TITANIC     //
    //                //
    ////////////////////
    
    private static void spawnTitanic(){
        
        List<String> themesTitanic = new ArrayList<>();
        themesTitanic.add("procgen_no_theme");
        themesTitanic.add(Tags.THEME_UNSAFE);
                
        List<String> entitiesTitanic = new ArrayList<>();
        entitiesTitanic.add(Tags.ACCRETION_DISK);
                
        SectorEntityToken targetTitanic = MagicCampaign.findSuitableTarget(
                null,
                null,
                "CLOSE",
                themesTitanic,
                null,
                entitiesTitanic,
                true,
                true,
                true
        );
        
        if(targetTitanic==null){
            LOG.info("NO SYSTEM AVAILABLE FOR TITANIC");
            return;
        } else {
            LOG.info("Adding TITANIC wreck in "+targetTitanic.getStarSystem().getName());
        }
        
        SectorEntityToken titanicWreck = MagicCampaign.createDerelict(
                "CIV_titanic_standard",
                ShipRecoverySpecial.ShipCondition.WRECKED,
                true,
                1000,
                true,
                targetTitanic.getStarSystem().getStar(),
                0,
                targetTitanic.getStarSystem().getStar().getRadius()*4,
                180
        );
        
        titanicWreck.setName(txt("titanicWreck"));
        titanicWreck.addTag(Tags.NEUTRINO);
        
        MagicCampaign.addSalvage(titanicWreck.getCargo(),
                titanicWreck,
                MagicCampaign.lootType.COMMODITY,
                Commodities.LUXURY_GOODS,
                120
        );
        
        MagicCampaign.addSalvage(titanicWreck.getCargo(),
                titanicWreck,
                MagicCampaign.lootType.COMMODITY,
                Commodities.LOBSTER,
                92
        );
    }
    
    ////////////////////
    //                //
    //     VOULGE     //
    //                //
    ////////////////////
    
    private static void spawnVoulge(){
        
        List<String> themesVoulge = new ArrayList<>();
        themesVoulge.add("procgen_no_theme");
        themesVoulge.add(Tags.THEME_UNSAFE);
        themesVoulge.add(Tags.THEME_INTERESTING_MINOR);
                
        List<String> entitiesVoulge = new ArrayList<>();
        entitiesVoulge.add(Tags.DEBRIS_FIELD);
        entitiesVoulge.add(Tags.GATE);
        entitiesVoulge.add(Tags.SALVAGEABLE);
                
        SectorEntityToken targetVoulge = MagicCampaign.findSuitableTarget(
                null,
                null,
                "CLOSE",
                themesVoulge,
                null,
                entitiesVoulge,
                true,
                true,
                true
        );
        
        if(targetVoulge==null){
            LOG.info("NO SYSTEM AVAILABLE FOR VOULGE");
            return;
        } else {
            LOG.info("Adding VOULGE wreck in "+targetVoulge.getStarSystem().getName());
        }
        
        SectorEntityToken voulgeWreck = MagicCampaign.createDerelict(
                "SKR_voulge_outdated",
                ShipRecoverySpecial.ShipCondition.BATTERED,
                true,
                500,
                true,
                targetVoulge,
                0,
                200,
                180
        );
        
        voulgeWreck.setName(txt("voulgeWreck"));
        voulgeWreck.addTag(Tags.NEUTRINO);
    }
}