//By Tartiflette.
package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_flareAI implements MissileAIPlugin, GuidedMissileAI {
    
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    
    //delay before engine ignition
    private float FUSE;
    
    private final Map<Integer, Color> COLORS=new HashMap<>();
    {
        COLORS.put(0, new Color(255,255,240));
        COLORS.put(1, new Color(255,30,30));
        COLORS.put(2, new Color(220,180,30));
        COLORS.put(3, new Color(30,255,30));
        COLORS.put(4, new Color(30,230,170));
        COLORS.put(5, new Color(30,30,255));
        COLORS.put(6, new Color(200,30,230));
        COLORS.put(7, new Color(240,240,255));
    }
    private final Map<Integer, String> SOUNDS=new HashMap<>();
    {
        SOUNDS.put(4, "CIV_fireworks_01");
        SOUNDS.put(3, "CIV_fireworks_02");
        SOUNDS.put(2, "CIV_fireworks_03");
        SOUNDS.put(1, "CIV_fireworks_04");
        SOUNDS.put(0, "CIV_fireworks_05");
    }

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public SKR_flareAI(MissileAPI missile, ShipAPI launchingShip) {
        this.engine = Global.getCombatEngine();
        this.missile = missile;
        this.target = null;
        this.FUSE = 1.5f+0.25f*(float)Math.random();
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused()) {return;}
        
        if(missile.getElapsed()<FUSE){
            missile.giveCommand(ShipCommand.ACCELERATE);
        } else {
            
            float size=50+(float)Math.random()*350;
                                   
            int chooser = Math.round(1+2.5f*(1+(float)FastTrig.sin(engine.getTotalElapsedTime(false)/5)));
            chooser+=(new Random().nextInt(2))-1;
            
            Color color=COLORS.get((int)chooser);
            
            engine.spawnExplosion(missile.getLocation(), new Vector2f(), color, size/3, 0.25f);
            
            engine.spawnProjectile(missile.getSource(), missile.getWeapon(), "flarelauncher1", missile.getLocation(), missile.getFacing(), MathUtils.getPoint(new Vector2f(), 350, missile.getFacing()+180));
            
            for(int i=0; i<size/7; i++){
                engine.addHitParticle(missile.getLocation(), MathUtils.getRandomPointInCircle(new Vector2f(), size/3), 3+3*(float)Math.random(), 1, 1+2f*(float)Math.random(), color);
            }
            
            engine.addHitParticle(missile.getLocation(), new Vector2f(), size*0.8f, 1, size/400, color);
            engine.addHitParticle(missile.getLocation(), new Vector2f(), size*0.8f, 1, 0.1f, new Color(200,230,255));
            
            size=((size-51)/350)*4;            
            Global.getSoundPlayer().playSound(SOUNDS.get(Math.max(0,(int)size)), 1, 1, missile.getLocation(), new Vector2f());
            
            engine.removeEntity(missile);
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
    
    public void init(CombatEngineAPI engine) {}
}
