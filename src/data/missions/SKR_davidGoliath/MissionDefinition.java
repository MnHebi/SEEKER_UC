package data.missions.SKR_davidGoliath;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
//import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "MSA", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "SFU", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Your Starfaring Armada");
		api.setFleetTagline(FleetSide.ENEMY, "Scavengers");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The Lookout must survive!");                
		
		// Set up the player's fleet    
//                api.addToFleet(FleetSide.PLAYER, "SKR_pinnace_standard", FleetMemberType.SHIP, "SAS Lookout", true);
                api.addToFleet(FleetSide.PLAYER, "SKR_clipper_original", FleetMemberType.SHIP, "SAS Lookout", true);		
                api.addToFleet(FleetSide.PLAYER, "hammerhead_Balanced", FleetMemberType.SHIP, false);            		
                api.addToFleet(FleetSide.PLAYER, "sunder_Assault", FleetMemberType.SHIP, false);		
                api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);    	
                api.addToFleet(FleetSide.PLAYER, "brawler_Assault", FleetMemberType.SHIP, false);	
                
		// Mark a ship as essential, if you want
		api.defeatOnShipLoss("SAS Lookout");
		
		// Set up the enemy fleet
                
		api.addToFleet(FleetSide.ENEMY, "SKR_siegfried_aaa_base", FleetMemberType.SHIP, "SFU Wolverine", true);
                api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);

		// Set up the map.
		float width = 14000f;
		float height = 16000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 3000);
		api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 3000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 12; i++) {
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
		api.addObjective(minX + width * (0.6f +(float) Math.random()*0.1f), minY + height * 0.6f, 
						 "sensor_array");
		api.addObjective(minX + width * (0.4f +(float) Math.random()*0.1f), minY + height * 0.4f,
						 "comm_relay");
//		api.addObjective(minX + width * 0.33f, minY + height * 0.25f, 
//						 "nav_buoy", BattleObjectiveAPI.Importance.NORMAL);
		api.addObjective(minX + width * 0.5f, minY + height * 0.85f, 
						 "nav_buoy");
		

//		api.addAsteroidField(minX + width*0.5f, minY + height*0.5f, 0, 500f,
//								150f, 200f, 100);
                
                api.addPlugin(new Plugin(width,height));
	}
        
    private final class Plugin extends BaseEveryFrameCombatPlugin {

        private boolean done = false;
        private final float mapX;
        private final float mapY;
        private float timer = 5f;

        private Plugin(float mapX, float mapY) {
            this.mapX = mapX;
            this.mapY = mapY;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (done || Global.getCombatEngine() == null || Global.getCombatEngine().isPaused()) {
                return;
            }

            timer -= amount;
            if (timer <= 0f) {
                for (FleetMemberAPI member : Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getReservesCopy()) {
                    if (!Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedCopy().contains(member)) {
                        Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).spawnFleetMember(member, getSafeSpawn(FleetSide.ENEMY, mapX, mapY), 270f, 1f);
                    }
                }
                done = true;
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
            }

                private Vector2f getSafeSpawn(FleetSide side, float mapX, float mapY) {
            Vector2f spawnLocation = new Vector2f();

            spawnLocation.x = MathUtils.getRandomNumberInRange(-mapX / 2, mapX / 2);
            if (side == FleetSide.PLAYER) {
                spawnLocation.y = (-mapY / 2f);

            } else {
                spawnLocation.y = mapY / 2;
            }

            return spawnLocation;
        }
    }
}






