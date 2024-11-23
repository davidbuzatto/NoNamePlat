package br.com.davidbuzatto.nonameplat;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import br.com.davidbuzatto.nonameplat.entities.characters.Hero;

/**
 * A parallax engine for background.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class ParallaxEngine {
        
    private final Image[] images;
    private final double worldWidth;
    private final double halfScreenWidth;
    private final double screenHeight;
    private final double baseLayerSpeed;
    private final int imageWidth;
    private final int imageHeight;
    private final int repeats;
    
    public ParallaxEngine( double worldWidth, double screenWidth, double screenHeight, double baseLayerSpeed ) {
        
        this.images = new Image[]{
            ImageUtils.loadImage( "resources/images/background/7.png" ),
            ImageUtils.loadImage( "resources/images/background/6.png" ),
            ImageUtils.loadImage( "resources/images/background/5.png" ),
            ImageUtils.loadImage( "resources/images/background/4.png" ),
            ImageUtils.loadImage( "resources/images/background/3.png" ),
            ImageUtils.loadImage( "resources/images/background/2.png" ),
            ImageUtils.loadImage( "resources/images/background/1.png" )
        };
        
        this.worldWidth = worldWidth;
        this.halfScreenWidth = screenWidth / 2;
        this.screenHeight = screenHeight;
        this.baseLayerSpeed = baseLayerSpeed;
        this.imageWidth = this.images[0].getWidth();
        this.imageHeight = this.images[0].getHeight();
        this.repeats = (int) ( this.worldWidth / this.imageWidth ) + 1;

    }

    public void draw( EngineFrame e, Hero hero ) {
        
        for ( int i = 0; i < images.length; i++ ) {
            
            double x = hero.getPos().x;
            
            if ( hero.getPos().x <= halfScreenWidth ) {
                x = halfScreenWidth;
            } else if ( hero.getPos().x >= worldWidth - halfScreenWidth ) {
                x = worldWidth - halfScreenWidth;
            }
            
            for ( int j = 0; j < repeats; j++ ) {
                e.drawImage( 
                    images[i], 
                    ( j * imageWidth ) - ( ( x - halfScreenWidth ) * ( baseLayerSpeed * ( i + 1 ) ) ), 
                    screenHeight - imageHeight
                );
            }
        }
        
    }

}
