package data.missions.SKR_spaceworthy;

import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "MNC", FleetGoal.ESCAPE, false);
		api.initFleet(FleetSide.ENEMY, "RED", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Recovery Party");
		api.setFleetTagline(FleetSide.ENEMY, "Red Eyes Pirates");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The Lookout must survive!");                
		api.addBriefingItem("But it's the only somewhat combat-grade vessel.");
		
		// Set up the player's fleet    
                api.addToFleet(FleetSide.PLAYER, "SKR_clipper_recovered", FleetMemberType.SHIP, "DCS Lookout", true);		
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, "MNC Quartz", false);    	
                api.addToFleet(FleetSide.PLAYER, "valkyrie_Elite", FleetMemberType.SHIP, "MNC Babylon", false);	            		
                api.addToFleet(FleetSide.PLAYER, "shepherd_Frontier", FleetMemberType.SHIP, "MNC Amanite", false);		
                api.addToFleet(FleetSide.PLAYER, "dram_Light", FleetMemberType.SHIP, "MNC Drunken Lady", false);
                                	
                api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);     
		api.addToFleet(FleetSide.PLAYER, "talon_wing", FleetMemberType.FIGHTER_WING, false);
                api.addToFleet(FleetSide.PLAYER, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
		// Mark a ship as essential, if you want
		api.defeatOnShipLoss("DCS Lookout");
		
		// Set up the enemy fleet
                
		api.addToFleet(FleetSide.ENEMY, "mule_d_pirates_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");	
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");	
		api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");		
		api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
		api.addToFleet(FleetSide.ENEMY, "lasher_d_CS", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
		api.addToFleet(FleetSide.ENEMY, "lasher_d_CS", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
		api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
		api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");

		// Set up the map.
		float width = 12000f;
		float height = 25000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(minX + width * 0.75f, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.25f, minY + height * 0.9f, 1000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.25f, minY + height * 0.5f, 
						 "sensor_array");
		api.addObjective(minX + width * 0.75f, minY + height * 0.5f,
						 "comm_relay");
		api.addObjective(minX + width * 0.33f, minY + height * 0.25f, 
						 "nav_buoy");
		api.addObjective(minX + width * 0.66f, minY + height * 0.75f, 
						 "nav_buoy");
		

		api.addAsteroidField(minX + width*0.5f, minY + height*0.5f, 0, 2500f,
								150f, 200f, 200);
	}

}






