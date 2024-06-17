package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.magiclib.util.MagicAnim;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;
import data.scripts.util.SKR_graphicLibEffects;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_balisongEffectDeprecated implements EveryFrameWeaponEffectPlugin {
    
    private float overcharge=0;
    
    //max free overcharge ammo:
    private final int AMMO=20;
    private final String FIRE_SOUND="SKR_balisongBushwhacker_0", ID="SKR_overcharge", EXTRA_AMMO="magazines", leftSlot = "LEFT", rightSlot = "RIGHT", leftModule = "MODULE_LEFT", rightModule = "MODULE_RIGHT", systemCharge = "SYSTEM";
    
    private final float FOLDED_ANGLE=-15;
    private final Vector2f FOLDED_POS=new Vector2f(25,16);
    
    private final Map<Integer, String> PROJ=new HashMap<>();
    {
        PROJ.put(1, "SKR_balisongBushwhackerWeakest");        
        PROJ.put(2, "SKR_balisongBushwhackerWeak");      
        PROJ.put(3, "SKR_balisongBushwhackerMedium");      
        PROJ.put(4, "SKR_balisongBushwhackerStrong");
        PROJ.put(5, "SKR_balisongBushwhacker");
        PROJ.put(6, "SKR_balisongBushwhackerWeak");
    }
        
    //modules overlap
    private Map<WeaponAPI, Vector2f> MODULE_POS=new HashMap<>();
//    private Map<WeaponAPI, Vector2f> MODULE_VEL=new HashMap<>();
    private Map<WeaponAPI, Vector2f> MODULE_OFST=new HashMap<>();
    
    private boolean runOnce=false, activated=false, hasFired=false, fullCharge=false, inbound=false, SHADER=false;
    private ShipAPI ship, moduleLeft, moduleRight;
    private ShipSystemAPI system;
    private WeaponAPI weaponLeft, weaponRight, decoCharge;
    private WeaponSlotAPI slotLeft, slotRight;
    private AnimationAPI charge;
    private float delay=0, lastCharge=0, fold=1, timer=0;
    private IntervalUtil animation = new IntervalUtil(0.05f,0.05f);
    private int chargeLevel=0, extraAmmo=0;
    private final String zapSprite="zap_0";
    private final int zapFrames=8;    
    private String personality;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine.isPaused()) return;
        
        //SETUP        
        if(!runOnce){
            //delay for module detection (to remove after 0.9)
//            if(weapon.getShip().getOriginalOwner()==1 && weapon.getShip().getFullTimeDeployed()<2.1f) return;
            
            //distortion + light effects
            SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
            
            runOnce=true;            
            ship=weapon.getShip();
            //system shenanigans
            personality = ship.getCaptain().getPersonalityAPI().getId();
            system=ship.getSystem();
            
            //module shenanigans
            ship.ensureClonedStationSlotSpec();
            
            for(WeaponAPI w : ship.getAllWeapons()){
                switch (w.getSlot().getId()){
                    case leftSlot:
                        weaponLeft=w;
                        MODULE_POS.put(w,w.getLocation());
//                        MODULE_VEL.put(w,new Vector2f());
                        MODULE_OFST.put(w,new Vector2f());
                        break;
                    case rightSlot:
                        weaponRight=w;
                        MODULE_POS.put(w,w.getLocation());
//                        MODULE_VEL.put(w,new Vector2f());
                        MODULE_OFST.put(w,new Vector2f());
                        break;
                    case systemCharge:
                        decoCharge=w;
                        charge=w.getAnimation();
                    default:
                        break;
                }
            }
            for(ShipAPI s : ship.getChildModulesCopy()){
                switch(s.getStationSlot().getId()){
                    case leftModule:
                        moduleLeft=s;
                        slotLeft=s.getStationSlot();
                        break;
                    case rightModule:
                        moduleRight=s;
                        slotRight=s.getStationSlot();
                        break;
                    default:
                        break;
                }
            }
            
            activated=false;
            overcharge=0;
            
            if(ship.getVariant().getHullMods().contains(EXTRA_AMMO)){
                extraAmmo=AMMO;
            }
            
            if(moduleLeft!=null){
                moduleAnimation(ship, moduleLeft, slotLeft, weaponLeft, -1, fold, amount);
            } else {
                weaponLeft.disable(true);
            }
            
            if(moduleRight!=null){
                moduleAnimation(ship, moduleRight, slotRight, weaponRight, 1, fold, amount);
            } else {
                weaponRight.disable(true);
            }
            
            fold=0;
        }
        
        
        ///////////////////////////////////
        //                               //
        //   WEAPON DESTRUCTION CHECK    //
        //                               //
        ///////////////////////////////////        
        
        if(moduleLeft!=null && !moduleLeft.isAlive()){
            weaponLeft.setAmmo(0);
            weaponLeft.disable(true);
            moduleLeft=null;
        }
        
        if(moduleRight!=null && !moduleRight.isAlive()){
            weaponRight.setAmmo(0);
            weaponRight.disable(true);
            moduleRight=null;
        }
        
        ///////////////////////////////////
        //                               //
        //      TRAVEL DRIVE CHECK       //
        //                               //
        /////////////////////////////////// 
        
        if(ship.getTravelDrive().isActive()){
            
            inbound=true;
            
            if(moduleLeft!=null){
                moduleAnimation(ship, moduleLeft, slotLeft, weaponLeft, -1, fold, amount);
            }        
            if(moduleRight!=null){
                moduleAnimation(ship, moduleRight, slotRight, weaponRight, 1, fold, amount);
            }
            return;
        } else if(inbound){
            inbound=false;
            Global.getSoundPlayer().playSound("SKR_balisong_close", 1, 1, ship.getLocation(), ship.getVelocity());
        }
                
        ///////////////////////////////////
        //                               //
        //     WEAPON EARLY RELEASE      //
        //                               //
        ///////////////////////////////////
        
        if(!hasFired && (weapon.getChargeLevel()>0 || lastCharge>0)){     
            if(weapon.getChargeLevel()==1){ 
            
                //NORMAL FIRE            
                hasFired=true;
//                if(activated){
//                    
//                    //SUPER OVERDRIVE SHOT (MAYBE)
//                    
//                    engine.spawnProjectile(
//                            ship,
//                            weapon,
//                            PROJ.get(PROJ.size()),
//                            weapon.getLocation(),
//                            weapon.getCurrAngle(),
//                            ship.getVelocity()
//                    );
//                }
            } else if(weapon.getChargeLevel()>lastCharge){    
                
                //VISUAL CHARGE EFFECT
                
                if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
                    //sucked particles
                    Vector2f loc = MathUtils.getRandomPointOnCircumference(null, 50+weapon.getChargeLevel()*100);
                    Vector2f vel = new Vector2f(loc);

                    Vector2f.add(vel, ship.getVelocity(), vel);
                    loc.scale(0.5f);
                    Vector2f.sub(new Vector2f(weapon.getLocation()), loc, loc);

                    engine.addHitParticle(
                            loc,
                            vel,
                            2+((float)Math.random()+weapon.getChargeLevel()),
                            1,
                            0.25f+(float)Math.random()/8,
                            new Color(0+(0.5f*weapon.getChargeLevel()), 0.25f+(0.5f*weapon.getChargeLevel()), 1)
                    );

                    //unstable glow
                    if(Math.random()<0.33f){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(weapon.getLocation(), weapon.getChargeLevel()*20),
                                ship.getVelocity(),
                                30+((float)Math.random()+weapon.getChargeLevel()+chargeLevel)*10,
                                1,
                                0.1f+(float)Math.random()/5,
                                new Color(0+(0.5f*weapon.getChargeLevel()), 0.25f+(0.5f*weapon.getChargeLevel()), 1)
                        );
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(weapon.getLocation(), weapon.getChargeLevel()*10),
                                ship.getVelocity(),
                                10+((float)Math.random()+weapon.getChargeLevel()+chargeLevel)*5,
                                0.5f,
                                0.1f+(float)Math.random()/10,
                                Color.white
                        );
                    }
                }
                       
                //SOUND EFFECT                
                Global.getSoundPlayer().playLoop("SKR_balisongBushwhacker_charge", ship, 0.5f+lastCharge, 0.5f+(lastCharge/2), weapon.getLocation(), ship.getVelocity());
                
                //FLASH ON CHARGE LEVEL THRESHOLDS                
                int overdrive=0;
                if(activated){
                    overdrive=1;
                }
                
                int currentLevel=(int)(weapon.getChargeLevel()*(PROJ.size()-1))+overdrive;
                
                if(chargeLevel<currentLevel){     
                    
                    //UPDATE CHARGE LEVEL                
                    chargeLevel=(int)(weapon.getChargeLevel()*(PROJ.size()-1))+overdrive;  
                    
                    Global.getSoundPlayer().playSound("SKR_balisongBushwhacker_threshold", 0.2f+((float)currentLevel/10), 0.05f+((float)currentLevel/30), weapon.getLocation(), ship.getVelocity());
                    
                    engine.addHitParticle(
                            weapon.getLocation(),
                            ship.getVelocity(),
                            (chargeLevel+1)*100,
                            1,
                            0.5f,
                            new Color(0+(0.5f*weapon.getChargeLevel()), 0.25f+(0.5f*weapon.getChargeLevel()), 1)
                    );                    
                    engine.addHitParticle(
                            weapon.getLocation(),
                            ship.getVelocity(),
                            (chargeLevel+1)*33,
                            0.5f,
                            0.2f,
                            Color.white
                    );
                }     
            } else if (chargeLevel>0){
                
                //EARLY FIRE RELEASE                
                hasFired=true;
                ship.getFluxTracker().increaseFlux(chargeLevel*500, false);   
                engine.spawnProjectile(
                        ship,
                        weapon,
                        PROJ.get(chargeLevel),
                        weapon.getLocation(),
                        weapon.getCurrAngle(),
                        ship.getVelocity()
                );
                
                Global.getSoundPlayer().playSound(FIRE_SOUND+chargeLevel, 1, 1, weapon.getLocation(), ship.getVelocity());
                
                for(int i=0; i<(5+chargeLevel*5); i++){
                    Vector2f vel = MathUtils.getRandomPointInCone(new Vector2f(), 200, weapon.getCurrAngle()-22.5f, weapon.getCurrAngle()+22.5f);
                    Vector2f pos = new Vector2f(vel);
                    pos.scale(0.25f);
                    Vector2f.add(pos, weapon.getLocation(), pos);
                    Vector2f.add(vel, ship.getVelocity(), vel);
                    engine.addHitParticle(pos, vel, 15+(35*(float)Math.random()), 1, 0.2f+(0.05f*(float)Math.random()), new Color(100,150,250,255));
                }
                
                weapon.setRemainingCooldownTo(3);
            }
            
            //UPDATE PREVIOUS CHARGE            
            lastCharge=weapon.getChargeLevel();   
            
        } else if(weapon.getChargeLevel()==0){
            
            //RESET            
            hasFired=false;
            chargeLevel=0;
        }
                
        ///////////////////////////////////
        //                               //
        //          OVERCHARGE           //
        //                               //
        ///////////////////////////////////
        
        if(system.isActive()){
            overcharge= Math.min(overcharge+amount, 10);
            
            
            //sound effects
            if(overcharge==10){
                if(!fullCharge){
                    fullCharge=true;                    
                    Global.getSoundPlayer().playSound("SKR_balisongSystem_maxCharge", 1, 0.7f, ship.getLocation(), ship.getVelocity());
                }
                Global.getSoundPlayer().playLoop("SKR_balisongSystem_charged", ship, 1, 1, ship.getLocation(), ship.getVelocity());
            } else{
                Global.getSoundPlayer().playLoop("SKR_balisongSystem_charging", ship, 0.75f+overcharge/40, Math.min(1,overcharge), ship.getLocation(), ship.getVelocity());
            }
            
            Color ui = new Color(255,128,0);
            if(overcharge==10){
                ui=new Color(255,0,0);
            }
//            MagicUI.drawSystemBar(ship,ui, overcharge/10,0);
            MagicUI.drawInterfaceStatusBar(ship, overcharge/10, ui, null, 0, "CHRG", (int) (overcharge*10));
            
            //charge visual effect            
            timer+=amount;
            animation.advance(amount);

            if(timer>(15-overcharge)/30){
                timer=0;
                if(Math.random()<0.75f){
                    zap(engine, MathUtils.getRandomPointInCircle(new Vector2f(), 50+5*overcharge), false);
                }
            }
            
            //animation
            if(animation.intervalElapsed()){
                int frame = charge.getFrame();
                frame++;
                if(frame>=charge.getNumFrames()-1){
                    frame=1;
                }
                charge.setFrame(frame);
                decoCharge.getSprite().setColor(new Color(1,1,1,MagicAnim.smoothNormalizeRange(overcharge/10,0,0.5f)));
                
                if(overcharge==10){
                    if(Math.random()<0.75f){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(ship.getLocation(),7),
                                ship.getVelocity(),
                                25+50*(float)Math.random(),
                                1,
                                0.1f+0.2f*(float)Math.random(),
                                new Color(200,50,100,128)
                        );      
                    } else {
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(ship.getLocation(),7),
                                ship.getVelocity(),
                                25+50*(float)Math.random(),
                                1,
                                0.05f+0.1f*(float)Math.random(),
                                Color.WHITE
                        ); 
                    }
                }
            }
            
            //closing sound
            if(fold==0){
                Global.getSoundPlayer().playSound("SKR_balisong_close", 1, 1, ship.getLocation(), ship.getVelocity());
            }
            
            //weapons forced folding
            fold=Math.min(1, fold+amount);
                    
            //Charging stats
            applyCharge(ship,Math.max(0, Math.min(1,overcharge-9)),ID);
            
        } else if (overcharge>0){
            overcharge= Math.max(0, overcharge-amount);
//            MagicUI.drawSystemBar(ship,new Color(0,128,255), overcharge/10,0);
            MagicUI.drawInterfaceStatusBar(ship, overcharge/10, Color.CYAN, null, 0, "CHRG", (int) (overcharge*10));
            
            //loop sound
            Global.getSoundPlayer().playLoop("SKR_balisongSystem_active", ship, 1f+overcharge/40, Math.min(1,overcharge/2), ship.getLocation(), ship.getVelocity());
            
            //release module weapons + free ammo
            if(!activated){
                activated=true;
                
                ship.getCaptain().setPersonality("reckless");
            
                //activation sound
                Global.getSoundPlayer().playSound("SKR_balisongSystem_activation", 1, 1, ship.getLocation(), ship.getVelocity());
                
                //Flux dump
                float level = ship.getFluxTracker().getFluxLevel();
                level = (level - (overcharge/20)*(level*level));
                ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux() * level);
                ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux() * level);
                //Maximum flux reduction is X-0.5*X^2, that translate in a larger dump if the flux is high
                
                if(moduleLeft!=null){
                    weaponLeft.setAmmo(Math.min(weaponLeft.getMaxAmmo()*2,weaponLeft.getAmmo()+(int)(overcharge/10 * (AMMO+extraAmmo))));
                }
                if(moduleRight!=null){
                    weaponRight.setAmmo(Math.min(weaponRight.getMaxAmmo()*2,weaponRight.getAmmo()+(int)(overcharge/10 * (AMMO+extraAmmo))));
                }
                
                //flash        
                engine.addHitParticle(
                        ship.getLocation(),
                        new Vector2f(),
                        50*overcharge,
                        1,
                        0.1f+overcharge/15,
                        new Color(200,50,100,128)
                );                
                engine.addHitParticle(
                        ship.getLocation(),
                        new Vector2f(),
                        40*overcharge,
                        1,
                        0.05f+overcharge/30,
                        Color.WHITE
                );  
                
                //animation                
                charge.setFrame(11);
                   
                if(MagicRender.screenCheck(0.25f, ship.getLocation())){
                    for(int i=0; i<3*overcharge; i++){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(ship.getLocation(), 30*overcharge),
                                MathUtils.getRandomPointInCircle(new Vector2f(), 600),
                                3+(float)Math.random()*3,
                                1,
                                0.5f+(float)Math.random()*0.5f,
                                new Color(0.5f+0.25f*(float)Math.random(),0.1f+0.1f*(float)Math.random(),0.25f+0.25f*(float)Math.random())
                        ); 
                    } 
                    if(SHADER){                        
                        SKR_graphicLibEffects.balisongRing(ship,overcharge);
                    }
                }
            }
            
            //overcharge visual   
            
            decoCharge.getSprite().setColor(new Color(1,1,1,MagicAnim.smoothNormalizeRange(overcharge/10,0,0.25f)));
            
            
            if(MagicRender.screenCheck(0.25f, weapon.getLocation())){
                timer+=amount;
                if(timer>(15-overcharge)/30){
                    timer=0;
                        zap(engine, MathUtils.getRandomPointInCircle(new Vector2f(), 50+8*overcharge), true); //zaps
                }
            }
            
            ship.setJitterUnder(ship, new Color(200,50,100,128), 0.25f+overcharge/10, (int)overcharge, 10+3*overcharge); //ship jitter
            if(moduleLeft!=null){
                moduleLeft.setJitterUnder(ship, new Color(200,50,100,128), 0.1f+overcharge/10, (int)overcharge, 5+3*overcharge); //weapon jitter
            }
            if(moduleRight!=null){
                moduleRight.setJitterUnder(ship, new Color(200,50,100,128), 0.1f+overcharge/10, (int)overcharge, 5+3*overcharge); //weapon jitter
            }
            
            if(fold==1){
                Global.getSoundPlayer().playSound("SKR_balisong_open", 1, 1, ship.getLocation(), ship.getVelocity());
            }
            
            //weapons unfolding and overlap
            fold=Math.max(0, fold-amount);
            
            //Overcharged stats
            applyOvercharge(ship,Math.max(0, Math.min(1,overcharge)),ID);
            
        } else {
//            MagicUI.drawSystemBar(ship,new Color(0,255,128), 0,0);
            MagicUI.drawInterfaceStatusBar(ship, 0, Color.CYAN, null, 0, "CHRG", 0);
            if(activated){
                fullCharge=false;
                activated = false;
                //Cancel mutable stats
                unapplyCharge(ship,ID);
                system.setAmmo(1);
                charge.setFrame(0);
                ship.getCaptain().setPersonality(personality);
            }
            
            //weapons folding if empty
            if(weaponLeft.getAmmo()==0 && weaponRight.getAmmo()==0){
                delay+=amount;
                if(delay>1){
                    if(fold==0){
                        Global.getSoundPlayer().playSound("SKR_balisong_close", 1, 1, ship.getLocation(), ship.getVelocity());
                    }
                    fold=Math.min(1, fold+amount);
                } else {                    
                    fold=Math.max(0, fold-amount);
                }
            } else {
                delay=0;
                fold=Math.max(0, fold-amount);
            }            
        }
        
        ///////////////////////////////////
        //                               //
        //        MODULES FACING         //
        //                               //
        ///////////////////////////////////
        
        if(moduleLeft!=null){
            moduleAnimation(ship, moduleLeft, slotLeft, weaponLeft, 1, fold, amount);
            //lock module weapon
            if(fold>0){
                weaponLeft.setRemainingCooldownTo(1f);
            }
        }        
        if(moduleRight!=null){
            moduleAnimation(ship, moduleRight, slotRight, weaponRight, -1, fold, amount);
            //lock module weapon
            if(fold>0){
                weaponRight.setRemainingCooldownTo(1f);
            }
        }
    }
    
    ///////////////////////////////////
    //                               //
    //       MODULES ANIMATION       //
    //                               //
    ///////////////////////////////////
    
    private void moduleAnimation(ShipAPI ship, ShipAPI module, WeaponSlotAPI slot, WeaponAPI weapon, Integer side, float folding, float amount){
        
//        module.setFacing((ship.getFacing()-side*FOLDED_ANGLE)+(1-MagicAnim.smoothNormalizeRange(folding,0,1))*(MathUtils.getShortestRotation(ship.getFacing(), weapon.getCurrAngle())+ side*FOLDED_ANGLE));
        slot.setAngle(side*FOLDED_ANGLE+(1-MagicAnim.smoothNormalizeRange(folding,0,1))*(MathUtils.getShortestRotation(ship.getFacing(), weapon.getCurrAngle()) - side*FOLDED_ANGLE));
        
//        Vector2f accel = MODULE_VEL.get(weapon);
//        accel.scale(amount);
//        Vector2f.add(MODULE_POS.get(weapon), accel, accel); //hypothetical position without acceleration
//        
//        Vector2f.sub(weapon.getLocation(), accel, accel); //deviation due to acceleration
//        
//        Vector2f offset = MODULE_OFST.get(weapon);
//        offset.scale(Math.max(0, 1-3*amount));
//        accel.scale(-(0.1f/amount));
//        Vector2f.add(offset, accel, offset); //new offset        
//        MODULE_OFST.put(weapon, new Vector2f(offset));
//        VectorUtils.rotate(offset,  -ship.getFacing(), offset); //rotate to compensate the ship facing
//        
//        offset.scale(1-folding); //folding scale
//        
//        //next data
//        Vector2f vel = MODULE_POS.get(weapon);
//        Vector2f.sub(weapon.getLocation(), vel, vel);
//        vel.scale(1/amount);
//        MODULE_VEL.put(weapon, vel);        
//        MODULE_POS.put(weapon, weapon.getLocation());
        
//        //apply folding
//        module.getModuleOffset().set(new Vector2f(
//                            -FOLDED_POS.x*MagicAnim.smoothNormalizeRange(folding,0,0.75f)-offset.x,
//                            -FOLDED_POS.y*(side*MagicAnim.smoothNormalizeRange(folding,0.25f,1))-offset.y
//                        ));
        
//        for(int i=0; i<weapon.getSpec().getTurretFireOffsets().size(); i++){            
//            weapon.getSpec().getHardpointFireOffsets().set(i, new Vector2f(offset.x+85,offset.y));
//        }
        module.getModuleOffset().set(
                new Vector2f(
                        FOLDED_POS.x*MagicAnim.smoothNormalizeRange(folding,0,0.75f),
                        FOLDED_POS.y*(side*MagicAnim.smoothNormalizeRange(folding,0.25f,1))
                )
        );
    }
    
    ///////////////////////////////////
    //                               //
    //      OVERCHARGE SPARKLES      //
    //                               //
    ///////////////////////////////////
    
    private void zap(CombatEngineAPI engine, Vector2f offset, boolean active){     
            
            int chooser = new Random().nextInt(zapFrames - 1) + 1;
            float rand = 0.5f*(float)Math.random()+0.5f;
            Vector2f vel = new Vector2f(offset);
            float boost = 0;
            if(active){
                vel.scale(0.66f);
                boost+=5+(float)Math.random()*5;
            } else {
                vel.scale(0.25f);
            }
            
            MagicRender.objectspace(
                    Global.getSettings().getSprite("fx",zapSprite+chooser),
                    ship,
                    offset,
                    new Vector2f(vel),
                    new Vector2f(48*rand+boost,48*rand+boost),
                    new Vector2f((float)Math.random()*20,(float)Math.random()*20),
                    (float)Math.random()*360,
                    (float)(Math.random()-0.5f)*50,
                    false,
                    new Color(255,100,155),
                    true,
                    0,
                    0.25f+(float)Math.random()*0.1f,
                    0.25f,
                    false
            );
            
            Vector2f loc=new Vector2f(offset);
            Vector2f.add(loc, ship.getLocation(), loc);
            Vector2f.add(vel, ship.getVelocity(), vel);
            
            engine.addHitParticle(
                    loc,
                    vel,
                    50*rand+boost*2,
                    1,                    
                    (float)Math.random()*0.1f,               
                    new Color(255,100,155)
            );        
    }
    
    ///////////////////////////////////
    //                               //
    //         CHARGE STATS          //
    //                               //
    ///////////////////////////////////
    
    private void applyCharge(ShipAPI ship, float charge, String ID){
        ship.getMutableStats().getEnergyRoFMult().modifyMult(ID, 0.75f+(0.25f*charge));        
        ship.getMutableStats().getBallisticRoFMult().modifyMult(ID, 0.75f+(0.25f*charge));   
        ship.getMutableStats().getBeamWeaponDamageMult().modifyMult(ID, 0.75f+(0.25f*charge));
        
        ship.getMutableStats().getAcceleration().modifyMult(ID, 0.5f+(0.5f*charge));
        ship.getMutableStats().getDeceleration().modifyMult(ID, 0.5f+(0.5f*charge));
        ship.getMutableStats().getTurnAcceleration().modifyMult(ID, 0.5f+(0.5f*charge));    
    }
    
    private void applyOvercharge(ShipAPI ship, float charge, String ID){ 

        //using percentage modifier for proper additive boost
        ship.getMutableStats().getEnergyRoFMult().modifyPercent(ID, 100*charge);
        ship.getMutableStats().getBallisticRoFMult().modifyPercent(ID, 100*charge);
        ship.getMutableStats().getBeamWeaponDamageMult().modifyPercent(ID, 100*charge);
        
        ship.getMutableStats().getAcceleration().modifyPercent(ID, 300f);
        ship.getMutableStats().getDeceleration().modifyPercent(ID, 300f);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(ID, 300f);
        
        ship.getMutableStats().getMaxSpeed().modifyPercent(ID, 100*charge);  
        ship.getMutableStats().getMaxTurnRate().modifyPercent(ID, 100*charge);   
    }
    
    private void unapplyCharge(ShipAPI ship, String ID){
        ship.getMutableStats().getEnergyRoFMult().unmodify(ID);
        ship.getMutableStats().getBallisticRoFMult().unmodify(ID);
        ship.getMutableStats().getBeamWeaponDamageMult().unmodify(ID);
        
        ship.getMutableStats().getAcceleration().unmodify(ID);
        ship.getMutableStats().getDeceleration().unmodify(ID);
        ship.getMutableStats().getTurnAcceleration().unmodify(ID);
        
        ship.getMutableStats().getMaxSpeed().unmodify(ID);
        ship.getMutableStats().getMaxTurnRate().unmodify(ID);
    }
    
    ///////////////////////////////////
    //                               //
    //          AUTOPILOT            //
    //                               //
    ///////////////////////////////////  
    
    public float getOvercharge(){
        return overcharge;
    }
}