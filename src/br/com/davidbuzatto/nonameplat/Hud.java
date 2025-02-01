package br.com.davidbuzatto.nonameplat;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.entities.characters.Hero;

/**
 * Hud representation for hero.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Hud {
    
    private Vector2 pos;
    private Hero hero;
    private Image heartImageFull;
    private Image heartImageEmpty;
    private Image portraitImage;
    private Image coinImage;
    
    public Hud( double x, double y, Hero hero, Image heartImage, Image portraitImage, Image coinImage ) {
        this.pos = new Vector2( x, y );
        this.hero = hero;
        this.heartImageFull = heartImage;
        this.heartImageEmpty = heartImage.copyColorGrayscale();
        this.portraitImage = portraitImage;
        this.coinImage = coinImage;
    }
    
    public void update( double delta ) {
        
    }
    
    public void draw( EngineFrame engine ) {
        
        double margin = 5;
        double y = pos.y;
        
        for ( int i = 0; i < hero.getMaxHp(); i++ ) {
            if ( i < hero.getHp() ) {
                engine.drawImage( heartImageFull, pos.x + ( heartImageFull.getWidth() + margin ) * i, y );
            } else {
                engine.drawImage( heartImageEmpty, pos.x + ( heartImageFull.getWidth() + margin ) * i, y );
            }
        }
        
        y += heartImageFull.getHeight() + margin;
        engine.drawImage( coinImage, pos.x + 3, y );
        engine.drawText( "x", pos.x + coinImage.getHeight() + margin + 6, y + 2, EngineFrame.BLACK );
        engine.drawText( "x", pos.x + coinImage.getHeight() + margin + 4, y, EngineFrame.WHITE );
        engine.drawText( String.valueOf( hero.getCoins() ), pos.x + coinImage.getHeight() + margin + 24, y + 3, EngineFrame.BLACK );
        engine.drawText( String.valueOf( hero.getCoins() ), pos.x + coinImage.getHeight() + margin + 22, y + 1, EngineFrame.WHITE );
        
        engine.drawImage( portraitImage, pos.x, engine.getScreenHeight() - pos.x - portraitImage.getHeight() );
        engine.drawText( "x", pos.x + portraitImage.getWidth() + margin + 2, engine.getScreenHeight() - pos.x - portraitImage.getHeight() / 2, EngineFrame.BLACK );
        engine.drawText( "x", pos.x + portraitImage.getWidth() + margin, engine.getScreenHeight() - pos.x - portraitImage.getHeight() / 2 - 2, EngineFrame.WHITE );
        engine.drawText( String.valueOf( hero.getLives() ), pos.x + portraitImage.getWidth() + margin + 20, engine.getScreenHeight() - pos.x - portraitImage.getHeight() / 2 + 1, EngineFrame.BLACK );
        engine.drawText( String.valueOf( hero.getLives() ), pos.x + portraitImage.getWidth() + margin + 18, engine.getScreenHeight() - pos.x - portraitImage.getHeight() / 2 - 1, EngineFrame.WHITE );
        
    }
    
}
