package br.com.davidbuzatto.nonameplat.entities.characters;

import br.com.davidbuzatto.jsge.animation.AnimationExecutionState;
import br.com.davidbuzatto.jsge.animation.AnimationUtils;
import br.com.davidbuzatto.jsge.animation.frame.FrameByFrameAnimation;
import br.com.davidbuzatto.jsge.animation.frame.SpriteMapAnimationFrame;
import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.collision.aabb.AABB;
import br.com.davidbuzatto.jsge.collision.aabb.AABBQuadtree;
import br.com.davidbuzatto.jsge.collision.aabb.AABBQuadtreeNode;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
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
    private Vector2 sliceDim;
    private Vector2 posAdjust;
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
    
    // state management
    private State lookingState;
    private State xState;
    private State yState;
    private State helthState;
    
    // AABB
    private AABB aabb;
    
    public BaseEnemy( Vector2 pos, Color color ) {
        
        this.walkSpeed = 200;
        this.pos = pos;
        this.prevPos = new Vector2();
        this.dim = new Vector2( 54, 60 );
        this.sliceDim = new Vector2( 80, 80 );
        this.posAdjust = new Vector2( this.sliceDim.x - this.dim.x - 5, this.sliceDim.y - this.dim.y );
        this.vel = new Vector2( -walkSpeed, 0.0 );
        this.color = color;
        this.maxFallSpeed = 600;
        
        this.aabb = new AABB( pos.x, pos.y, pos.x + dim.x, pos.y + dim.y, AABB.Type.DYNAMIC, this );
        
        this.cpLeft = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpRight = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpUp = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpDown = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        
        this.lookingState = State.LOOKING_LEFT;
        this.xState = State.IDLE;
        this.yState = State.ON_GROUND;
        this.helthState = State.ALIVE;
        
        loadImagesAndCreateAnimations();
        
    }
    
    public void update( double worldWidth, double worldHeight, List<Tile> tiles, AABBQuadtree quadtree, double delta ) {
        
        if ( helthState != State.DEAD ) {
            
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

            if ( vel.y < 0 ) {
                yState = State.JUMPING;
            } else if ( vel.y > 0 ) {
                yState = State.FALLING;
            } else {
                yState = State.ON_GROUND;
            }

            vel.y += GameWorld.GRAVITY * delta;

            if ( vel.y > maxFallSpeed ) {
                vel.y = maxFallSpeed;
            }

            if ( helthState == State.ALIVE ) {
                idleAnimationRight.update( delta );
                idleAnimationLeft.update( delta );
                walkAnimationRight.update( delta );
                walkAnimationLeft.update( delta );
            } else if ( helthState == State.DYING ) {
                deathAnimationRight.update( delta );
                deathAnimationLeft.update( delta );
            }

            if ( deathAnimationRight.getState() == AnimationExecutionState.FINISHED ) {
                helthState = State.DEAD;
                aabb.active = false;
            }

            prevPos.x = pos.x;
            prevPos.y = pos.y;
            aabb.moveTo( pos.x, pos.y );

            updateCollisionProbes();
            
        }
        
    }
    
    public void draw( EngineFrame e ) {
        
        if ( lookingState == State.LOOKING_RIGHT ) {
            if ( helthState == State.ALIVE ) {
                if ( yState == State.ON_GROUND || yState == State.FALLING ) {
                    if ( xState == State.MOVING ) {
                        walkAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    } else {
                        idleAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    }
                }
            } else if ( helthState == State.DYING ) {
                deathAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
            }
        } else {
            if ( helthState == State.ALIVE ) {
                if ( yState == State.ON_GROUND || yState == State.FALLING ) {
                    if ( xState == State.MOVING ) {
                        walkAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    } else {
                        idleAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    }
                }
            } else if ( helthState == State.DYING ) {
                deathAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
            }
        }
        
        if ( GameWorld.SHOW_BOUNDARIES ) {
            e.fillAABB( aabb, ColorUtils.fade( color, 0.4 ) );
            e.drawAABB( aabb, EngineFrame.BLACK );
        }
        
        if ( GameWorld.SHOW_COLLISION_PROBES ) {
            drawCollisionProbes( e );
        }
        
    }
    
    private void drawCollisionProbes( EngineFrame e ) {
        
        e.fillRectangle( cpLeft, lookingState == State.LOOKING_LEFT ? GameWorld.CP_COLOR1 : GameWorld.CP_COLOR2 );
        e.fillRectangle( cpRight, lookingState == State.LOOKING_RIGHT ? GameWorld.CP_COLOR1 : GameWorld.CP_COLOR2 );
        e.fillRectangle( cpUp, yState == State.JUMPING ? GameWorld.CP_COLOR1 : GameWorld.CP_COLOR2 );
        if ( yState == State.ON_GROUND ) {
            e.fillRectangle( cpDown, GameWorld.CP_COLOR3 );
        } else {
            e.fillRectangle( cpDown, yState == State.FALLING ? GameWorld.CP_COLOR1 : GameWorld.CP_COLOR2 );
        }
        
    }
    
    public void updateCollisionProbes() {
        
        cpLeft.x = pos.x;
        cpLeft.y = pos.y + dim.y / 2 - cpLeft.height / 2;
        cpRight.x = pos.x + dim.x - cpRight.width ;
        cpRight.y = pos.y + dim.y / 2 - cpRight.height / 2;
        cpUp.x = pos.x + dim.x / 2 - cpUp.width / 2;
        cpUp.y = pos.y;
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
                pos.x = tile.getPos().x + tile.getDim().x;
                vel.x = -vel.x;
                break;
            case RIGHT:
                pos.x = tile.getPos().x - dim.x;
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
        
        deathAnimationLeft = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( deathImageMap, sliceDim.x, sliceDim.y ),
            false
        );
        deathAnimationRight = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( deathImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            false
        );
        
        idleAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        idleAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            true
        );
        
        walkAnimationLeft = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        walkAnimationRight = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
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
    
    public boolean isAlive() {
        return this.helthState == State.ALIVE;
    }
    
    public void prepareToDie() {
        this.helthState = State.DYING;
        this.vel.x = 0;
    }
    
}
