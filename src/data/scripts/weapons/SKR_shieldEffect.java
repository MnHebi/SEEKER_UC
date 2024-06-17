package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicUI;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;

//By Tartiflette

public class SKR_shieldEffect implements EveryFrameWeaponEffectPlugin {
    
    private final float MAX_SHIELD=10;
    
    private boolean runOnce = false, disabled=false;
    private float shieldT=0,aiT=0;
    private ShipAPI ship;
    private ShieldAPI shield;       
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        
        if (!runOnce){
            runOnce=true;
            ship=weapon.getShip();
            shield=ship.getShield();
        }        
        
        if (engine.isPaused() || !ship.isAlive()) {return;}
        
        if(shield!=null){
            if(!disabled){
                if(shield.isOn()){
                    shieldT=Math.min(MAX_SHIELD, shieldT+amount);
                } else {
                    shieldT=Math.max(0, shieldT-(amount*2));
                }
                
                //UI
                float color=shieldT/MAX_SHIELD;
//                MagicUI.drawSystemBar(ship,new Color(color,1,(1-color)/2), shieldT/MAX_SHIELD, 0);
                MagicUI.drawInterfaceStatusBar(ship, 1-shieldT/MAX_SHIELD, new Color(color*0.4f+0.6f,1f-0.4f*color,0), null, 0, txt("siegShield"), (int) (100-100*shieldT/MAX_SHIELD));
                       
                //AI
                if(ship.getAIFlags()!=null){
                    aiT+=amount;
                    if(aiT>0.5f){
                        aiT=0;

                        if(
                                !ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS) &&
                                Math.random()>0.75f &&
                                (shieldT/MAX_SHIELD)>0.8f                               
                                ){
                            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS,2);
                        }
                    }
                }
                
                if(shieldT==MAX_SHIELD){
                    shield.toggleOff();
                    ship.getFluxTracker().showOverloadFloatyIfNeeded(txt("siegShieldOff"), Color.red, 0, true);
                    ship.getFluxTracker().beginOverloadWithTotalBaseDuration(0.5f);
                    disabled=true;
                    //prevent AI use
                    if(ship.getAIFlags()!=null){
                        ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS,MAX_SHIELD*2.1f);
                    }
                }
                
            } else {
                shield.toggleOff();
                shieldT=Math.max(0, shieldT-(amount/2));
                
//                MagicUI.drawSystemBar(ship,new Color(255,0,0), shieldT/MAX_SHIELD,0);
                MagicUI.drawInterfaceStatusBar(ship, 1-shieldT/MAX_SHIELD, Color.RED, null, 0, "SHIELD", (int) (100-100*shieldT/MAX_SHIELD));
                
                if(shieldT==0){
                    disabled=false;
                    ship.getFluxTracker().showOverloadFloatyIfNeeded(txt("siegShieldOn"), Color.green, 0, true);
                }
            }
        }
    }
}