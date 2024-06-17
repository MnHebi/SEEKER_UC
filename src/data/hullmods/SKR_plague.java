package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.SKR_plagueEffect;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;
//import java.util.HashMap;
import java.util.Map;

public class SKR_plague extends BaseHullMod {    
    
    //apply the effect    
    private final String EFFECT=txt("plagueEffect");
    
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {        
        float debuff = SKR_plagueEffect.getTotalInfectionCost(stats.getVariant());
        if(debuff==0){
            stats.getVariant().getHullMods().remove("SKR_plagueWarning");
        } 
        else {
            float maxCR = 0.7f*stats.getMaxCombatReadiness().computeMultMod();
            stats.getMaxCombatReadiness().modifyFlat(id, Math.max(-debuff*0.01f,-maxCR), EFFECT);
        }
    }
    
    //description
    private final String DESC0=txt("plagueWarning");
    private final String DESC1=txt("plagueWeak");
    private final String DESC2=txt("plagueMild");
    private final String DESC3=txt("plagueStrong");
    private final String DESC4=""+SKR_plagueEffect.RATES.get("WEAK");
    private final String DESC5=""+SKR_plagueEffect.RATES.get("MILD");
    private final String DESC6=""+SKR_plagueEffect.RATES.get("STRONG");
    private final String DESC7=Math.round(100*(1-SKR_plagueEffect.HSS))+txt("%");
    private final String DESC8=Global.getSettings().getHullModSpec("hardened_subsystems").getDisplayName();
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        
        if (index == 0) return DESC0;        
        if (index == 1) return DESC1;
        if (index == 2) return DESC2;
        if (index == 3) return DESC3;        
        if (index == 4) return DESC4;
        if (index == 5) return DESC5;
        if (index == 6) return DESC6;        
        if (index == 7) return DESC7;       
        if (index == 8) return DESC8;
        return null;
    }
    
    //detailed description    
    private final Color HL=Global.getSettings().getColor("hColor");
    private final Color BAD=Misc.getNegativeHighlightColor();
    private final String TTIP0 = txt("plagueTitle");
    private final String TTIP1 = txt("plagueTxt1");
    private final String TTIP2 = txt("plagueTxt2");
    private final String TTIP3 = txt("plagueTxt3");
    private final String TTIP4 = txt("plagueSource1");
    private final String TTIP5 = txt("plagueSource2");
    private final String TTIP6 = txt("plagueHullmod1");
    private final String TTIP7 = txt("plagueHullmod2");
    private final String TTIP8 = txt("plagueHullmod3");
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
	
        Map<String,Float> sources = SKR_plagueEffect.getDebuffs(ship.getVariant());
        float total=0;
        for(String s : sources.keySet()){
            total+=sources.get(s);
        }
        boolean reduced=false;
        float mult=1;
        if(ship.getVariant().getHullMods().contains("hardened_subsystems")){
            reduced=true;
            mult=SKR_plagueEffect.HSS;
        }
        
        //title
        tooltip.addSectionHeading(TTIP0, Alignment.MID, 15);        
        
        //total effect
        LabelAPI global = tooltip.addPara(
                    TTIP1
                    + sources.size()
                    + TTIP2
                    + Math.round(total)
                    + TTIP3
                    , 10);
        global.setHighlightColors(HL,BAD);
        global.setHighlight(""+sources.size(), ""+(Math.round(total)));
        
        if(reduced){
            LabelAPI hullmod = tooltip.addPara(
                    TTIP6
                    + Math.round(total*mult)
                    + TTIP7
                    + DESC8
                    + TTIP8,
                    10
            );
            hullmod.setHighlightColors(BAD,HL);
            hullmod.setHighlight(""+Math.round(total*mult),DESC8);
        }
        
        //detailed breakdown
        tooltip.setBulletedListMode("    - ");  
        for(String s : sources.keySet()){
            
            String source=ship.getVariant().getWeaponSpec(s).getWeaponName();
            
            int effect = Math.round(sources.get(s));
            
            tooltip.addPara(source
                    + TTIP4
                    + Math.round(-effect*mult)
                    + TTIP5,
                    3,
                    HL,
                    ""+Math.round(-effect*mult)
            );
        }
        tooltip.setBulletedListMode(null);
    }
}