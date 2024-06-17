package data.campaign.customstart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import org.magiclib.util.MagicCampaign;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static data.scripts.util.SKR_txt.txt;
import org.lazywizard.lazylib.MathUtils;

public class SKR_poisonStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(
            Arrays.asList(
                    new String[]{
                        "SKR_poisonivy_scrapper",
                        //"SKR_falcon_p_start",
                        "mule_d_Standard",
                        "SKR_malet_p_standard",
                        "vanguard_pirates_Strike",
                        "gremlin_d_pirates_Strike",
                        "gremlin_d_pirates_Strike",
                        "SKR_affictorP_start",
                        "shade_d_pirates_Assault",
                    }
            )
    );

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PIRATES);
        ExerelinSetupData.getInstance().freeStart = true;

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);

//        data.addScriptBeforeTimePass(
        data.addScript(
            new Script() {
                @Override
                public void run() {

                    //pick random location
                    SectorEntityToken location = null;
                    for (Integer i = 0; i < 9; i++) {

                        List<String> faction = new ArrayList<>();
                        faction.add(Factions.PIRATES);

                        SectorEntityToken token = MagicCampaign.findSuitableTarget(
                                null,
                                faction,
                                "CORE",
                                null,
                                null,
                                null,
                                false,
                                false,
                                false
                        );

                        if (token != null) {
                            location = token;
                            break;
                        }
                    }

                    //spawn location
                    Global.getSector().getMemoryWithoutUpdate().set("$nex_startLocation", location.getId());

                    //spawn former asscociates
                    PersonAPI ascociateA = MagicCampaign.createCaptainBuilder(Factions.PIRATES)
                            .setPersonality(Personalities.AGGRESSIVE)
							.setFactionId(Factions.PIRATES)
							.setLevel(4)
							.setEliteSkillsOverride(2)
                            .setSkillPreference(OfficerManagerEvent.SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE)
							.create();           
                    CampaignFleetAPI fleetA = MagicCampaign.createFleetBuilder()
                            .setFleetName(txt("poison_fleetA"))
                            .setFleetFaction(Factions.PIRATES)
                            .setFlagshipName(txt("poison_flagshipA"))
                            .setFlagshipVariant("SKR_poisonivy_mugster")
                            .setFlagshipAlwaysRecoverable(true)
                            .setFlagshipAutofit(false)
                            .setCaptain(ascociateA)
                            .setSupportAutofit(true)
                            .setMinFP(60)
                            .setReinforcementFaction(Factions.PIRATES)
                            .setQualityOverride(0.5f)
                            .setSpawnLocation(location)
                            .setAssignment(FleetAssignment.RAID_SYSTEM)
                            .setAssignmentTarget(location)
							.create(); 
                    //force spawn near player                
                    fleetA.setLocation(location.getLocation().x+MathUtils.getRandomNumberInRange(-25, 25), location.getLocation().y+MathUtils.getRandomNumberInRange(-25, 25));

                    PersonAPI ascociateB = MagicCampaign.createCaptainBuilder(Factions.PIRATES)
                            .setPersonality(Personalities.CAUTIOUS)
							.setFactionId(Factions.PIRATES)
                            .setLevel(6)
                            .setEliteSkillsOverride(3)
                            .setSkillPreference(OfficerManagerEvent.SkillPickPreference.NO_ENERGY_YES_BALLISTIC_YES_MISSILE_YES_DEFENSE)
							.create();            
                    CampaignFleetAPI fleetB = MagicCampaign.createFleetBuilder()
                            .setFleetName(txt("poison_fleetB"))
                            .setFleetFaction(Factions.PIRATES)
                            .setFlagshipName(txt("poison_flagshipB"))
                            .setFlagshipVariant("SKR_poisonivy_highwayman")
                            .setFlagshipAlwaysRecoverable(true)
                            .setFlagshipAutofit(false)
                            .setCaptain(ascociateB)
                            .setSupportAutofit(true)
                            .setMinFP(75)
                            .setReinforcementFaction(Factions.PIRATES)
                            .setQualityOverride(0.75f)
                            .setSpawnLocation(location)
                            .setAssignment(FleetAssignment.RAID_SYSTEM)
                            .setAssignmentTarget(location)
							.create(); 
                    //force spawn near player     
                    fleetB.setLocation(location.getLocation().x+MathUtils.getRandomNumberInRange(-25, 25), location.getLocation().y+MathUtils.getRandomNumberInRange(-25, 25));

                    //Prevent the MagicBounty from spawning
                    Global.getSector().getMemoryWithoutUpdate().set("$SKR_poisonIvy", true);
                }
            }
        );

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
