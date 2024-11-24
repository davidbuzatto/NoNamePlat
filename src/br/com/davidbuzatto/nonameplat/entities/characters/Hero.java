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
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.GameWorld;
import br.com.davidbuzatto.nonameplat.entities.CollisionType;
import br.com.davidbuzatto.nonameplat.entities.Entity;
import br.com.davidbuzatto.nonameplat.entities.tiles.Tile;
import br.com.davidbuzatto.nonameplat.utils.Utils;
import java.awt.Color;
import java.util.List;

/**
 * The game hero!
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Hero extends Entity {
    
    private static final double CP_WIDTH_SML = 10;
    private static final double CP_WIDTH_BIG = 20;
    
    private Vector2 pos;
    private Vector2 prevPos;
    private Vector2 dim;
    private Vector2 vel;
    private Color color;

    private double walkSpeed;
    private double maxAcceleration;
    private double jumpSpeed;
    
    private double maxFallSpeed;
    
    private int remainingJumps;
    private Vector2 doubleJumpPos;
    
    private Image idleImageMap;
    private Image walkImageMap;
    private Image runImageMap;
    private Image dustImageMap;
    private Image pushImageMap;
    private Image jumpImageMap;
    private Image doubleJumpDustImageMap;
    
    private FrameByFrameAnimation<SpriteMapAnimationFrame> idleAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> idleAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> walkAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> walkAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> runAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> runAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> dustAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> dustAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> pushAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> pushAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> jumpAnimationRight;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> jumpAnimationLeft;
    private FrameByFrameAnimation<SpriteMapAnimationFrame> doubleJumpDustAnimation;
    
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
    private State yState;
    
    private boolean running;
    private boolean pushing;
    
    // acceleration
    private int accelerationStep;
    private int accelerationStepTick;
    private double accelerationMaxStep;
    private double nextAccelerationTickCounter;
    private double nextAccelerationTickTime;
    
    // AABB
    private AABB aabb;
    
    public Hero( Vector2 pos, Color color ) {
        
        this.pos = pos;
        this.prevPos = new Vector2();
        this.dim = new Vector2( GameWorld.BASE_WIDTH, GameWorld.BASE_WIDTH );
        this.vel = new Vector2();
        this.color = color;
        this.walkSpeed = 300;
        this.maxAcceleration = 200;
        this.jumpSpeed = -450;
        this.maxFallSpeed = 600;
        
        this.aabb = new AABB( pos.x, pos.y, pos.x + dim.x, pos.y + dim.y, AABB.Type.DYNAMIC, this );
        
        this.remainingJumps = 2;
        this.doubleJumpPos = new Vector2();
        
        this.cpLeft = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpRight = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpUp = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpDown = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpLeftAdjust = 10;
        this.cpRightAdjust = -10;
        this.cpUpAdjust = 10;
        this.cpDownAdjust = 10;
        
        this.lookingState = State.LOOKING_RIGHT;
        this.xState = State.IDLE;
        this.yState = State.ON_GROUND;
        this.running = false;
        this.pushing = false;
        
        this.accelerationStep = 0;
        this.accelerationStepTick = 1;
        this.accelerationMaxStep = 10;
        this.nextAccelerationTickCounter = 0;
        this.nextAccelerationTickTime = 0.1;
        
        loadImagesAndCreateAnimations();
        
    }
    
    public void update( EngineFrame e, double worldWidth, double worldHeight, List<Tile> tiles, AABBQuadtree quadtree, double delta ) {
        
        pos.x += vel.x * delta;
        pos.y += vel.y * delta;
        
        if ( e.isKeyDown( EngineFrame.KEY_CONTROL ) ) {
            if ( accelerationStep < accelerationMaxStep ) {
                nextAccelerationTickCounter += delta;
                if ( nextAccelerationTickCounter > nextAccelerationTickTime ) {
                    nextAccelerationTickCounter = 0;
                    accelerationStep += accelerationStepTick;
                    
                }
            }
            running = true;
        } else {
            accelerationStep = 0;
            nextAccelerationTickCounter = 0;
            running = false;
        }
        
        double currentSpeed = walkSpeed + maxAcceleration * ( accelerationStep / accelerationMaxStep );
        
        if ( accelerationStep > 5 ) {
            runAnimationRight.setTimeToNextFrame( 0.03 );
            runAnimationLeft.setTimeToNextFrame( 0.03 );
        } else {
            runAnimationRight.setTimeToNextFrame( 0.06 );
            runAnimationLeft.setTimeToNextFrame( 0.06 );
        }
        
        pushing = false;
        
        if ( e.isKeyDown( EngineFrame.KEY_LEFT ) ) {
            
            if ( lookingState == State.LOOKING_RIGHT ) {
                accelerationStep = 0;
            }
            
            vel.x = -currentSpeed;
            lookingState = State.LOOKING_LEFT;
            xState = State.MOVING;
            
        } else if ( e.isKeyDown( EngineFrame.KEY_RIGHT ) ) {
            
            if ( lookingState == State.LOOKING_LEFT ) {
                accelerationStep = 0;
            }
            
            vel.x = currentSpeed;
            lookingState = State.LOOKING_RIGHT;
            xState = State.MOVING;
            
        } else {
            vel.x = 0;
            accelerationStep = 0;
            xState = State.IDLE;
        }
        
        //resolveCollisionTiles( tiles );
        resolveCollisionQuadtree( quadtree );
        
        if ( e.isKeyPressed( EngineFrame.KEY_SPACE ) && remainingJumps > 0 ) {
            vel.y = jumpSpeed;
            remainingJumps--;
            jumpAnimationRight.reset();
            jumpAnimationLeft.reset();
            if ( remainingJumps == 0 ) {
                doubleJumpPos.x = pos.x;
                doubleJumpPos.y = pos.y;
                doubleJumpDustAnimation.reset();
            }
        }
        
        if ( vel.y < 0 ) {
            yState = State.JUMPING;
            pushing = false;
        } else if ( vel.y > 0 ) {
            yState = State.FALLING;
            pushing = false;
        } else {
            yState = State.ON_GROUND;
        }
        
        vel.y += GameWorld.GRAVITY;
        
        if ( vel.y > maxFallSpeed ) {
            vel.y = maxFallSpeed;
        }
        
        idleAnimationRight.update( delta );
        idleAnimationLeft.update( delta );
        walkAnimationRight.update( delta );
        walkAnimationLeft.update( delta );
        runAnimationRight.update( delta );
        runAnimationLeft.update( delta );
        dustAnimationRight.update( delta );
        dustAnimationLeft.update( delta );
        pushAnimationRight.update( delta );
        pushAnimationLeft.update( delta );
        
        if ( yState != State.ON_GROUND ) {
            jumpAnimationRight.update( delta );
            jumpAnimationLeft.update( delta );
            if ( remainingJumps == 0 ) {
                doubleJumpDustAnimation.update( delta );
            }
        }
        
        prevPos.x = pos.x;
        prevPos.y = pos.y;
        aabb.moveTo( pos.x, pos.y );
        
        updateCollisionProbes();
        
    }
    
    public void draw( EngineFrame e ) {
        
        //e.fillRectangle( pos, dim, color );
        //e.drawRectangle( pos, dim, EngineFrame.BLACK );
        
        if ( remainingJumps == 0 && doubleJumpDustAnimation.getState() != AnimationExecutionState.FINISHED ) {
            doubleJumpDustAnimation.getCurrentFrame().draw( e, doubleJumpPos.x, doubleJumpPos.y );
        }
        
        if ( lookingState == State.LOOKING_RIGHT ) {
            if ( yState == State.ON_GROUND ) {
                if ( xState == State.MOVING ) {
                    if ( pushing ) {
                        pushAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
                    } else if ( running ) {
                        if ( accelerationStep > 5 ) {
                            dustAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
                        }
                        runAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
                    } else {
                        walkAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
                    }
                } else {
                    idleAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
                }
            } else {
                jumpAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
            }
        } else {
            if ( yState == State.ON_GROUND ) {
                if ( xState == State.MOVING ) {
                    if ( pushing ) {
                        pushAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
                    } else if ( running ) {
                        if ( accelerationStep > 5 ) {
                            dustAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
                        }
                        runAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
                    } else {
                        walkAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
                    }
                } else {
                    idleAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
                }
            } else {
                jumpAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
            }
        }
        
        //drawCollisionProbes( e );
        
    }
    
    private void drawCollisionProbes( EngineFrame e ) {
        
        e.fillRectangle( cpLeft, lookingState == State.LOOKING_LEFT ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpRight, lookingState == State.LOOKING_RIGHT ? EngineFrame.GREEN : EngineFrame.RED );
        e.fillRectangle( cpUp, yState == State.JUMPING ? EngineFrame.GREEN : EngineFrame.RED );
        if ( yState == State.ON_GROUND ) {
            e.fillRectangle( cpDown, EngineFrame.BLUE );
        } else {
            e.fillRectangle( cpDown, yState == State.FALLING ? EngineFrame.GREEN : EngineFrame.RED );
        }
        
    }
    
    public void updateCollisionProbes() {
        
        cpLeft.x = pos.x + cpLeftAdjust;
        cpLeft.y = pos.y + dim.y / 2 - cpLeft.height / 2;
        cpRight.x = pos.x + dim.x - cpRight.width + cpRightAdjust;
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
                        //if ( a.active && b.active ) {
                            if ( a.referencedObject instanceof Hero ) {
                                if ( b.referencedObject instanceof Tile t ) {
                                    resolveCollisionTile( t );
                                } else {
                                    break;
                                }
                            }
                        //}
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
                remainingJumps = 2;
                break;
            case LEFT:
                pos.x = tile.getPos().x + tile.getDim().x - cpLeftAdjust;
                pushing = true;
                accelerationStep = 0;
                break;
            case RIGHT:
                pos.x = tile.getPos().x - dim.x - cpRightAdjust;
                pushing = true;
                accelerationStep = 0;
                break;
            case UP:
                vel.y = 0;
                pos.y = tile.getPos().y + tile.getDim().y;
                break;
        }
        
        updateCollisionProbes();
        
    }
    
    /*public void resolveCollisionTiles( List<Tile> tiles ) {
        
        for ( Tile tile : tiles ) {
            CollisionType c = checkCollisionTile( tile );
            switch ( c ) {
                case DOWN:
                    pos.y = tile.getPos().y - dim.y;
                    vel.y = 0;
                    remainingJumps = 2;
                    break;
                case LEFT:
                    pos.x = tile.getPos().x + tile.getDim().x - cpLeftAdjust;
                    pushing = true;
                    accelerationStep = 0;
                    break;
                case RIGHT:
                    pos.x = tile.getPos().x - dim.x - cpRightAdjust;
                    pushing = true;
                    accelerationStep = 0;
                    break;
                case UP:
                    vel.y = 0;
                    pos.y = tile.getPos().y + tile.getDim().y;
                    break;
            }
            updateCollisionProbes();
        }
        
    }*/
    
    private void loadImagesAndCreateAnimations() {
        
        this.idleImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/idle.png" );
        this.walkImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/walk.png" );
        this.runImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/run.png" );
        this.dustImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/dust.png" );
        this.pushImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/push.png" );
        this.jumpImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/jump.png" );
        this.doubleJumpDustImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/doubleJumpDust.png" );
        
        Image[] images = {
            this.idleImageMap,
            this.walkImageMap,
            this.runImageMap,
            this.pushImageMap,
            this.jumpImageMap
        };
        
        for ( Image image : images ) {
            Utils.replaceHeroImageColors( image );
        }
        
        this.idleAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap, dim.x, dim.y ),
            true
        );
        this.idleAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
        this.walkAnimationRight = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap, dim.x, dim.y ),
            true
        );
        this.walkAnimationLeft = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
        this.runAnimationRight = new FrameByFrameAnimation<>( 
            0.06,
            AnimationUtils.getSpriteMapAnimationFrameList( runImageMap, dim.x, dim.y ),
            true
        );
        
        this.runAnimationLeft = new FrameByFrameAnimation<>( 
            0.06,
            AnimationUtils.getSpriteMapAnimationFrameList( runImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
        this.dustAnimationRight = new FrameByFrameAnimation<>( 
            0.03,
            AnimationUtils.getSpriteMapAnimationFrameList( dustImageMap, dim.x, dim.y ),
            true
        );
        this.dustAnimationLeft = new FrameByFrameAnimation<>( 
            0.03,
            AnimationUtils.getSpriteMapAnimationFrameList( dustImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
        this.pushAnimationRight = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( pushImageMap, dim.x, dim.y ),
            true
        );
        this.pushAnimationLeft = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( pushImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            true
        );
        
        this.jumpAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( jumpImageMap, dim.x, dim.y ),
            false
        );
        this.jumpAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( jumpImageMap.copyFlipHorizontal(), dim.x, dim.y, true ),
            false
        );
        
        this.doubleJumpDustAnimation = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( doubleJumpDustImageMap, dim.x, dim.y ),
            false
        );
        this.doubleJumpDustAnimation.setStopAtLastFrameWhenFinished( false );
        
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

    public int getRemainingJumps() {
        return remainingJumps;
    }
    
    public boolean isMoving() {
        return xState == State.MOVING;
    }

    public AABB getAABB() {
        return aabb;
    }
    
}
