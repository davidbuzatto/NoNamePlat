package br.com.davidbuzatto.nonameplat.entities.characters;

import br.com.davidbuzatto.jsge.animation.AnimationUtils;
import br.com.davidbuzatto.jsge.animation.frame.FrameByFrameAnimation;
import br.com.davidbuzatto.jsge.animation.frame.SpriteMapAnimationFrame;
import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.collision.aabb.AABB;
import br.com.davidbuzatto.jsge.collision.aabb.AABBQuadtree;
import br.com.davidbuzatto.jsge.collision.aabb.AABBQuadtreeNode;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.GameWorld;
import br.com.davidbuzatto.nonameplat.entities.CollisionType;
import br.com.davidbuzatto.nonameplat.entities.Entity;
import br.com.davidbuzatto.nonameplat.entities.tiles.Tile;
import java.awt.Color;
import java.util.List;

/**
 * The base enemy :D
 * 
 * @author Prof. Dr. David Buzatto
 */
public class BaseEnemy extends Entity {
    
    private static final double CP_WIDTH_SML = 10;
    private static final double CP_WIDTH_BIG = 20;
    
    private Vector2 pos;
    private Vector2 prevPos;
    private Vector2 dim;
    private Vector2 vel;
    private Color color;

    private double walkSpeed;
    private double maxFallSpeed;
    
    private Image attackImageMap;
    private Image deathImageMap;
    private Image hurtImageMap;
    private Image idleImageMap;
    private Image walkImageMap;
    
    private FrameByFrameAnimation<SpriteMapAnimationFrame> attackAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> attackAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> deathAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> deathAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> hurtAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> hurtAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> idleAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> idleAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> walkAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> walkAnimationLeft;
    
    // collision probes
    private Rectangle cpLeft;
    private Rectangle cpRight;
    private Rectangle cpUp;
    private Rectangle cpDown;
    private double cpLeftAdjust;
    private double cpRightAdjust;
    private double cpUpAdjust;
    private double cpDownAdjust;
    
    // state management
    private State lookingState;
    private State xState;
    
    // AABB
    private AABB aabb;
    
    public BaseEnemy( Vector2 pos, Color color ) {
        
        this.walkSpeed = 200;
        this.pos = pos;
        this.prevPos = new Vector2();
        this.dim = new Vector2( 80, 80 );
        this.vel = new Vector2( -walkSpeed, 0.0 );
        this.color = color;
        this.maxFallSpeed = 600;
        
        this.aabb = new AABB( pos.x, pos.y, pos.x + dim.x, pos.y + dim.y, AABB.Type.DYNAMIC, this );
        
        this.cpLeft = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpRight = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpUp = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpDown = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpLeftAdjust = 20;
        this.cpRightAdjust = -5;
        this.cpUpAdjust = 15;
        this.cpDownAdjust = 10;
        
        this.lookingState = State.LOOKING_LEFT;
        this.xState = State.IDLE;
        
        loadImagesAndCreateAnimations();
        
    }
    
    public void update( double worldWidth, double worldHeight, List<Tile> tiles, AABBQuadtree quadtree, double delta ) {
        
        pos.x += vel.x * delta;
        pos.y += vel.y * delta;
        
        if ( vel.x < 0 ) {
            lookingState = State.LOOKING_LEFT;
            xState = State.MOVING;
        } else if ( vel.x > 0 )  {
            lookingState = State.LOOKING_RIGHT;
            xState = State.MOVING;
        } else {
            xState = State.IDLE;
        }
        
        resolveCollisionQuadtree( quadtree );
        
        vel.y += GameWorld.GRAVITY;
        
        if ( vel.y > maxFallSpeed ) {
            vel.y = maxFallSpeed;
        }
        
        idleAnimationRight.update( delta );
        idleAnimationLeft.update( delta );
        walkAnimationRight.update( delta );
        walkAnimationLeft.update( delta );
        
        prevPos.x = pos.x;
        prevPos.y = pos.y;
        aabb.moveTo( pos.x, pos.y );
        
        updateCollisionProbes();
        
    }
    
    public void draw( EngineFrame e ) {
        
        /*e.fillRectangle( pos.x, pos.y, dim.x, dim.y, color );
        e.drawRectangle( pos.x, pos.y, dim.x, dim.y, EngineFrame.BLACK );*/
        
        if ( lookingState == State.LOOKING_RIGHT ) {
            if ( xState == State.MOVING ) {
                walkAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
            } else {
                idleAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
            }
        } else {
            if ( xState == State.MOVING ) {
                walkAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
            } else {
                idleAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
            }
        }
        
        //drawCollisionProbes( e );
        
    }
    
    private void drawCollisionProbes( EngineFrame e ) {
        
        e.fillRectangle( cpLeft, lookingState == State.LOOKING_LEFT ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpRight, lookingState == State.LOOKING_RIGHT ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpUp, EngineFrame.GREEN );
        e.fillRectangle( cpDown, EngineFrame.GREEN );
        
    }
    
    public void updateCollisionProbes() {
        
        cpLeft.x = pos.x + cpLeftAdjust;
        cpLeft.y = pos.y + dim.y / 2 - cpLeft.height / 2;
        cpRight.x = pos.x + dim.x - cpRight.width + cpRightAdjust;
        cpRight.y = pos.y + dim.y / 2 - cpRight.height / 2;
        cpUp.x = pos.x + dim.x / 2 - cpUp.width / 2;
        cpUp.y = pos.y + cpUpAdjust;
        cpDown.x = pos.x + dim.x / 2 - cpDown.width / 2;
        cpDown.y = pos.y + dim.y - cpDown.height;
        
    }
    
    public CollisionType checkCollisionTile( Tile tile ) {
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpDown, tile.getAABB() ) ) {
            return CollisionType.DOWN;
        }
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpLeft, tile.getAABB() ) ) {
            return CollisionType.LEFT;
        }
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpRight, tile.getAABB() ) ) {
            return CollisionType.RIGHT;
        }
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpUp, tile.getAABB() ) ) {
            return CollisionType.UP;
        }
        
        return CollisionType.NONE;
        
    }
    
    public void resolveCollisionQuadtree( AABBQuadtree quadtree ) {
        resolveCollisionQuadnode( quadtree.getRoot(), quadtree.getMaxDepth() );
    }
    
    private void resolveCollisionQuadnode( AABBQuadtreeNode node, int maxTreeDepth ) {
        
        if ( node.depth < maxTreeDepth ) {
            
            int size = node.aabbs.size();
            for ( int i = 0; i < size; i++ ) {
                for ( int j = i+1; j < size; j++ ) {
                    try {
                        AABB a = node.aabbs.get( i );
                        AABB b = node.aabbs.get( j );
                        if ( a.referencedObject instanceof BaseEnemy ) {
                            if ( b.referencedObject instanceof Tile t ) {
                                resolveCollisionTile( t );
                            } else {
                                break;
                            }
                        }
                    } catch ( IndexOutOfBoundsException | NullPointerException exc ) {
                    }
                }
            }
            
            resolveCollisionQuadnode( node.nw, maxTreeDepth );
            resolveCollisionQuadnode( node.ne, maxTreeDepth );
            resolveCollisionQuadnode( node.sw, maxTreeDepth );
            resolveCollisionQuadnode( node.se, maxTreeDepth );
            
        }
        
    }
    
    public void resolveCollisionTile( Tile tile ) {
        
        CollisionType c = checkCollisionTile( tile );
        
        switch ( c ) {
            case DOWN:
                pos.y = tile.getPos().y - dim.y;
                vel.y = 0;
                break;
            case LEFT:
                pos.x = tile.getPos().x + tile.getDim().x - cpLeftAdjust;
                vel.x = -vel.x;
                break;
            case RIGHT:
                pos.x = tile.getPos().x - dim.x - cpRightAdjust;
                vel.x = -vel.x;
                break;
            case UP:
                vel.y = 0;
                pos.y = tile.getPos().y + tile.getDim().y;
                break;
        }
        
        updateCollisionProbes();
        
    }
    
    private void loadImagesAndCreateAnimations() {
        
        attackImageMap = ImageUtils.loadImage( "resources/images/sprites/enemies/bear/attack.png" );
        deathImageMap = ImageUtils.loadImage( "resources/images/sprites/enemies/bear/death.png" );
        hurtImageMap = ImageUtils.loadImage( "resources/images/sprites/enemies/bear/hurt.png" );
        idleImageMap = ImageUtils.loadImage( "resources/images/sprites/enemies/bear/idle.png" );
        walkImageMap = ImageUtils.loadImage( "resources/images/sprites/enemies/bear/walk.png" );
        
        idleAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap, dim.x, dim.y ),
            true
        );
        idleAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
        walkAnimationLeft = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap, dim.x, dim.y ),
            true
        );
        walkAnimationRight = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
    }
    
    public Vector2 getPos() {
        return pos;
    }

    public Vector2 getDim() {
        return dim;
    }

    public Vector2 getPrevPos() {
        return prevPos;
    }

    public Vector2 getVel() {
        return vel;
    }
    
    public boolean isMoving() {
        return xState == State.MOVING;
    }

    public AABB getAABB() {
        return aabb;
    }
    
}
