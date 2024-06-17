package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.util.SKR_txt.txt;
import org.lazywizard.lazylib.MathUtils;

public class SKR_farm extends BaseHullMod {    
    private final float REVENUE_STREAM=1000;
    private final IntervalUtil tic = new IntervalUtil (10,10);
    private final String CREDITS= txt("farm");
    
    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        //skip if mothballed
        if(member.isMothballed())return;
        
        tic.advance(amount);
        if(tic.intervalElapsed()){
            if (
                    member.getFleetData() != null 
                    && member.getFleetData().getFleet() != null 
                    && member.getFleetCommander().isPlayer()
                    ){

                //nothing in hyperspace
                if(member.getFleetData().getFleet().isInHyperspace())return;
                
                int light=0;
                int population=0;
                for (PlanetAPI p : member.getFleetData().getFleet().getContainingLocation().getPlanets()){
                    if(p.isStar() && !p.getSpec().isBlackHole() && !p.getSpec().isPulsar()){
                        light++;
                        continue;
                    }
                    if(p.getMarket()!=null && !p.getMarket().getFaction().isHostileTo(Factions.PLAYER)){
                        population+=Math.max(p.getMarket().getSize()-4,0);
                    }
                }

                if(light>0 && population>0){
//                    Global.getSector().getPlayerFleet().getCargo().getCredits().add(Math.min(population*REVENUE_STREAM,2500));
                    float credits= population*REVENUE_STREAM*MathUtils.getRandomNumberInRange(0.8f,1.1f);
                    Global.getSector().getPlayerFleet().getCargo().getCredits().add(credits);
                    member.getFleetData().getFleet().addFloatingText((int)credits+CREDITS, Misc.getTextColor(), 0.5f);
                }
            }
        }
    }
}
