package data.campaign.customstart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import org.magiclib.util.MagicCampaign;
import org.magiclib.util.MagicVariables;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SKR_clipperStart extends CustomStart {

    protected List<String> ships = new ArrayList<>(
            Arrays.asList(
                    new String[]{
                        "SKR_clipper_original",}
            )
    );

    @Override
    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER);
        ExerelinSetupData.getInstance().freeStart = true;

        CharacterCreationData data = (CharacterCreationData) memoryMap.get(MemKeys.LOCAL).get("$characterData");

        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds(dialog, data, null, ships);

        data.addScriptBeforeTimePass(
                new Script() {
            @Override
            public void run() {

                //pick random location
                SectorEntityToken location = null;
                for (Integer i = 0; i < 9; i++) {

                    List<String> themes = new ArrayList<>();
                    themes.add(Tags.THEME_INTERESTING);
                    themes.add(Tags.THEME_RUINS_MAIN);

                    List<String> notThemes = new ArrayList<>();
                    notThemes.add(MagicVariables.AVOID_COLONIZED_SYSTEM);
                    notThemes.add(MagicVariables.AVOID_BLACKHOLE_PULSAR);
                    notThemes.add(MagicVariables.AVOID_OCCUPIED_SYSTEM);
                    notThemes.add(Tags.THEME_UNSAFE);
                    notThemes.add("theme_hidden");

                    List<String> entities = new ArrayList<>();
                    entities.add(Tags.GAS_GIANT);

                    SectorEntityToken token = MagicCampaign.findSuitableTarget(
                            null,
                            null,
                            "CLOSE",
                            themes,
                            notThemes,
                            entities,
                            false,
                            true,
                            false
                    );

                    if (token != null) {
                        location = token;
                        break;
                    }
                }

                //spawn location
                Global.getSector().getMemoryWithoutUpdate().set("$nex_startLocation", location.getId());

                //relations
                //neutral to *everyone*
                for(FactionAPI faction : Global.getSector().getAllFactions()){
                    if(faction!=Global.getSector().getFaction(Factions.PLAYER)){
                        Global.getSector().getFaction(Factions.PLAYER).setRelationship(faction.getId(), RepLevel.NEUTRAL);
                    }
                }

                //Prevent the MagicBounty from spawning
                Global.getSector().getMemoryWithoutUpdate().set("$SKR_clipper", true);
            }
        }
        );

        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
    }
}
