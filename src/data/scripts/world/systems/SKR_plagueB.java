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
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import data.scripts.util.MagicCampaign;
import java.awt.Color;
//import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
//import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_plagueB {
    
    public void generate(SectorAPI sector, Integer direction) {       
        
        String systemName = txt("plague_B"+MathUtils.getRandomNumberInRange(0, 9));
        
        StarSystemAPI system = sector.createStarSystem(systemName);
        system.setEnteredByPlayer(false);
        system.getTags().add(Tags.THEME_HIDDEN);
        system.getTags().add(Tags.THEME_UNSAFE);
        system.getTags().add("theme_plaguebearer");
        
        system.setBackgroundTextureFilename("graphics/SEEKER/backgrounds/SKR_plagueB.png");
                
        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("plague_"+systemName, // unique id for this star
                                        "star_white", // id in planets.json
                                        150f,
                                        150);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(75, 75, 150)); // light color in entire system, affects all entities
        

        //simpler random location
        
	system.getLocation().set(
                MathUtils.getPointOnCircumference(
                        new Vector2f(),
                        MathUtils.getRandomNumberInRange(20000, 50000),
                        MathUtils.getRandomNumberInRange(direction-45, direction+45)
                )
        );
        
        //research station
        SectorEntityToken research1 = system.addCustomEntity("plagueB_research", null, "station_research", "neutral");
        research1.setCircularOrbitPointingDown(star, 180, 750, 90);
        research1.setDiscoverable(true);
               
        //remote planet
        PlanetAPI p1 = system.addPlanet(
                "plagueB_1",
                star,
                txt("plague_B_planet1"),
                "barren",
                0,
                225,
                2500,
                190
        );
        p1.getMarket().addCondition(Conditions.ORE_SPARSE);
        p1.getMarket().addCondition(Conditions.RARE_ORE_ULTRARICH);
        p1.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
        p1.getMarket().addCondition(Conditions.DARK);
        p1.getMarket().addCondition(Conditions.COLD);
        p1.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        
        //jump point from planet
        MagicCampaign.createJumpPoint("plagueB_jp1", systemName + txt("plague_jp"), p1, star, 60, 2500, 300);
        
        //2 stable locations
        SectorEntityToken s1 = system.addCustomEntity("plagueB_stable1", null, "stable_location", "neutral");
        s1.setCircularOrbit(star, -240, 2900, 220);
        SectorEntityToken s2 = system.addCustomEntity("plagueB_stable2", null, "stable_location", "neutral");
        s2.setCircularOrbit(star, 60, 2900, 220);
        
        PersonAPI plagueB = MagicCampaign.createCaptain(
                true,
                Commodities.ALPHA_CORE,
                "Rampage",
                "Rampage",
                "SKR_plagueB",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.RECKLESS,
                6,
                6,
                OfficerManagerEvent.SkillPickPreference.GENERIC,
                null
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
                txt("plague_B_fleet"),
                "plague",
                null,
                txt("plague_B_boss"),
                "SKR_rampage_01",
                plagueB, //officer
                null, //no escort fleet
                -1, //no support fleet
                null, //no support faction
                null, //no quallity override
                null,
                FleetAssignment.ORBIT_PASSIVE,
                p1,
                true,
                false
        );
        boss.getCargo().addCommodity(Commodities.ALPHA_CORE, 2);
        boss.getCargo().addHullmods("SKR_plagueLPC", 1);
        boss.setDiscoverable(true);
        
        
        //random stuff to the outside
        
        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                        4, 6, // min/max entities to add
                        3000, // radius to start adding at 
                        0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                        true); // whether to use custom or system-name based names
        
        system.autogenerateHyperspaceJumpPoints(true, true, true);        
        
        MagicCampaign.hyperspaceCleanup(system);
    }
}