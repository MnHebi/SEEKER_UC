package data.campaign.bosses;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import data.campaign.ids.SKR_ids;
import org.magiclib.util.MagicCampaign;
import static data.scripts.util.SKR_plagueEffect.LPC;
import static data.scripts.util.SKR_plagueEffect.SOURCES;
import static data.scripts.util.SKR_plagueEffect.WEAPONS;
import static data.scripts.util.SKR_txt.txt;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;

/**
 * @author Tartiflette
 */

public class SKR_safeguardLoot implements FleetEventListener{
    
    private final String SAFE_DROP_ALREADY = "$safe_drop";
    
    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        
        // ignore that whole ordeal if the ship already dropped
        if(Global.getSector().getMemoryWithoutUpdate().contains(SAFE_DROP_ALREADY) 
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(SAFE_DROP_ALREADY)){
            return;            
        }
        
        if(fleet.getFlagship()==null || !fleet.getFlagship().getHullSpec().getBaseHullId().equals("SKR_keep")){
            //boss is dead, spawn a derelict            
            SectorEntityToken wreck;
            if(fleet.isInHyperspace()){
                //in hyperspace, just leave it there
                wreck = Global.getSector().getHyperspace().addCustomEntity(SKR_ids.BOSS_SAFEGUARD_WRECK, txt("plague_A_wreck"), SKR_ids.BOSS_SAFEGUARD_WRECK, Factions.NEUTRAL);
                wreck.setFixedLocation(fleet.getLocation().x, fleet.getLocation().y);
            } else {
                //find a reference placement
                StarSystemAPI system = fleet.getStarSystem();
                //spawn the derelict object
                wreck = system.addCustomEntity(SKR_ids.BOSS_SAFEGUARD_WRECK, txt("plague_A_wreck"), SKR_ids.BOSS_SAFEGUARD_WRECK, Factions.NEUTRAL);
                wreck.setLocation(fleet.getLocation().x, fleet.getLocation().y);
                MagicCampaign.placeOnStableOrbit(wreck, true);
                system.removeTag(SKR_ids.THEME_PLAGUEBEARER);
            }
            
            wreck.getMemoryWithoutUpdate().set("$abandonedStation", true);
            
            MarketAPI wreckMarket = Global.getFactory().createMarket(SKR_ids.BOSS_SAFEGUARD_MARKET, txt("plague_A_wreckMarket"), 0);
            wreckMarket.setPrimaryEntity(wreck);
            wreckMarket.setFactionId(Factions.NEUTRAL);
            wreckMarket.addCondition(Conditions.ABANDONED_STATION);
            wreckMarket.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            wreckMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            ((StoragePlugin) wreckMarket.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            wreck.setMarket(wreckMarket);
//            wreck.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addHullmods("SKR_plagueLPC", 1);
            wreck.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addCommodity(Commodities.ALPHA_CORE, 1);
            wreck.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addSpecial(new SpecialItemData(SKR_ids.BOSS_SAFEGUARD_ITEM, null), 1);
            
            
            //add some juicy loot to ensure _something_ drops
            for(Integer i=0; i<20; i++){
                String weapon = WEAPONS.get(MathUtils.getRandomNumberInRange(0, WEAPONS.size()-1));
                i+=Math.round(SOURCES.get(weapon));                
                wreck.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addWeapons(weapon, 1);
            }
            String lpc = LPC.get(MathUtils.getRandomNumberInRange(0, LPC.size()-1));             
            wreck.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addFighters(lpc, 1);
            
            CoreLifecyclePluginImpl.addJunk(wreckMarket);
                
            //set memkey that the wreck must never spawn
            Global.getSector().getMemory().set(SAFE_DROP_ALREADY,true);            
            Global.getSector().getMemory().set("$SKR_safeguard_boss",false);            
//            Global.getSector().getMemory().set("$SKR_safeguard",true);

            //check around if there is an existing wreck to remove just in case
            List<SectorEntityToken>wrecks = fleet.getStarSystem().getEntitiesWithTag(Tags.WRECK);
            if(!wrecks.isEmpty()){
                for (SectorEntityToken t : wrecks){
                    if(t.getCustomEntitySpec().getSpriteName().startsWith("SKR_keep")){
                        fleet.getStarSystem().removeEntity(t);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
    }
}
