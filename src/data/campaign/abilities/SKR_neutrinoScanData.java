package data.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OrbitalStationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

/**
 * @author Tartiflette
 */
public class SKR_neutrinoScanData {

    //class defining the shape of the sensor pings
    public static class GSPing {

        public float arc; //wideness of the contact ping
        public float angle; //direction of the contact ping
        public float strength; //intensity of the contact ping
        public FaderUtil fader;
        public boolean withSound = false;

        public GSPing(float angle, float arc, float strength, float in, float out) {
            this.arc = arc;
            this.angle = angle;
            this.strength = strength;
            fader = new FaderUtil(0, in, out, false, true);
            fader.fadeIn();
        }

        //aging and sound stuff
        public void advance(float days) {
            fader.advance(days);
            if (withSound && fader.getBrightness() >= 0.5f) {
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
                float dist = 1000f + (1f - Math.min(1f, strength / 200f)) * 1450f;
                loc.scale(dist);
                Vector2f.add(loc, Global.getSector().getPlayerFleet().getLocation(), loc);
                Global.getSoundPlayer().playSound("ui_neutrino_detector_ping", 1, 1, loc, new Vector2f());
                withSound = false;
            }
        }
        
        //forced fadeout
        public boolean isDone() {
            return fader.isFadedOut();
        }
    }

    private final SKR_neutrinoScanAbility ability;

    private final float FULL_SCAN=15; //days to fully scan a system
    private final int RESOLUTION = 360;
    transient private float[] data;

    private final List<GSPing> pings = new ArrayList<>();

    private final IntervalUtil highSourcesInterval = new IntervalUtil(0.01f, 0.01f);
    private final IntervalUtil specialSourcesInterval = new IntervalUtil(0.075f, 0.125f);

    public SKR_neutrinoScanData(SKR_neutrinoScanAbility ability) {
        this.ability = ability;
    }

    private float scanLevel;
    
    public void advance(float days) {
        
        //skip conditions
        if (ability.getFleet() == null || ability.getFleet().getContainingLocation() == null) {
            return;
        }
        if (ability.getFleet().isInHyperspace()) {
            data = null;
            return;
        }
        
        //memory stuff
        String memKey = "$scan_"+ability.getFleet().getContainingLocation().getId();
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        //add to the total time spent with the ability active
        scanLevel=days/FULL_SCAN;        
        if(mem.contains(memKey)){
            scanLevel = Math.min((float)mem.get(memKey)+scanLevel, 1);
            mem.set(memKey, scanLevel);
        } else {
            mem.set(memKey, scanLevel);
        }
        
        //age existing pings, remove expired ones
        Iterator<GSPing> iter = pings.iterator();
        while (iter.hasNext()) {
            GSPing ping = iter.next();
            ping.advance(days);
            if (ping.isDone()) {
                iter.remove();
            }
        }

        //update high neutrino sources
        highSourcesInterval.advance(days);
        if (highSourcesInterval.intervalElapsed()) {
            highSourcePings();
        }
        
        //update low neutrino and false sources
        specialSourcesInterval.advance(days);
        if (specialSourcesInterval.intervalElapsed()) {
            specialSourcesPings();
        }
        
        //combine pings into an indexed data set for the sensor ring
        updateData();
    }

    
    public void updateData() {
        //create data set
        data = new float[RESOLUTION];
        float incr = 360f / (float) RESOLUTION;
        
        //add each ping to the data
        for (GSPing ping : pings) {
            
            //skip faded pings
            float brightness = ping.fader.getBrightness();
            if (brightness <= 0) {
                continue;
            }

            float arc = ping.arc;
            float facing = ping.angle;
            float half = (float) Math.ceil(0.5f * arc / incr);
            
            //add the ping's with its fading arc to the data set
            for (float i = -half; i <= half; i++) {
                float curr = facing + incr * i;
                int index = getIndex(curr);
                float intensity = 1f - Math.abs(i / half);
                intensity *= intensity;
                float value = ping.strength * intensity * brightness;
                //pings are cumulative
                data[index] += value;
            }
        }
    }


//    private int initialCount = 0; //That looks useless, lets try to remove it
    private final List<SectorEntityToken> special = new ArrayList<>();
    
    //low neutrino sources are dependent on range and come with fake signals
    private void specialSourcesPings() {
        
        CampaignFleetAPI fleet = ability.getFleet();
        Vector2f loc = fleet.getLocation();
        LocationAPI location = fleet.getContainingLocation();

        float neutrinoLowSkipProb = 0.8f-Math.max(scanLevel-0.2f,0); //0.8 skip probability at 0% scan level, 0.7 at 50%, 0.2 at 100%
        
        //list the relevant neutrino sources
        if (special.isEmpty()) {
            
            //add all low and average neutrino sources
            for (Object object : location.getEntities(CustomCampaignEntityAPI.class)) {
                if (object instanceof SectorEntityToken) {
                    SectorEntityToken entity = (SectorEntityToken) object;                    
                    //skip high sources: dealt by highSourcePings()
                    boolean neutrinoHigh = entity.hasTag(Tags.NEUTRINO_HIGH);
                    if (neutrinoHigh) {
                        continue;
                    }
                    boolean neutrino = entity.hasTag(Tags.NEUTRINO);
                    boolean neutrinoLow = entity.hasTag(Tags.NEUTRINO_LOW);
                    boolean station = entity.hasTag(Tags.STATION);                    
                    //not a neutrino source, does not show up on scanner
                    if (!neutrino && !neutrinoLow && !station) {
                        continue;
                    }                    
                    //randomly skip low sources
                    if (neutrinoLow && (float) Math.random() < neutrinoLowSkipProb) {
                        continue;
                    }
                    special.add(entity);
                }
            }
            
            //add fleets without the NEUTRINO_HIGH tag
            for (CampaignFleetAPI curr : location.getFleets()) {
                //skip player fleet
                if (fleet == curr) {
                    continue;
                }
                //skip high sources: dealt by highSourcePings()
                boolean neutrinoHigh = curr.hasTag(Tags.NEUTRINO_HIGH);
                if (neutrinoHigh) {
                    continue;
                }
                //randomly skip some of them
                if ((float) Math.random() < neutrinoLowSkipProb) {
                     continue;
                }
                special.add(curr);
            }

//            initialCount = special.size();
        }
        
        //looks suspiciously complicated, lets try to make it simple
//        int batch = (int) Math.ceil(initialCount / 1f);
//        for (int i = 0; i < batch; i++) {
        for (int i = 0; i < special.size(); i++) {
            
            //skip if there is nothing left
            if (special.isEmpty()) {
                break;
            }
            
            //extract first entry of the stack
            SectorEntityToken curr = special.remove(0);

            //distance
            float distance = Misc.getDistance(loc, curr.getLocation());
            
            //visible arc
            float mult = 2-scanLevel; //detection is fuzzy at low scan levels, but get increasingly precise over time
            float visibleArc = Misc.computeAngleSpan(curr.getRadius(), distance);
            visibleArc *= mult;
            if (visibleArc > 150f) {
                visibleArc = 150f;
            } else if (visibleArc < 7.5f*mult) {
                visibleArc = 7.5f*mult;
            }
            
            //direction
            float angle = Misc.getAngleInDegrees(loc, curr.getLocation());
            
            //signal strength
            float strength = getGravity(curr);
            strength *= getRangeGMult(distance);

            //fading
            float in = 0.05f + 0.1f * (float) Math.random();
            in *= 0.25f;
            float out = in;
            out *= 2f + 3*scanLevel;
            
            GSPing ping = new GSPing(angle, visibleArc, strength, in, out);
//            ping.withSound = true; //skip sound to make it related to false pings
            pings.add(ping);
        }

        //false signals
        if(scanLevel<1){
        
            long seed = (long) (location.getLocation().x * 1300000 + location.getLocation().y * 3700000 + 1213324234234L);
            Random random = new Random(seed);

            //number of false readings diminishes with the scan level            
            int numFalse = random.nextInt(1+(int)(7*(1-scanLevel)));

            for (int i = 0; i < numFalse; i++) {

                boolean constant = random.nextFloat() > 0.25f;
                if (!constant && (float) Math.random() < neutrinoLowSkipProb) {
                    random.nextFloat();
                    random.nextFloat();
                    continue;
                }

                float arc = 15;
                float angle = random.nextFloat() * 360f;
                float in = 0.05f + 0.1f * (float) Math.random();
                in *= 0.25f;
                float out = in;
                out *= 2f;

                float g = 80 + random.nextFloat() * 60;

                GSPing ping = new GSPing(angle, arc, g, in, out);
                ping.withSound = true;
                pings.add(ping);
            }
        }
    }

    
    //high neutrino sources are always visible regardless of distance
    private void highSourcePings() {
        
        CampaignFleetAPI fleet = ability.getFleet();
        Vector2f loc = fleet.getLocation();
        LocationAPI location = fleet.getContainingLocation();

        //create a list of all high neutrino sources, starting with all the planets
        List<SectorEntityToken> all = new ArrayList<SectorEntityToken>(location.getPlanets());
        
        //add all the custom entities with the NEUTRINO_HIGH tag
        for (Object object : location.getEntities(CustomCampaignEntityAPI.class)) {
            if (object instanceof SectorEntityToken) {
                SectorEntityToken entity = (SectorEntityToken) object;
                boolean neutrinoHigh = entity.hasTag(Tags.NEUTRINO_HIGH);
                if (neutrinoHigh) {
                    all.add(entity);
                }
            }
        }        
        //add all the fleets with the NEUTRINO_HIGH tag
        for (CampaignFleetAPI curr : location.getFleets()) {
            if (fleet == curr) {
                continue;
            }
            boolean neutrinoHigh = curr.hasTag(Tags.NEUTRINO_HIGH);
            if (neutrinoHigh) {
                all.add(curr);
            }
        }
        //add all the stations
        for (Object object : location.getEntities(OrbitalStationAPI.class)) {
            if (object instanceof SectorEntityToken) {
                SectorEntityToken entity = (SectorEntityToken) object;
                all.add(entity);
            }
        }
        //add all the jump-points
        for (Object object : location.getJumpPoints()) {
            if (object instanceof SectorEntityToken) {
                SectorEntityToken entity = (SectorEntityToken) object;
                all.add(entity);
            }
        }

        //now convert all those entities into appropriate sensor pings
        for (SectorEntityToken entity : all) {
            
            //skip "nebula center" fake planet
            if (entity instanceof PlanetAPI) {
                PlanetAPI planet = (PlanetAPI) entity;
                if (planet.getSpec().isNebulaCenter()) {
                    continue;
                }
            }
            //skip objects without visible radius
            if (entity.getRadius() <= 0) {
                continue;
            }

            //distance
            float distance = Misc.getDistance(loc, entity.getLocation());
           
            //visible arc
            float mult = 2-scanLevel; //detection is fuzzy at low scan levels, but get increasingly precise over time
            float visibleArc = Misc.computeAngleSpan(entity.getRadius(), distance);
            visibleArc *= mult;
            if (visibleArc > 150f) {
                visibleArc = 150f;
            } else if (visibleArc < 10*mult) {
                visibleArc = 10*mult;
            }
            
            //direction
            float angle = Misc.getAngleInDegrees(loc, entity.getLocation());
            
            //signal strength
            float strength = getGravity(entity);
            strength *= .1f;
            if (entity.hasTag(Tags.NEUTRINO_HIGH) || entity instanceof OrbitalStationAPI) {
                strength *= 2f;
            }
            strength *= getRangeGMult(distance);

            //NOT SURE WHAT THIS DOES, LETS IGNORE IT FOR NOW            
//            Vector2f netForce = new Vector2f();
//            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
//            dir.scale(graviticSignal);
//            Vector2f.add(netForce, dir, netForce);

            //fading
            float in = highSourcesInterval.getIntervalDuration() * 5f;
            float out = in;
            
            //create the actual ping
            GSPing ping = new GSPing(angle, visibleArc, strength, in, out);
            pings.add(ping);
        }
    }
    
    
    //get range multiplier for effective contact sensor signature
    public float getRangeGMult(float range) {
        range -= 3000;
        if (range < 0) {
            range = 0;
        }

        //range increases over time
        float max = 15000+(45000*scanLevel);
        if (range > max) {
            range = max;
        }

        return 1f - 0.85f * range / max;
    }
    
    
    //get a token's "sensor signature" 
    public float getGravity(SectorEntityToken entity) {
        float g = entity.getRadius();

        if (entity instanceof PlanetAPI) {
            PlanetAPI planet = (PlanetAPI) entity;

            g *= 2f;

            if (planet.getSpec().isBlackHole()) {
                g *= 2f;
            }
        }

        if (entity instanceof OrbitalStationAPI) {
            g *= 4f;
            if (g > 200) {
                g = 200;
            }
        }

        if (entity instanceof CustomCampaignEntityAPI) {
            g *= 4f;
            if (g > 200) {
                g = 200;
            }
        }

        if (entity instanceof CampaignFleetAPI) {
            g *= 2f;
            if (g > 200) {
                g = 200;
            }
        }
        return g;
    }
    
    
    //INTERFACE   
    
    
    //return data at an arbitrary angle
    public float getDataAt(float angle) {
        if (data == null) {
            return 0f;
        }
        int index = getIndex(angle);
        return data[index];
    }

    
    //convert float angle to sensor data index
    public int getIndex(float angle) {
        angle = Misc.normalizeAngle(angle);
        int index = (int) Math.floor(RESOLUTION * angle / 360f);
        return index;
    }
    
}
