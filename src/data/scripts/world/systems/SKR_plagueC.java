package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.convertOrbitWithSpin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantStationFleetManager;
import org.magiclib.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.SKR_txt.txt;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_plagueC {
    
    public void generate(SectorAPI sector, Integer direction) {   
        
        String systemName = txt("plague_C"+MathUtils.getRandomNumberInRange(0, 9));
        
        StarSystemAPI system = sector.createStarSystem(systemName);
        system.setEnteredByPlayer(false);
        system.getTags().add(Tags.THEME_HIDDEN);
        system.getTags().add(Tags.THEME_UNSAFE);
        system.getTags().add("theme_plaguebearer");
        
        system.setBackgroundTextureFilename("graphics/SEEKER/backgrounds/SKR_plagueC.png");
                
        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("plague_"+systemName, // unique id for this star
                                        "star_blue_giant", // id in planets.json
                                        500f,
                                        600);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(200, 225, 255)); // light color in entire system, affects all entities
        
        //simpler random location
        
	system.getLocation().set(
                MathUtils.getPointOnCircumference(
                        new Vector2f(),
                        MathUtils.getRandomNumberInRange(20000, 50000),
                        MathUtils.getRandomNumberInRange(direction-45, direction+45)
                )
        );
        
        SectorEntityToken s1 = system.addCustomEntity("plagueC_stable1", null, "stable_location", "neutral");        
        s1.setCircularOrbit(star, 90, 10000, 1200);
        
        MagicCampaign.addJumpPoint("plagueC_jp0", systemName+txt("plague_jp"), null, star, 0, 700, 150);
        
        //random stuff to the outside
        
        StarSystemGenerator.addOrbitingEntities(
                system,
                star,
                StarAge.YOUNG,
                5, 7, // min/max entities to add
                1000, // radius to start adding at 
                0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true); // whether to use custom or system-name based names
        
        
        String companionName = txt("plague_D"+MathUtils.getRandomNumberInRange(0, 9));
        
        PlanetAPI companion = system.addPlanet(
                "plague_"+companionName, 
                star,
                companionName,
                "star_yellow",
                90, 
                175,
                20000, 
                3000
        );
        system.addCorona(companion, Terrain.CORONA, 200, 10, 1f, 1f);
        system.setType(StarSystemGenerator.StarSystemType.BINARY_FAR);
        system.setSecondary(companion);
            
        //main planet
        PlanetAPI p1 = system.addPlanet(
                "plagueC_1",
                companion,
                txt("plague_C_planet1"),
                "lava",
                120,
                100,
                1250,
                200
        );
        system.addCorona(p1, Terrain.CORONA_AKA_MAINYU, 100, 10, 0.75f, 0.5f);
        p1.getMarket().addCondition(Conditions.ORE_RICH);        
        p1.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
        p1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
        p1.getMarket().addCondition(Conditions.HOT);
        p1.getMarket().addCondition(Conditions.IRRADIATED);
        
        MagicCampaign.addJumpPoint("plagueC_jp1", companionName+txt("plague_jp"), p1, companion, 0, 1250, 200);
        	
        system.addTag(Tags.THEME_REMNANT);
        system.addTag(Tags.THEME_REMNANT_SECONDARY);
        RemnantSeededFleetManager fleets = new RemnantSeededFleetManager(system, 3, 8, 1, 2, 0.05f);
        system.addScript(fleets);
        CampaignFleetAPI station = addRemnantBattlestation(p1, 0, 200, 30, "remnant_station2_Damaged");
        RemnantStationFleetManager activeFleets = new RemnantStationFleetManager(
                station,
                1f,
                0,
                3,
                60f,
                2,
                4
        );
        system.addScript(activeFleets);
        
        SectorEntityToken fieldA = MagicCampaign.createDebrisField(
                "plagueC_debris1",
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
                p1, 
                0, 
                200,
                30
        );
        MagicCampaign.addSalvage(null, fieldA, MagicCampaign.lootType.SUPPLIES, null, 61);
        
        SectorEntityToken relay = system.addCustomEntity(
                "plagueC_relay",
                null, // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "neutral"); // faction
        relay.setCircularOrbitPointingDown(companion, 270-60, 700, 62);
        
        
        
        //main planet
        PlanetAPI p2 = system.addPlanet(
                "plagueC_2",
                companion,
                txt("plague_C_planet2"),
                "arid",
                -120,
                125,
                1250,
                200
        );
        p2.getMarket().addCondition(Conditions.RUINS_SCATTERED);
        p2.getMarket().addCondition(Conditions.FARMLAND_POOR);
        p2.getMarket().addCondition(Conditions.ORGANICS_COMMON);
        p2.getMarket().addCondition(Conditions.HOT);
        p2.getMarket().addCondition(Conditions.HABITABLE);
        
        PersonAPI plagueC = MagicCampaign.createCaptainBuilder("plague")
                .setIsAI(true)
                .setAICoreType(Commodities.ALPHA_CORE)
                .setFirstName(txt("plague_C_boss"))
                .setLastName(txt("plague_C_boss"))
                .setPortraitId("SKR_plagueC")
                .setGender(FullName.Gender.ANY)
                .setFactionId("plague")
                .setRankId(Ranks.SPACE_COMMANDER)
                .setPostId(Ranks.POST_FLEET_COMMANDER)
                .setPersonality(Personalities.AGGRESSIVE)
                .setLevel(8)
                .setEliteSkillsOverride(8)
                .setSkillPreference(OfficerManagerEvent.SkillPickPreference.YES_ENERGY_NO_BALLISTIC_YES_MISSILE_YES_DEFENSE)
				.create();
        
        SectorEntityToken boss = MagicCampaign.createFleetBuilder()
                .setFleetName(txt("plague_C_fleet"))
                .setFleetFaction("plague")
                .setFlagshipName(txt("plague_C_boss"))
                .setFlagshipVariant("SKR_whiteDwarf_1")
                .setCaptain(plagueC)
                .setMinFP(150)
                .setReinforcementFaction(Factions.REMNANTS)
                .setQualityOverride(1f)
                .setSpawnLocation(p2)
                .setAssignment(FleetAssignment.ORBIT_PASSIVE)
                .setAssignmentTarget(p2)
                .setIsImportant(true)
                .setTransponderOn(false)
				.create();
        boss.getCargo().addCommodity(Commodities.ALPHA_CORE, 3);
        boss.getCargo().addHullmods("SKR_plagueLPC", 1);
        boss.setDiscoverable(true);        
        
        system.autogenerateHyperspaceJumpPoints(true, true, true);        
        
        MagicCampaign.hyperspaceCleanup(system);
    }
    
    
    private CampaignFleetAPI addRemnantBattlestation(
            SectorEntityToken orbitFocus,
            float orbitAngle,
            float orbitRadius,
            Integer orbitPeriod,
            String type) {
        Random random = new Random();
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.BATTLESTATION, null);

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, type);
        fleet.getFleetData().addFleetMember(member);

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);

        fleet.setStationMode(true);

        addRemnantStationInteractionConfig(fleet);

        orbitFocus.getStarSystem().addEntity(fleet);
        fleet.setCircularOrbit(orbitFocus, orbitAngle, orbitRadius, orbitPeriod);
        convertOrbitWithSpin(fleet, 5f);

        //fleet.setTransponderOn(true);
        fleet.clearAbilities();
        fleet.addAbility(Abilities.TRANSPONDER);
        fleet.getAbility(Abilities.TRANSPONDER).activate();
        fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);

        fleet.setAI(null);

        boolean damaged = type.toLowerCase().contains("damaged");
        float mult = 25f;
        int level = 20;
        if (damaged) {
                mult = 10f;
                level = 10;
                fleet.getMemoryWithoutUpdate().set("$damagedStation", true);
        }
        
        PersonAPI commander = OfficerManagerEvent.createOfficer(
                        Global.getSector().getFaction(Factions.REMNANTS), level, true);
        if (!damaged) {
                commander.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 3);
        }
        FleetFactoryV3.addCommanderSkills(commander, fleet, random);
        fleet.setCommander(commander);
        fleet.getFlagship().setCaptain(commander);

        member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());

        return fleet;
    }
	
    private void addRemnantStationInteractionConfig(CampaignFleetAPI fleet) {
        fleet.getMemoryWithoutUpdate().set(
                MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, 
                new RemnantStationInteractionConfigGen()
        );		
    }
	
    private class RemnantStationInteractionConfigGen implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        @Override
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

            config.alwaysAttackVsAttack = true;
            config.leaveAlwaysAvailable = true;
            config.showFleetAttitude = false;
            config.showTransponderStatus = false;
            config.showEngageText = false;

            config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                @Override
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
                    new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
                                            postPlayerSalvageGeneration(dialog, context, salvage);
                }
                @Override
                public void notifyLeave(InteractionDialogAPI dialog) {
                }
                @Override
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.aiRetreatAllowed = false;
                    bcc.objectivesAllowed = false;
                }
            };
            return config;
        }
    }
}