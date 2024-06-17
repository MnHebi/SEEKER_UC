package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicRender;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SKR_canAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private boolean windup=false;
    private boolean runOnce=false;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private final IntervalUtil blink = new IntervalUtil(0.5f,0.5f);

    public SKR_canAI(MissileAPI missile, ShipAPI launchingShip) {	        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        this.missile = missile;
    }

    @Override
    public void advance(float amount) {        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (engine.isPaused() || missile.isFading()) {return;}
        
//        if(missile.getVelocity().lengthSquared()>1600){
//            missile.giveCommand(ShipCommand.DECELERATE);
//        }
        
        if(!runOnce){
            runOnce=true;
            missile.setCollisionClass(CollisionClass.SHIP);
            missile.setMass(5000);
        }
        
        if(missile.isArmed()){
            /*
            public DamagingExplosionSpec(
                float duration,
                float radius,
                float coreRadius,
                float maxDamage, 
                float minDamage, 
                CollisionClass collisionClass,
                CollisionClass collisionClassByFighter,
                float particleSizeMin,
                float particleSizeRange,
                float particleDuration,
                int particleCount,
                Color particleColor,
                Color explosionColor
            )
            */
            DamagingExplosionSpec boom = new DamagingExplosionSpec(
                    0.5f,
                    1500,
                    500,
                    1000,
                    250,
                    CollisionClass.MISSILE_FF,
                    CollisionClass.MISSILE_FF,
                    2,
                    5,
                    0.5f,
                    25,
                    new Color(225,100,0,64),
                    new Color(200,100,25,64)
            );
            boom.setDamageType(DamageType.HIGH_EXPLOSIVE);
            boom.setShowGraphic(true);
            boom.setSoundSetId("SKR_canister_explode");
            engine.spawnDamagingExplosion(boom, missile.getSource(), missile.getLocation(),false);
            
            float angle=(float)Math.random()*360;
            //visual effect
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_wave"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(512,512),
                    new Vector2f(2048,2048),
                    (float)Math.random()*360,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    new Color(128,255,128,255),
                    true,
                    0,
                    0,
                    2
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_wave"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(720,720),
                    new Vector2f(1600,1600),
                    (float)Math.random()*360,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    new Color(128,128,255,255),
                    true,
                    0,
                    0,
                    1.5f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_smoke"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(960,960),
                    new Vector2f(150,150),
                    angle+(float)Math.random()*5,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    new Color(255,75,75,128),
                    true,
                    0f,
                    0.5f,
                    1.5f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_smoke"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(1024,1024),
                    new Vector2f(128,128),
                    angle+(float)Math.random()*10,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    new Color(128,128,128,128),
                    false,
                    1f,
                    0.5f,
                    3f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_smoke"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(1024,1024),
                    new Vector2f(128,128),
                    angle+(float)Math.random()*5,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    new Color (64,64,64,128),
                    false,
                    1f,
                    1f,
                    5f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_glow"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(1024,1024),
                    new Vector2f(128,128),
                    angle+(float)Math.random()*10,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    Color.WHITE,
                    true,
                    0f,
                    0f,
                    0.5f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "skr_can_burn"),
                    new Vector2f(missile.getLocation()),
                    new Vector2f(),
                    new Vector2f(1024,1024),
                    new Vector2f(128,128),
                    angle+(float)Math.random()*10,
                    MathUtils.getRandomNumberInRange(-5, 5),
                    new Color(255,128,64,255),
                    true,
                    0f,
                    0.25f,
                    1f
            );
            engine.addSmoothParticle(missile.getLocation(), new Vector2f(), 3000, 2, 0.1f, Color.white);
            engine.addHitParticle(missile.getLocation(), new Vector2f(), 2000, 1, 0.4f, new Color(200,100,25));
            engine.spawnExplosion(missile.getLocation(), new Vector2f(), Color.DARK_GRAY, 2000, 1f);
            
            if(Global.getSettings().getModManager().isModEnabled("shaderLib")){
                SKR_graphicLibEffects.CustomRippleDistortion(missile.getLocation(), new Vector2f(), 2048, 30, false, 0, 360, 0, 0.5f, 0, 0.25f, 0.25f, 0);
                SKR_graphicLibEffects.customLight(missile.getLocation(), new SimpleEntity(missile.getLocation()), 1500, 1, Color.WHITE, 0, 0.5f, 3);
            }
            
            for(int i=0; i<180; i++){
                  engine.spawnProjectile(missile.getSource(), missile.getWeapon(), "SKR_canister_sub", missile.getLocation(), 2*i, new Vector2f());
//                  engine.spawnProjectile(missile.getSource(), missile.getWeapon(), "hveldriver", missile.getLocation(), 2*i, new Vector2f());
            }
            
            engine.removeEntity(missile);
        } else {
            blink.advance(amount);
            if(blink.intervalElapsed()){
                float interval=Math.min(0.5f, Math.max(0.1f, (missile.getArmingTime()-missile.getFlightTime())/6));
                blink.setInterval(interval,interval);
                if(blink.getMinInterval()<0.5){
                    engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 300-200*blink.getMaxInterval(), 0.4f, 0.1f, Color.red);
                } else {
                    engine.addHitParticle(missile.getLocation(), missile.getVelocity(), 150, 0.4f, 0.1f, Color.ORANGE);
                }
            }
            if(!windup && missile.getArmingTime()-missile.getFlightTime()<3){
                windup=true;
                Global.getSoundPlayer().playSound("SKR_canister_windup", 1, 0.5f, missile.getLocation(), missile.getVelocity());
            }
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
}