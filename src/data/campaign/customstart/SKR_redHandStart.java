package data.campaign.customstart;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.magiclib.util.MagicCampaign;
import exerelin.campaign.ExerelinSetupData;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static data.scripts.util.SKR_txt.txt;

public class SKR_redHandStart extends CustomStart {
	
	protected List<String> ships = new ArrayList<>(
                Arrays.asList(
                        new String[]{
                                "SKR_cassowaryRH_start",
                                //"SKR_bonnetheadP_start",
                                //"CIV_craneP_start",
                                "SKR_falcon p_start",
                                "SKR_colossus3_start",
                                //"SKR_bullyP_start",
                                "SKR_buffalo2_start",
                                "SKR_affictorP_start",
                                "SKR_cerberusP_start",
                                "SKR_cerberusP_start",
                                "SKR_cerberusP_start",
                                "buffalo_pirates_Standard",
                                "phaeton_Standard",
                        }
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
                            PlanetAPI location=null;
                            for(Integer i=0; i<9; i++){
                                List<String> ids = new ArrayList<>();
                                ids.add("penelope2");

                                List<String> themes = new ArrayList<>();
                                themes.add(Tags.THEME_CORE_UNPOPULATED);
                                themes.add(Tags.THEME_MISC);
                                themes.add(Tags.THEME_DERELICT_PROBES);
                                themes.add(Tags.THEME_INTERESTING_MINOR);
                                themes.add(Tags.THEME_RUINS_SECONDARY);

                                List<String> notThemes = new ArrayList<>();
                                notThemes.add("theme_already_occupied");
                                notThemes.add("theme_already_colonized");
                                notThemes.add("theme_hidden");

                                List<String> entities = new ArrayList<>();
                                entities.add(Tags.GAS_GIANT);
                                entities.add(Tags.PLANET);

                                SectorEntityToken token = MagicCampaign.findSuitableTarget(
                                        ids,
                                        null,
                                        "CORE",
                                        themes,
                                        notThemes,
                                        entities, 
                                        false,
                                        true, 
                                        false
                                );
                                
                                if(token!=null && token instanceof PlanetAPI){
                                    location = (PlanetAPI) token;
                                    break;
                                }
                            }
                            
                            //spawn location
                            Global.getSector().getMemoryWithoutUpdate().set("$nex_startLocation", location.getId());
                            
                            //battle debris
                            DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
				300f, // field radius - should not go above 1000 for performance reasons
				1f, // density, visual - affects number of debris pieces
				90f, // duration in days 
				60f); // days the field will keep generating glowing pieces
                            params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.SALVAGE;
                            params.baseDensity = 1;
                            params.density = 1;
                            params.baseSalvageXP = 250;
                            SectorEntityToken debrisWreckage = Misc.addDebrisField(location.getStarSystem(), params, StarSystemGenerator.random);
                            debrisWreckage.setDiscoverable(false);
                            debrisWreckage.setCircularOrbit(
                                    location.getOrbitFocus(),
                                    location.getCircularOrbitAngle(),
                                    location.getCircularOrbitRadius(),
                                    location.getCircularOrbitPeriod()
                            );
                            
//                            debrisWreckage.addDropRandom("freighter_cargo", 50, 200);
                            debrisWreckage.addDropRandom("supply", 100, 1000);
                            debrisWreckage.addDropRandom("weapons1", 75, 250);
                            debrisWreckage.addDropRandom("weapons2", 50, 150);
                            debrisWreckage.addDropRandom("any_hullmod_medium", 10, 1000);
//
                            
                            debrisWreckage.setId("SKR_redHandStart_debris"); 
                            
                            //Hideout
                            SectorEntityToken neutralStation = location.getStarSystem().addCustomEntity(
                                    "SKR_redHandStart_hideout",
                                    txt("RHstart_hideout"),
                                    "station_side06",
                                    "neutral"
                            );
                            neutralStation.setCircularOrbitPointingDown(location, 45, 300, 30);		
                            neutralStation.setCustomDescriptionId("asharu_platform");
                            neutralStation.setInteractionImage("illustrations", "abandoned_station2");
                            Misc.setAbandonedStationMarket("SKR_redHandStart_hideout_market", neutralStation);
                            CargoAPI cargo = neutralStation.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
                            cargo.addFuel(500+(int)(Math.random()*100));
                            cargo.addSupplies(900+(int)(Math.random()*100));
                            
                            WeightedRandomPicker<String> factions = new WeightedRandomPicker<>();
                            factions.add(Factions.PERSEAN, 1);
                            factions.add(Factions.INDEPENDENT, 0.5f);
                            factions.add(Factions.DIKTAT, 2);

                            addFighters(cargo,6,3,0.25f,factions);
                            addWeapons(cargo,12,3,0.25f,factions);

                            //relations
                            for(FactionAPI f : Global.getSector().getAllFactions()){
                                if(!f.isPlayerFaction()){
                                    f.setRelationship(Factions.PLAYER, RepLevel.HOSTILE);
                                }
                            }
                            Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.PIRATES, RepLevel.SUSPICIOUS);
                            Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.SCAVENGERS, RepLevel.INHOSPITABLE);
                            Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.INDEPENDENT, RepLevel.SUSPICIOUS);
                            Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.LUDDIC_PATH, RepLevel.SUSPICIOUS);
                            
                            //Prevent the MagicBounty from spawning
                            Global.getSector().getMemoryWithoutUpdate().set("$SKR_redHand", true);
                        }
                    }
                );
		
		FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4");
	}
        
        protected void addFighters(CargoAPI cargo, int num, int maxTier, float quality, WeightedRandomPicker<String> factionPicker) {


            WeightedRandomPicker<FighterWingSpecAPI> picker = new WeightedRandomPicker<FighterWingSpecAPI>(false);
            
            for (int i = 0; i < factionPicker.getItems().size(); i++) {
                String factionId = factionPicker.getItems().get(i);
                float w = factionPicker.getWeight(i);
                
                FactionAPI faction = Global.getSector().getFaction(factionId);
                
                for (String id : faction.getKnownFighters()) {
                    FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(id);
                    if (spec == null) {
                            throw new RuntimeException("Fighter wing spec with id [" + id + "] not found");
                    }
                    if (spec.getTier() > maxTier) continue;
                    
                    float p = DefaultFleetInflater.getTierProbability(spec.getTier(), quality);
                    p *= w;
                    picker.add(spec, p);
                }
            }

            for (int i = 0; i < num && !picker.isEmpty(); i++) {
                FighterWingSpecAPI spec = picker.pick();

                int count = 2;
                switch (spec.getRole()) {
                case ASSAULT: count = 2; break;
                case BOMBER: count = 2; break;
                case INTERCEPTOR: count = 4; break;
                case FIGHTER: count = 3; break;
                case SUPPORT: count = 2; break;
                }

                cargo.addItems(CargoAPI.CargoItemType.FIGHTER_CHIP, spec.getId(), count);
            }
	}
	
	protected void addWeapons(CargoAPI cargo, int num, int maxTier, float quality, WeightedRandomPicker<String> factionPicker) {
            WeightedRandomPicker<WeaponSpecAPI> picker = new WeightedRandomPicker<WeaponSpecAPI>(true);

            WeightedRandomPicker<WeaponSpecAPI> pd = new WeightedRandomPicker<WeaponSpecAPI>(true);
            WeightedRandomPicker<WeaponSpecAPI> kinetic = new WeightedRandomPicker<WeaponSpecAPI>(true);
            WeightedRandomPicker<WeaponSpecAPI> nonKinetic = new WeightedRandomPicker<WeaponSpecAPI>(true);
            WeightedRandomPicker<WeaponSpecAPI> missile = new WeightedRandomPicker<WeaponSpecAPI>(true);
            WeightedRandomPicker<WeaponSpecAPI> strike = new WeightedRandomPicker<WeaponSpecAPI>(true);

            for (int i = 0; i < factionPicker.getItems().size(); i++) {
                String factionId = factionPicker.getItems().get(i);
                float w = factionPicker.getWeight(i);

                FactionAPI faction = Global.getSector().getFaction(factionId);

                for (String id : faction.getKnownWeapons()) {
                    WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(id);
                    if (spec.getTier() > maxTier) continue;

                    float p = DefaultFleetInflater.getTierProbability(spec.getTier(), quality);
                    p *= w;
                    picker.add(spec, p);

                    String cat = spec.getAutofitCategory();
                    if (cat != null && spec.getSize() != WeaponAPI.WeaponSize.LARGE) {
                        if (CoreAutofitPlugin.PD.equals(cat)) {
                            pd.add(spec, p);
                        } else if (CoreAutofitPlugin.STRIKE.equals(cat)) {
                            strike.add(spec, p);
                        } else if (CoreAutofitPlugin.KINETIC.equals(cat)) {
                            kinetic.add(spec, p);
                        } else if (CoreAutofitPlugin.MISSILE.equals(cat) || CoreAutofitPlugin.ROCKET.equals(cat)) {
                            missile.add(spec, p);
                        } else if (CoreAutofitPlugin.HE.equals(cat) || CoreAutofitPlugin.ENERGY.equals(cat)) {
                            nonKinetic.add(spec, p);
                        }
                    }
                }
            }

            if (num > 0 && !pd.isEmpty()) {
                pickAndAddWeapons(cargo, pd);
                num--;
            }
            if (num > 0 && !kinetic.isEmpty()) {
                pickAndAddWeapons(cargo, kinetic);
                num--;
            }
            if (num > 0 && !missile.isEmpty()) {
                pickAndAddWeapons(cargo, missile);
                num--;
            }
            if (num > 0 && !nonKinetic.isEmpty()) {
                pickAndAddWeapons(cargo, nonKinetic);
                num--;
            }
            if (num > 0 && !strike.isEmpty()) {
                pickAndAddWeapons(cargo, strike);
                num--;
            }

            for (int i = 0; i < num && !picker.isEmpty(); i++) {
                pickAndAddWeapons(cargo, picker);
            }
	}
	
	protected void pickAndAddWeapons(CargoAPI cargo, WeightedRandomPicker<WeaponSpecAPI> picker) {
            WeaponSpecAPI spec = picker.pick();
            if (spec == null) return;

            int count = 2;
            switch (spec.getSize()) {
            case LARGE: count = 2; break;
            case MEDIUM: count = 4; break;
            case SMALL: count = 8; break;
            }
            cargo.addWeapons(spec.getWeaponId(), count);
	}
}
