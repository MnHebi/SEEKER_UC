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
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import data.scripts.util.MagicCampaign;
import java.awt.Color;
import static data.scripts.util.SKR_txt.txt;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_plagueD {
    
    public void generate(SectorAPI sector, Integer direction) {       
        //Misc.generatePlanetConditions(system, StarAge.ANY)
        
        String systemName = txt("plague_D"+MathUtils.getRandomNumberInRange(0, 9));
        
        StarSystemAPI system = sector.createStarSystem(systemName);
        system.setEnteredByPlayer(false);
        system.getTags().add(Tags.THEME_HIDDEN);
        system.getTags().add(Tags.THEME_UNSAFE);
        system.getTags().add("theme_plaguebearer");
        
        system.setBackgroundTextureFilename("graphics/SEEKER/backgrounds/SKR_plagueD.png");
                
        // create the star and generate the hyperspace anchor for this system
        PlanetAPI star = system.initStar("plague_"+systemName, // unique id for this star
                                        "star_orange", // id in planets.json
                                        400,
                                        200);		// radius (in pixels at default zoom)
	system.setLightColor(new Color(255, 200, 75)); // light color in entire system, affects all entities
        
        //simpler random location
        
	system.getLocation().set(
                MathUtils.getPointOnCircumference(
                        new Vector2f(),
                        MathUtils.getRandomNumberInRange(20000, 50000),
                        MathUtils.getRandomNumberInRange(direction-45, direction+45)
                )
        );
        
        PlanetAPI p1 = system.addPlanet(
                "plagueD_1",
                star,
                txt("plague_D_planet1"),
                "rocky_metallic",
                160,
                100, 
                850,
                -180
        );
        p1.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
        p1.getMarket().addCondition(Conditions.HOT);
        p1.getMarket().addCondition(Conditions.HIGH_GRAVITY);
        p1.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        SectorEntityToken mining1 = system.addCustomEntity("plagueD_mining", null, "station_mining", "neutral");
        mining1.setCircularOrbitPointingDown(p1, 250, 150, -20);
        mining1.setDiscoverable(true);
        
        MagicCampaign.createJumpPoint("plagueD_jp1", systemName+txt("plague_jp"), p1, star, 280, 850, -180);
        
        SectorEntityToken s1 = system.addCustomEntity("plagueD_stable1", null, "stable_location", "neutral");
        s1.setCircularOrbit(star, -40, 800, -180);
        
        //asteroid belt
        system.addAsteroidBelt(star, 300, 1200, 150, -260, -280);
        system.addRingBand(star, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 1200, -270, Terrain.ASTEROID_BELT, null);
        
        SectorEntityToken c1 = system.addCustomEntity("plagueD_cache1", null, "weapons_cache_high", "neutral");
        c1.setCircularOrbit(star, -125, 1200, -270);
        c1.setDiscoverable(true);
        SectorEntityToken c2 = system.addCustomEntity("plagueD_cache2", null, "equipment_cache", "neutral");
        c2.setCircularOrbit(star, 125, 1200, -270);
        c2.setDiscoverable(true);
        
        PlanetAPI p2 = system.addPlanet(
                "plagueD_2",
                star,
                txt("plague_D_planet2"),
                "jungle",
                120,
                150,
                1750,
                -360
        );
        p2.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
        p2.getMarket().addCondition(Conditions.FARMLAND_ADEQUATE);
        p2.getMarket().addCondition(Conditions.ORGANICS_PLENTIFUL);
        p2.getMarket().addCondition(Conditions.INIMICAL_BIOSPHERE);
        p2.getMarket().addCondition(Conditions.HABITABLE);
        SectorEntityToken habitat1 = system.addCustomEntity("plagueD_habitat", null, "orbital_habitat", "neutral");
        habitat1.setCircularOrbitPointingDown(p2, -40, 250, -66);
        habitat1.setDiscoverable(true);
        SectorEntityToken fieldA = MagicCampaign.createDebrisField(
                "plagueD_debris1", 
                250,
                1,
                99999,
                0, 
                250, 
                0,
                null, 
                0,
                0.5f,
                true,
                null,
                p2,
                140,
                250,
                -66
        );
        MagicCampaign.addSalvage(null, fieldA, MagicCampaign.lootType.COMMODITY, Commodities.BETA_CORE, 2);
                
        Map<String,Integer>retinue=new HashMap<>();
        retinue.put("SKR_guru_assault", 1);
        retinue.put("SKR_guru_standard", 1);
        retinue.put("SKR_guru_support", 1);
        retinue.put("SKR_zealot_assault", 2);
        retinue.put("SKR_zealot_standard", 2);
        retinue.put("SKR_zealot_support", 2);
        retinue.put("SKR_fanatic_assault", 3);
        retinue.put("SKR_fanatic_standard", 3);
        retinue.put("SKR_fanatic_support", 3);
        retinue.put("SKR_cultist_assault", 3);
        retinue.put("SKR_cultist_standard", 3);
        retinue.put("SKR_cultist_support", 3);
        
        PersonAPI plagueD = MagicCampaign.createCaptain(
                true,
                Commodities.ALPHA_CORE,
                "Cataclysm",
                "Cataclysm",
                "SKR_plagueD",
                FullName.Gender.ANY,
                "plague",
                Ranks.SPACE_COMMANDER,
                Ranks.POST_FLEET_COMMANDER,
                Personalities.AGGRESSIVE,
                10,
                10,
                OfficerManagerEvent.SkillPickPreference.GENERIC,
                null //defined skills
        );
        
	SectorEntityToken boss = MagicCampaign.createFleet(
                txt("plague_D_fleet"),
                "plague",
                null,
                txt("plague_D_boss"),
                "SKR_cataclysm_1",
                plagueD,
                retinue,
                0,
                null,
                2f,
                null,
                FleetAssignment.ORBIT_PASSIVE,
                p2,
                true,
                false
        );
        boss.getCargo().addCommodity(Commodities.ALPHA_CORE, 4);
        boss.getCargo().addHullmods("SKR_plagueLPC", 1);
        boss.setDiscoverable(true);
        
        //old gate
        SectorEntityToken gate1 = system.addCustomEntity("plagueD_gate", null, "inactive_gate", "neutral");
        gate1.setCircularOrbit(star, 90, 12000, -9000);
        gate1.setDiscoverable(true);
        SectorEntityToken fieldB = MagicCampaign.createDebrisField(
                "plagueD_debris2", 
                500,
                2,
                99999,
                0, 
                500, 
                0,
                null, 
                0,
                0.5f,
                true,
                null,
                star,
                90,
                12000,
                -9000
        );
        
        //random stuff to the outside
        
        StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                        5, 7, // min/max entities to add
                        2750, // radius to start adding at 
                        0, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                        true); // whether to use custom or system-name based names
        
        system.autogenerateHyperspaceJumpPoints(true, true, true);        
        
        MagicCampaign.hyperspaceCleanup(system);
    }
}