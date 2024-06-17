/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class SKR_txt {   
    private static final String ML="seeker";    
    
    public static String txt(String id){
        return Global.getSettings().getString(ML, id);
    }       
}