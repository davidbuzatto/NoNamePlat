package br.com.davidbuzatto.nonameplat.entities.tiles;

import br.com.davidbuzatto.jsge.collision.aabb.AABB;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.GameWorld;
import br.com.davidbuzatto.nonameplat.entities.Entity;
import java.awt.Color;

/**
 * One tile for the world.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Tile extends Entity {
    
    private Vector2 pos;
    private Vector2 dim;
    private Color color;
    private Image skin;
    
    // AABB
    private AABB aabb;
    
    public Tile( Vector2 pos, Color color, Image skin ) {
        this.pos = pos;
        this.dim = new Vector2( GameWorld.BASE_WIDTH, GameWorld.BASE_WIDTH );
        this.color = color;
        this.skin = skin;
        this.aabb = new AABB( pos.x, pos.y, pos.x + dim.x, pos.y + dim.y, AABB.Type.STATIC, this );
    }
    
    public Tile( Vector2 pos, Color color ) {
        this( pos, color, null );
    }
    
    public Tile( Vector2 pos ) {
        this( pos, EngineFrame.WHITE, null );
    }
    
    public void draw( EngineFrame e ) {
        
        if ( skin != null ) {
            e.drawImage( skin, pos.x, pos.y );
        } else {
            e.fillRectangle( pos, dim, color );
            e.drawRectangle( pos, dim, EngineFrame.BLACK );
        }
        
        if ( GameWorld.SHOW_BOUNDARIES ) {
            e.fillAABB( aabb, ColorUtils.fade( color, 0.2 ) );
            e.drawAABB( aabb, EngineFrame.BLACK );
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
    
}
