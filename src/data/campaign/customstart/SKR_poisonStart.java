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
import data.scripts.util.MagicCampaign;
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
                    PersonAPI ascociateA = MagicCampaign.createCaptain(
                            false,
                            null,
                            null,
                            null,
                            null, 
                            null,
                            Factions.PIRATES,
                            null,
                            null,
                            Personalities.AGGRESSIVE,
                            4,
                            2,
                            OfficerManagerEvent.SkillPickPreference.GENERIC,
                            null
                    );                
                    CampaignFleetAPI fleetA = MagicCampaign.createFleet(
                            txt("poison_fleetA"),
                            Factions.PIRATES,
                            null,
                            txt("poison_flagshipA"),
                            "SKR_poisonivy_mugster",
                            true,
                            false,
                            ascociateA,
                            null,
                            true,
                            60,
                            Factions.PIRATES,
                            0.5f,
                            location,
                            FleetAssignment.RAID_SYSTEM,
                            location,
                            false,
                            false,
                            null
                    );
                    //force spawn near player                
                    fleetA.setLocation(location.getLocation().x+MathUtils.getRandomNumberInRange(-25, 25), location.getLocation().y+MathUtils.getRandomNumberInRange(-25, 25));

                    PersonAPI ascociateB = MagicCampaign.createCaptain(
                            false,
                            null,
                            null,
                            null,
                            null, 
                            null,
                            Factions.PIRATES,
                            null,
                            null,
                            Personalities.CAUTIOUS,
                            6,
                            3,
                            OfficerManagerEvent.SkillPickPreference.GENERIC,
                            null
                    );                
                    CampaignFleetAPI fleetB = MagicCampaign.createFleet(
                            txt("poison_fleetB"),
                            Factions.PIRATES,
                            null,
                            txt("poison_flagshipB"),
                            "SKR_poisonivy_highwayman",
                            true,
                            false,
                            ascociateB,
                            null,
                            true,
                            75,
                            Factions.PIRATES,
                            0.75f,
                            location,
                            FleetAssignment.RAID_SYSTEM,
                            location,
                            false,
                            false,
                            null
                    );
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
