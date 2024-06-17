package data.missions.SKR_playground;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "SAS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "???", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Scientific mission");
        api.setFleetTagline(FleetSide.ENEMY, "Unknown contact");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Let's do SCIENCE!");

        // Set up the player's fleet                                  
        api.addToFleet(FleetSide.PLAYER, "SKR_malet_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_malet_p_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_malet_lp_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_adze_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_adze_p_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_adze_lp_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_halligan_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_halligan_p_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_halligan_lp_standard", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "SKR_hedone_razorback", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_augur_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_endymion_standard", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "SKR_557_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_marksman_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_aethernium_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_butterfly_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_obelisk_cairn", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "SKR_tumbleweed_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_tumbleweed_p_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_retiarius_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_bullhorn_combat", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "SKR_cassowary_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SKR_voulge_standard", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.PLAYER, "CIV_blitzkrieg_standard", FleetMemberType.SHIP, "SAS Blitzkrieg", false);
        api.addToFleet(FleetSide.PLAYER, "CIV_demeter_standard", FleetMemberType.SHIP, "SAS Demeter", false);
        api.addToFleet(FleetSide.PLAYER, "CIV_titanic_standard", FleetMemberType.SHIP, "SAS Titanic VII", false);

        if (Global.getSettings().isDevMode()) {
            // secret stuff
            
            
            api.addToFleet(FleetSide.PLAYER, "SKR_siegfried_base", FleetMemberType.SHIP, false);
            
            api.addToFleet(FleetSide.PLAYER, "SKR_poisonivy_mugster", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_balisong_overdrive", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_trailblazer_combat", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_clipper_makeshift", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_clipper_original", FleetMemberType.SHIP, false);

            api.addToFleet(FleetSide.PLAYER, "SKR_cassowaryRH_combat", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_quicksilver_deepDive", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_dawn_moonlight", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_onyx_newMoon", FleetMemberType.SHIP, false);

            api.addToFleet(FleetSide.PLAYER, "SKR_fresnel_combat", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_gawon_avatar", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_nova_falseOmega", FleetMemberType.SHIP, false);

            api.addToFleet(FleetSide.PLAYER, "SKR_keep_safeguard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_rampage_01", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_whiteDwarf_1", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_cataclysm_1", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_cultist_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_fanatic_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_zealot_standard", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.PLAYER, "SKR_guru_standard", FleetMemberType.SHIP, false);
        }

        // Mark a ship as essential, if you want
        //api.defeatOnShipLoss("ISS Black Star");
        // Set up the enemy fleet
        api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
        api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");

        // Set up the map.
        float width = 20000f;
        float height = 12000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // Add two big nebula clouds
        api.addNebula(minX + width * 0.66f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.6f, 1000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.4f, 1000);

        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
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

        api.addAsteroidField(-(minY + height), minY + height, -90, 500f,
                150f, 200f, 100);
    }
}
