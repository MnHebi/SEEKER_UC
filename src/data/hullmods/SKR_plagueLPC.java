package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.SKR_plagueEffect;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;

public class SKR_plagueLPC extends BaseHullMod {    

    private final float REFIT_BONUS=0.05f;
    private final float CR_LOSS = SKR_plagueEffect.RATES.get("STRONG");
    
    private final String DESC0=txt("plagueWarning");
    private final String DESC1=""+SKR_plagueEffect.RATES.get("STRONG");
    private final String DESC2=Math.round(100*(1-SKR_plagueEffect.HSS))+txt("%");
    private final String DESC3=Global.getSettings().getHullModSpec("hardened_subsystems").getDisplayName();
    private final String DESC4=txt("plagueLPCrequirement");
    
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return DESC0;
        if (index == 1) return DESC1;
        if (index == 2) return DESC2;
        if (index == 3) return DESC3;
        if (index == 4) return DESC4;
        return null;
    }
    
    private final String POST0=txt("plagueLPC_0");
    private final String POST1=txt("plagueLPC_1");
    private final String POST2=txt("plagueLPC_2");
    private final String POST3=txt("plagueLPC_3");
    private final String POST4=txt("plagueLPC_4");
    private final String POST5=txt("plagueLPC_5");
    private final String POST6=txt("plagueLPC_6");
    private final String POST7=txt("plagueLPC_7");
    private final String POST8=txt("plagueLPC_8");
    
    private final String POST_reduced1=txt("plagueLPCreduced_1");
    private final String POST_reduced2=txt("plagueLPCreduced_2");
    private final String POST_reduced3=txt("plagueLPCreduced_3");
    
    private final String POST_noeffect=txt("plagueLPCnoEffect");
    
    private final Color HL=Global.getSettings().getColor("hColor");
    private final Color BAD=Misc.getNegativeHighlightColor();
    
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        //title
        tooltip.addSectionHeading(POST0, Alignment.MID, 15);        
        
        if(ship!=null && ship.getVariant()!=null){
            if( ship.getVariant().getNonBuiltInWings().isEmpty()){
                //no wing fitted
                tooltip.addPara(
                        POST1
                        ,10
                        ,HL
                );
            } else if(!allPlagueWing(ship.getVariant())){
                //non plague wings installed
                tooltip.addPara(
                        POST2
                        ,10
                        ,HL
                );
            } else {
                //effect applied

                //plague bearers have no negative effect but still require the hullmod
                if(ship.getVariant().getHullMods().contains("SKR_plagueBearer")){
                    tooltip.addPara(
                            POST_noeffect
                            ,10
                            ,BAD
                    );
                } else {

                    float total = CR_LOSS*ship.getVariant().getNonBuiltInWings().size();

                    boolean reduced=false;
                    float mult=1;
                    if(ship.getVariant().getHullMods().contains("hardened_subsystems")){
                        reduced=true;
                        mult=SKR_plagueEffect.HSS;
                    }


                    String crLoss = String.valueOf((int)total);
                    String wings = String.valueOf((int)ship.getVariant().getNonBuiltInWings().size());

                    tooltip.addPara(
                            POST3
                            + crLoss
                            + POST4
                            + wings
                            + POST5
                            ,10
                            ,BAD
                            ,crLoss
                            ,wings
                    );

                    //Hadened subsystems installed
                    if(reduced){
                        LabelAPI hullmod = tooltip.addPara(
                                POST_reduced1
                                + Math.round(total*mult)
                                + POST_reduced2
                                + DESC3
                                + POST_reduced3,
                                10
                        );
                        hullmod.setHighlightColors(BAD,HL);
                        hullmod.setHighlight(""+Math.round(total*mult),DESC3);
                    }

                    //list plague wings
                    tooltip.addPara(
                            POST6
                            ,10
                            ,HL
                    );

                    tooltip.setBulletedListMode("    - ");  

                    for(String w : ship.getVariant().getNonBuiltInWings()){
                        String wingName = Global.getSettings().getFighterWingSpec(w).getWingName();

                        tooltip.addPara(
                                wingName
                                + POST7
                                + Math.round(-CR_LOSS*mult)
                                + POST8
                                ,3
                                ,HL
                                ,""+Math.round(-CR_LOSS*mult)
                        );
                    }
                    tooltip.setBulletedListMode(null);
                }
            }
        }
        
    }
    
    private final String EFFECT=txt("plagueLPCEffect");
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        if(allPlagueWing(stats.getVariant())){
            
            //faster repairs
            stats.getFighterRefitTimeMult().modifyMult(id, REFIT_BONUS, "Plague Contamination protocols");
            
            //CR loss
            float debuff = CR_LOSS*stats.getVariant().getNonBuiltInWings().size();
            if(stats.getVariant().getHullMods().contains("hardened_subsystems")){
                debuff*=SKR_plagueEffect.HSS;
            }
            float maxCR = 0.7f*stats.getMaxCombatReadiness().computeMultMod();
            stats.getMaxCombatReadiness().modifyFlat(id, Math.max(-debuff*0.01f,-maxCR), EFFECT);
        }
    }
    
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if(ship==null) return false;
        if(ship.getVariant().getHullMods().contains("SKR_plagueBearer")) return false;
        int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedInt();
        return bays > 0; 
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return txt("plagueLPCnodeck");
    }

    private boolean allPlagueWing(ShipVariantAPI v){
        boolean allPlagueWing=true;
        for(String w : v.getWings()){
            if (!w.equals("") && (!w.startsWith("SKR")||!SKR_plagueEffect.LPC.contains(w))){
                allPlagueWing=false;
            }
        }
        return allPlagueWing;
    }
}

