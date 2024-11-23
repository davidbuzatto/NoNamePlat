package br.com.davidbuzatto.nonameplat.utils;

import br.com.davidbuzatto.jsge.image.Image;
import java.awt.Color;

/**
 *
 * @author Prof. Dr. David Buzatto
 */
public class Utils {
    
    private static final Color[] FROM_HERO_COLORS = new Color[]{
        new Color( 244, 137, 246 ),
        new Color( 216, 64, 251 ),
        new Color( 120, 11, 247 )
    };

    private static final Color[] TO_HERO_COLORS = new Color[]{
        new Color( 106, 156, 246 ),
        new Color( 15, 94, 238 ),
        new Color( 9, 56, 147 )
    };
    
    public static void replaceHeroImageColors( Image image ) {
        
        for ( int i = 0; i < FROM_HERO_COLORS.length; i++ ) {
            image.colorReplace( FROM_HERO_COLORS[i], TO_HERO_COLORS[i] );
        }
        
    }
    
}
