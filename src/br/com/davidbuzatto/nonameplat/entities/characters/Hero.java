package br.com.davidbuzatto.nonameplat.entities.characters;

import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.GameWorld;
import br.com.davidbuzatto.nonameplat.entities.CollisionType;
import br.com.davidbuzatto.nonameplat.entities.Entity;
import br.com.davidbuzatto.nonameplat.entities.EntityState;
import br.com.davidbuzatto.nonameplat.entities.tiles.Tile;
import java.awt.Color;
import java.util.List;

/**
 * The game hero!
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Hero extends Entity {
    
    private static final double CP_WIDTH_SML = 10;
    private static final double CP_WIDTH_BIG = 30;
    
    private Vector2 pos;
    private Vector2 dim;
    private Vector2 vel;
    private Color color;

    private double walkSpeed;
    private double runSpeed;
    private double jumpSpeed;
    
    private double maxFallSpeed;
    
    private EntityState xState;
    private EntityState yState;
    private EntityState lookingState;
    private EntityState healthState;
    
    // collision probes
    private Rectangle cpLeft;
    private Rectangle cpRight;
    private Rectangle cpUp;
    private Rectangle cpDown;
    
    public Hero( Vector2 pos, Color color ) {
        
        this.pos = pos;
        this.dim = new Vector2( GameWorld.BASE_WIDTH, GameWorld.BASE_WIDTH );
        this.vel = new Vector2();
        this.color = color;
        this.walkSpeed = 400;
        this.runSpeed = 600;
        this.jumpSpeed = -450;
        this.maxFallSpeed = 600;
        
        this.lookingState = EntityState.LOOKING_RIGHT;
        this.xState = EntityState.IDLE;
        this.yState = EntityState.ON_GROUND;
        this.healthState = EntityState.ALIVE;
        
        this.cpLeft = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpRight = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpUp = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpDown = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        
    }
    
    public void update( EngineFrame e, double worldWidth, double worldHeight, double delta ) {
        
        pos.x += vel.x * delta;
        pos.y += vel.y * delta;
        
        if ( e.isKeyDown( EngineFrame.KEY_LEFT ) ) {
            vel.x = -walkSpeed;
        } else if ( e.isKeyDown( EngineFrame.KEY_RIGHT ) ) {
            vel.x = walkSpeed;
        } else {
            vel.x = 0;
        }
        
        if ( e.isKeyPressed( EngineFrame.KEY_SPACE ) ) {
            vel.y = jumpSpeed;
        }
        
        vel.y += GameWorld.GRAVITY;
        
        if ( vel.y > maxFallSpeed ) {
            vel.y = maxFallSpeed;
        }
        
        if ( vel.x < 0 ) {
            lookingState = EntityState.LOOKING_LEFT;
            xState = EntityState.WALKING;
        } else if ( vel.x > 0 ) {
            lookingState = EntityState.LOOKING_RIGHT;
            xState = EntityState.WALKING;
        } else {
            xState = EntityState.IDLE;
        }
        
        if ( vel.y < 0 ) {
            yState = EntityState.JUMPING;
        } else if ( vel.y > 0 ) {
            yState = EntityState.FALLING;
        } else {
            yState = EntityState.ON_GROUND;
        }
        
        updateCollisionProbes();
        
    }
    
    public void draw( EngineFrame e ) {
        
        e.fillRectangle( pos, dim, color );
        e.drawRectangle( pos, dim, EngineFrame.BLACK );
        
        e.fillRectangle( cpLeft, lookingState == EntityState.LOOKING_LEFT ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpRight, lookingState == EntityState.LOOKING_RIGHT ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpUp, yState == EntityState.JUMPING ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpDown, yState == EntityState.FALLING ? EngineFrame.GREEN : EngineFrame.RED );
        
    }
    
    public void updateCollisionProbes() {
        cpLeft.x = pos.x;
        cpLeft.y = pos.y + dim.y / 2 - cpLeft.height / 2;
        cpRight.x = pos.x + dim.x - cpRight.width;
        cpRight.y = pos.y + dim.y / 2 - cpRight.height / 2;
        cpUp.x = pos.x + dim.x / 2 - cpUp.width / 2;
        cpUp.y = pos.y;
        cpDown.x = pos.x + dim.x / 2 - cpDown.width / 2;
        cpDown.y = pos.y + dim.y - cpDown.height;
    }
    
    public CollisionType checkCollisionTile( Tile tile ) {
        
        if ( CollisionUtils.checkCollisionRectangles( cpDown, tile.getBB() ) ) {
            return CollisionType.DOWN;
        }
        
        if ( CollisionUtils.checkCollisionRectangles( cpLeft, tile.getBB() ) ) {
            return CollisionType.LEFT;
        }
        
        if ( CollisionUtils.checkCollisionRectangles( cpRight, tile.getBB() ) ) {
            return CollisionType.RIGHT;
        }
        
        if ( CollisionUtils.checkCollisionRectangles( cpUp, tile.getBB() ) ) {
            return CollisionType.UP;
        }
        
        return CollisionType.NONE;
        
    }
    
    public void resolveCollisionTiles( List<Tile> tiles ) {
        
        for ( Tile tile : tiles ) {
            CollisionType c = checkCollisionTile( tile );
            switch ( c ) {
                case DOWN:
                    pos.y = tile.getPos().y - dim.y;
                    break;
                case LEFT:
                    pos.x = tile.getPos().x + tile.getDim().x;
                    break;
                case RIGHT:
                    pos.x = tile.getPos().x - dim.x;
                    break;
                case UP:
                    vel.y = 0;
                    pos.y = tile.getPos().y + tile.getDim().y;
                    break;
            }
            updateCollisionProbes();
        }        
        
    }
    
    public Vector2 getPos() {
        return pos;
    }

    public Vector2 getDim() {
        return dim;
    }
    
}
