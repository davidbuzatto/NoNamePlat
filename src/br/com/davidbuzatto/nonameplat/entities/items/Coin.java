package br.com.davidbuzatto.nonameplat.entities.items;

import br.com.davidbuzatto.jsge.animation.AnimationUtils;
import br.com.davidbuzatto.jsge.animation.frame.FrameByFrameAnimation;
import br.com.davidbuzatto.jsge.animation.frame.SpriteMapAnimationFrame;
import br.com.davidbuzatto.jsge.collision.aabb.AABB;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.GameWorld;
import br.com.davidbuzatto.nonameplat.entities.Entity;
import java.awt.Color;

/**
 * A coin.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Coin extends Entity {
    
    private Vector2 pos;
    private Vector2 dim;
    private Color color;
    
    private State state;
    
    private Image imageMap;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> animation;
    
    // AABB
    private AABB aabb;
    
    public Coin( Vector2 pos, Color color ) {
        
        this.pos = pos;
        this.dim = new Vector2( 34, 34 );
        this.color = color;
        this.aabb = new AABB( pos.x, pos.y, pos.x + dim.x, pos.y + dim.y, AABB.Type.STATIC, this );
        
        this.state = State.ACTIVE;
        
        this.imageMap = ImageUtils.loadImage( "resources/images/sprites/items/bigCoin.png" );
        this.animation = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( imageMap, dim.x, dim.y ),
            true
        );
        
    }
    
    public void update( double delta ) {
        if ( state == State.ACTIVE ) {
            animation.update( delta );
        }
    }
    
    public void draw( EngineFrame e ) {
        
        if ( state == State.ACTIVE ) {
            
            animation.getCurrentFrame().draw( e, pos.x, pos.y );

            if ( GameWorld.SHOW_BOUNDARIES ) {
                e.fillAABB( aabb, ColorUtils.fade( color, 0.2 ) );
                e.drawAABB( aabb, EngineFrame.BLACK );
            }
            
        }
        
    }

    public Vector2 getPos() {
        return pos;
    }

    public Vector2 getDim() {
        return dim;
    }

    public AABB getAABB() {
        return aabb;
    }
    
    public boolean isActive() {
        return state == State.ACTIVE;
    }
    
    public void collect() {
        state = State.INACTIVE;
        aabb.active = false;
    }
    
}
