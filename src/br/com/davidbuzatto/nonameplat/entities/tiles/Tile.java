package br.com.davidbuzatto.nonameplat.entities.tiles;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.GameWorld;
import java.awt.Color;

/**
 * On tile for the world.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Tile {
    
    private Vector2 pos;
    private Vector2 dim;
    private Color color;
    
    private Rectangle bb;
    
    public Tile( Vector2 pos, Color color ) {
        this.pos = pos;
        this.dim = new Vector2( GameWorld.BASE_WIDTH, GameWorld.BASE_WIDTH );
        this.color = color;
        bb = new Rectangle( pos.x, pos.y, dim.x, dim.y );
    }
    
    public void draw( EngineFrame e ) {
        e.fillRectangle( pos, dim, color );
        e.drawRectangle( pos, dim, EngineFrame.BLACK );
    }

    public Vector2 getPos() {
        return pos;
    }

    public Vector2 getDim() {
        return dim;
    }

    public Rectangle getBB() {
        return bb;
    }
    
}
