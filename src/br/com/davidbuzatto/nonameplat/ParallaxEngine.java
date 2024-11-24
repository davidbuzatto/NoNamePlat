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
    private final double worldHeight;
    private final double screenWidth;
    private final double screenHeight;
    private final double halfScreenWidth;
    private final double halfScreenHeight;
    private final double baseLayerSpeed;
    private final int imageWidth;
    private final int imageHeight;
    private final int repeats;
    
    public ParallaxEngine( double worldWidth, double worldHeight, double screenWidth, double screenHeight, double baseLayerSpeed ) {
        
        this.images = new Image[]{
            ImageUtils.loadImage( "resources/images/background/field/7.png" ),
            ImageUtils.loadImage( "resources/images/background/field/6.png" ),
            ImageUtils.loadImage( "resources/images/background/field/5.png" ),
            ImageUtils.loadImage( "resources/images/background/field/4.png" ),
            ImageUtils.loadImage( "resources/images/background/field/3.png" ),
            ImageUtils.loadImage( "resources/images/background/field/2.png" ),
            ImageUtils.loadImage( "resources/images/background/field/1.png" )
        };
        
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.halfScreenWidth = screenWidth / 2;
        this.halfScreenHeight = screenHeight / 2;
        this.baseLayerSpeed = baseLayerSpeed;
        this.imageWidth = this.images[0].getWidth();
        this.imageHeight = this.images[0].getHeight();
        this.repeats = (int) ( this.worldWidth / this.imageWidth ) + 1;

    }

    public void draw( EngineFrame e, Hero hero ) {
        
        for ( int i = 0; i < images.length; i++ ) {
            
            double x = hero.getPos().x;
            double y = hero.getPos().y;
            
            if ( x <= halfScreenWidth ) {
                x = halfScreenWidth;
            } else if ( x >= worldWidth - halfScreenWidth ) {
                x = worldWidth - halfScreenWidth;
            }
            
            if ( y <= worldHeight - halfScreenHeight ) {
                y = worldHeight - y;
            } else {
                y = halfScreenHeight;
            }
            
            for ( int j = 0; j < repeats; j++ ) {
                e.drawImage( 
                    images[i], 
                    ( j * imageWidth ) - ( ( x - halfScreenWidth ) * ( baseLayerSpeed * ( i + 1 ) ) ), 
                    screenHeight - imageHeight + ( ( y - halfScreenHeight ) * ( baseLayerSpeed * ( i + 1 ) ) )
                );
            }
        }
        
    }

}
