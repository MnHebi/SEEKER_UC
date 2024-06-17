package data.campaign.bosses;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI.AbilitySlotAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.SKR_ids;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;

/**
 * @author Tartiflette
 */
public class SKR_sustainedBurn_item extends BaseSpecialItemPlugin{
	
	@Override
	public int getPrice(MarketAPI market, SubmarketAPI submarket) {
		return super.getPrice(market, submarket);
	}
	
	@Override
	public String getDesignType() {
		return null;
	}
	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
		//super.createTooltip(tooltip, expanded, transferHandler, stackSource);
		
		float pad = 3f;
		float opad = 10f;
		float small = 5f;
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		Color b = Misc.getPositiveHighlightColor();

		tooltip.addTitle(getName());
		
		String design = getDesignType();
		if (design != null) {
			Misc.addDesignTypePara(tooltip, design, 10f);
		}
		
		if (!spec.getDesc().isEmpty()) {
			tooltip.addPara(spec.getDesc(), Misc.getTextColor(), opad);
		}
		
		addCostLabel(tooltip, opad, transferHandler, stackSource);
		
		tooltip.addPara(txt("itemTT1") + getName() + txt("itemTT2"), b, opad);
	}

	@Override
	public float getTooltipWidth() {
		return super.getTooltipWidth();
	}
	
	@Override
	public boolean isTooltipExpandable() {
		return false;
	}
	
	@Override
	public boolean hasRightClickAction() {
		return true;
	}

	@Override
	public boolean shouldRemoveOnRightClickAction() {
		return true;
	}

	@Override
	public void performRightClickAction() {
                final CharacterDataAPI player = Global.getSector().getCharacterData();
                if (player.getAbilities().contains(SKR_ids.ABILITY_SUSTAINED_BURN)) return;

                // Unlock the ability and replace the corresponding vanilla ability
                player.addAbility(SKR_ids.ABILITY_SUSTAINED_BURN);
                
                boolean slotted = false;
                for (AbilitySlotAPI slot : Global.getSector().getUIData().getAbilitySlotsAPI().getCurrSlotsCopy()){
                    if (slot.getAbilityId() != null && slot.getAbilityId().equals("sustained_burn")){
                        slot.setAbilityId(SKR_ids.ABILITY_SUSTAINED_BURN);
                        slotted=true;
                        break;
                    }
                }
                
                //place in empty slot otherwise
                if(!slotted){
                    for (AbilitySlotAPI slot : Global.getSector().getUIData().getAbilitySlotsAPI().getCurrSlotsCopy()){
                        if (slot.getAbilityId() == null){
                            slot.setAbilityId(SKR_ids.ABILITY_SUSTAINED_BURN);
                            break;
                        }
                    }
                }
                
                //notify player of the effect
		Global.getSoundPlayer().playUISound(getSpec().getSoundId(), 1f, 1f);
		Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
				getName() + txt("ESB_item"));
	}
}
