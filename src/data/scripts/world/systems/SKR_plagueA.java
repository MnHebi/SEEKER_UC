package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import data.scripts.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.SKR_txt.txt;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_plagueA {
    
    public void generate(SectorAPI sector, Integer direction) {   
        String systemName = txt("plague_A"+MathUtils.getRandomNumberInRange(0, 9));
        
        StarSystemAPI system = sector.createStarSystem(systemName);
        
        system.setEnteredByPlayer(false);
        system.getTags().add(Tags.THEME_HIDDEN);
        system.getTags().add(Tags.THEME_UNSAFE);
        system.getTags().add("theme_plaguebearer");
               
        system.setBackgroundTextureFilename("graphics/SEEKER/backgrounds/SKR_plagueA.png");
                
        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("plague_"+systemName, // unique id for this star
                                        "star_browndwarf", // id in planets.json
                                        300f,
                                        50);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(200, 125, 75)); // light color in entire system, affects all entities
        
        
//        final HyperspaceTerrainPlugin hyper = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
//        final int[][] cells = hyper.getTiles();
//        final float cellSize = hyper.getTileSize();
//        
//	system.getLocation().set(
//                MathUtils.getPointOnCircumference(
//                        new Vector2f(),
//                        MathUtils.getRandomNumberInRange(20000, Math.min(cells.length, cells[0].length)*cellSize*0.6f),
//                        MathUtils.getRandomNumberInRange(-120, -150)
//                )
//        );

        //simpler random location
        
	system.getLocation().set(
                MathUtils.getPointOnCircumference(
                        new Vector2f(),
                        MathUtils.getRandomNumberInRange(20000, 50000),
                        MathUtils.getRandomNumberInRange(direction-45, direction+45)
                )
        );
        
        PlanetAPI p1 = system.addPlanet(
                "plagueA_1",
                star,
                txt("plague_A_planet1"),
                "desert",
                -120,
                125,
                900,
                200
        );
        p1.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
        p1.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
        p1.getMarket().addCondition(Conditions.ORE_MODERATE);
        p1.getMarket().addCondition(Conditions.HOT);
        p1.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
        
        MagicCampaign.createJumpPoint("plagueA_jp1", systemName+txt("plague_jp"), p1, star, 0, 900, 200);
        
        SectorEntityToken s1 = system.addCustomEntity("plagueA_stable1", null, "stable_location", "neutral");
        s1.setCircularOrbit(star, -240, 900, 200);
        
        PlanetAPI p2 = system.addPlanet(
                "plagueA_2",
                star,
                txt("plague_A_planet2"),
                "gas_giant",
                120,
                250,
                2000,
                600
        );
        system.addAsteroidBelt(p2, 50, 450, 50, 30, 40);
        system.addRingBand(p2, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 450, 35, null, null);
        system.addPlanet(
                "plagueA_2a",
                p2,
                txt("plague_A_planet2a"),
                "barren",
                60,
                40,
                550,
                75
        );
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
                p2, 
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
        
           //testing
//        CargoAPI cargo = MagicCampaign.buildCargo(null, MagicCampaign.lootType.FUEL, null, 99999);
//        cargo = MagicCampaign.buildCargo(cargo, MagicCampaign.lootType.CREW, null, 99999);
//        cargo = MagicCampaign.buildCargo(cargo, MagicCampaign.lootType.COMMODITY, Commodities.ALPHA_CORE, 99);
//        cargo = MagicCampaign.buildCargo(cargo, MagicCampaign.lootType.FIGHTER, "SKR_beetle_wing", 99999);
//        cargo = MagicCampaign.buildCargo(cargo, MagicCampaign.lootType.HULLMOD, "SKR_plagueLPC", 99999);
//        cargo = MagicCampaign.buildCargo(cargo, MagicCampaign.lootType.WEAPON, "SKR_kaleidoscope", 99999);        
//        MagicCampaign.addSalvageCargo(fieldA, cargo);
        
//        Map<String,Integer>skills=new HashMap<>();
//        skills.put(Skills.COMBAT_ENDURANCE, 1);
//        skills.put(Skills.MISSILE_SPECIALIZATION, 1);
//        skills.put(Skills.DAMAGE_CONTROL, 1);
//        skills.put(Skills.IMPACT_MITIGATION, 1);
//        skills.put(Skills.DEFENSIVE_SYSTEMS, 1);
//        skills.put(Skills.ADVANCED_COUNTERMEASURES, 1);
//        skills.put(Skills.EVASIVE_ACTION, 1);
//        skills.put(Skills.FIGHTER_DOCTRINE, 3);
//        skills.put(Skills.CARRIER_COMMAND, 3);
//        skills.put(Skills.WING_COMMANDER, 3);
        
        PersonAPI plagueA = MagicCampaign.createCaptain(
                true,
                Commodities.GAMMA_CORE,
                "Safeguard",
                "Safeguard",
                "SKR_plagueA",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.AGGRESSIVE,
                4,
                4,
                OfficerManagerEvent.SkillPickPreference.GENERIC,
                null
//                skills
        );
        
        /**
        * Creates a fleet with a defined flagship and optional escort
        * 
        * @param fleetName
        * @param fleetFaction
        * @param fleetType
        * campaign.ids.FleetTypes, default to FleetTypes.PERSON_BOUNTY_FLEET
        * @param flagshipName
        * Optional flagship name
        * @param flagshipVariant
        * @param captain
        * PersonAPI, can be NULL for random captain, otherwise use createCaptain() 
        * @param supportFleet
        * Optional escort ship VARIANTS and their NUMBERS
        * @param minFP
        * Minimal fleet size, can be used to adjust to the player's power, set to 0 to ignore
        * @param reinforcementFaction
        * Reinforcement faction, if the fleet faction is a "neutral" faction without ships
        * @param qualityOverride
        * Optional ship quality override, default to 2 (no D-mods) if null or <0
        * @param spawnLocation
        * Where the fleet will spawn, default to assignmentTarget if NULL
        * @param assignment
        * campaign.FleetAssignment, default to orbit aggressive
        * @param assignementTarget
        * @param isImportant
        * @param transponderOn
        * @return 
        */
	SectorEntityToken boss = MagicCampaign.createFleet(
                txt("plague_A_fleet"),
                "plague",
                null,
                txt("plague_A_boss"),
                "SKR_keep_safeguard",
                plagueA,
                null,
                200,
                Factions.DERELICT,
                0.5f,
                null,
                FleetAssignment.ORBIT_PASSIVE,
                p2,
                true,
                false
        );
        boss.getCargo().addCommodity(Commodities.ALPHA_CORE, 1);
        boss.getCargo().addHullmods("SKR_plagueLPC", 1);
        boss.setDiscoverable(true);
        
        SectorEntityToken relay = system.addCustomEntity("plagueA_relay",
                null, // name - if null, defaultName from custom_entities.json will be used
                "comm_relay", // type of object, defined in custom_entities.json
                "neutral"); // faction
        relay.setCircularOrbitPointingDown(star, 270-60, 4500, 1250);
        
        //random stuff to the outside
        
        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                        4, 6, // min/max entities to add
                        2750, // radius to start adding at 
                        0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                        true); // whether to use custom or system-name based names
                
        system.autogenerateHyperspaceJumpPoints(true, true, true);        
        
        MagicCampaign.hyperspaceCleanup(system);
    }
}