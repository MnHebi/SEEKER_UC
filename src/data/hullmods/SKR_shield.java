package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class SKR_shield extends BaseHullMod {
    
    private final String INNERLARGE = "graphics/SEEKER/fx/SKR_derelictShield_in.png";
    private final String OUTERLARGE = "graphics/SEEKER/fx/SKR_derelictShield_ring.png";
    
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getShield().setRadius(ship.getShieldRadiusEvenIfNoShield(), INNERLARGE, OUTERLARGE);
        ship.getShield().setInnerRotationRate(0.05f);
    }
}
