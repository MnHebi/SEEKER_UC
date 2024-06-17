/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;

public class SKR_graphicLibEffects {    
    
    public static void quicksilverRing(ShipAPI ship){
        RippleDistortion ring = new RippleDistortion(ship.getLocation(),ship.getVelocity());
        ring.setIntensity(25);
        ring.setSize(1750);
        ring.fadeInSize(0.9f);
        //ring.fadeInIntensity(0.1f);
        ring.fadeOutIntensity(0.9f);
        ring.setFrameRate(60);
        DistortionShader.addDistortion(ring);
        
        StandardLight light = new StandardLight(ship.getLocation(), ship.getVelocity(), new Vector2f(), ship);
        light.setColor(new Color(255,160,50,255));
        light.setSize(2000);
        light.setIntensity(0.25f);
        light.setLifetime(2f);
        light.fadeOut(1.5f);
        light.fadeIn(0.25f);
        LightShader.addLight(light);
    }
    
    public static void customLight(Vector2f loc, CombatEntityAPI anchor, float size, float intensity, Color color, float fadeIn, float last, float fadeOut){
        StandardLight light = new StandardLight();
        light.setLocation(loc);
        if(anchor!=null){
            light.attachTo(anchor);
        }
        light.setSize(size);
        light.setIntensity(intensity);
        light.setColor(color);
        light.setLifetime(last);
        light.fadeIn(fadeIn);
        light.fadeOut(fadeOut);
        LightShader.addLight(light);
    }
    
    public static void balisongRing(ShipAPI ship, float intensity){
        RippleDistortion ring = new RippleDistortion(ship.getLocation(),ship.getVelocity());
        ring.setIntensity(5*intensity);
        ring.setSize(100+20*intensity);
        ring.fadeInSize(1);
        ring.fadeOutIntensity(1);
        ring.setFrameRate(60);
        DistortionShader.addDistortion(ring);   
        
        StandardLight light = new StandardLight(ship.getLocation(), ship.getVelocity(), new Vector2f(), ship);
        light.setColor(new Color(255, 200, 150));
        light.setSize(intensity * 100);
        light.setIntensity(intensity/20 + 0.5f);
        light.fadeOut(intensity/2);
        LightShader.addLight(light);
    }
    
    public static void whirlwindWave(WeaponAPI weapon){
        float angle = weapon.getCurrAngle();
        WaveDistortion wave = new WaveDistortion(weapon.getLocation(),new Vector2f());
        wave.setSize(2000);
        wave.setArc(angle-20, angle+20);
        wave.setArcAttenuationWidth(10f);
        wave.setIntensity(500);
        wave.flip(true);
        wave.setLifetime(1f);
        wave.fadeInIntensity(0.5f);
        wave.fadeOutSize(0.5f);
        DistortionShader.addDistortion(wave);
    }
    
    public static void CustomBubbleDistortion (Vector2f loc, Vector2f vel, float size, float intensity, boolean flip, float angle, float arc, float edgeSmooth, float fadeIn, float last, float fadeOut, float growthTime, float shrinkTime){
                
        WaveDistortion wave = new WaveDistortion(loc, vel);

        wave.setIntensity(intensity);
        wave.setSize(size);
        wave.setArc(angle-arc/2,angle+arc/2);
        if(edgeSmooth!=0){
            wave.setArcAttenuationWidth(edgeSmooth);            
        }
        wave.flip(flip);
        if(fadeIn!=0){
            wave.fadeInIntensity(fadeIn);
        }
        wave.setLifetime(last);
        if(fadeOut!=0){
            wave.setAutoFadeIntensityTime(fadeOut);
//            wave.fadeOutIntensity(fadeOut);
        } else {
            wave.setAutoFadeIntensityTime(99);
        }
        if(growthTime!=0){
            wave.fadeInSize(growthTime);
        }
        if(shrinkTime!=0){
            wave.setAutoFadeSizeTime(shrinkTime);
//            wave.fadeOutSize(shrinkTime);
        } else {
            wave.setAutoFadeSizeTime(99);
        }
        DistortionShader.addDistortion(wave);
    }

    public static void CustomRippleDistortion (Vector2f loc, Vector2f vel, float size, float intensity, boolean flip, float angle, float arc, float edgeSmooth, float fadeIn, float last, float fadeOut, float growthTime, float shrinkTime){
                
        RippleDistortion ripple = new RippleDistortion(loc, vel);

        ripple.setIntensity(intensity);
        ripple.setSize(size);
        ripple.setArc(angle-arc/2,angle+arc/2);
        if(edgeSmooth!=0){
            ripple.setArcAttenuationWidth(edgeSmooth);            
        }
        ripple.flip(flip);
        if(fadeIn!=0){
            ripple.fadeInIntensity(fadeIn);
        }
        ripple.setLifetime(last);
        if(fadeOut!=0){
            ripple.setAutoFadeIntensityTime(fadeOut);
        }
        if(growthTime!=0){
            ripple.fadeInSize(growthTime);
        }
        if(shrinkTime!=0){
            ripple.setAutoFadeSizeTime(shrinkTime);
        } 
        ripple.setFrameRate(60);
        DistortionShader.addDistortion(ripple);
        
    }

        public static void CustomStaticRippleDistortion (Vector2f loc, Vector2f vel, float size, float intensity, boolean flip, float angle, float arc, float edgeSmooth, float fadeIn, float last, float fadeOut, float growthTime, float shrinkTime, boolean wide){
                
        RippleDistortion ripple = new RippleDistortion(loc, vel);

        ripple.setFrameRate(0);
        if(wide){
            ripple.setCurrentFrame(10);
        } else {
            ripple.setCurrentFrame(40);
        }
        
        ripple.setIntensity(intensity);
        ripple.setSize(size);
        ripple.setArc(angle-arc/2,angle+arc/2);
        if(edgeSmooth!=0){
            ripple.setArcAttenuationWidth(edgeSmooth);            
        }
        ripple.flip(flip);
        if(fadeIn!=0){
            ripple.fadeInIntensity(fadeIn);
        }
        ripple.setLifetime(last);
        if(fadeOut!=0){
            ripple.setAutoFadeIntensityTime(fadeOut);
        }
        if(growthTime!=0){
            ripple.fadeInSize(growthTime);
        }
        if(shrinkTime!=0){
            ripple.setAutoFadeSizeTime(shrinkTime);
        } 
        DistortionShader.addDistortion(ripple);
        
    }
}