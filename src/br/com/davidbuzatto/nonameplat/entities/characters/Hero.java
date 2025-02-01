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
import br.com.davidbuzatto.nonameplat.entities.items.Coin;
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
    
    public static final double WALK_SPEED = 300;
    public static final double MAX_ACCELERATION = 200;
    public static final double JUMP_SPEED = -450;
    public static final double MAX_FALL_SPEED = 600;
    
    private static final double CP_WIDTH_SML = 10;
    private static final double CP_WIDTH_BIG = 20;
    
    private Vector2 pos;
    private Vector2 prevPos;
    private Vector2 dim;
    private Vector2 sliceDim;
    private Vector2 posAdjust;
    private Vector2 vel;
    private Color color;
    
    private int hp;
    private int maxHp;
    private int lives;
    private int coins;
    
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
        this.dim = new Vector2( 40, 56 );
        this.sliceDim = new Vector2( 64, 64 );
        this.posAdjust = new Vector2( this.sliceDim.x - this.dim.x - 15, this.sliceDim.y - this.dim.y );
        this.vel = new Vector2();
        this.color = color;
        
        this.hp = 5;
        this.maxHp = 5;
        this.lives = 3;
        this.coins = 0;
        
        this.aabb = new AABB( pos.x, pos.y, pos.x + dim.x, pos.y + dim.y, AABB.Type.DYNAMIC, this );
        
        this.remainingJumps = 2;
        this.doubleJumpPos = new Vector2();
        
        this.cpLeft = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpRight = new Rectangle( 0, 0, CP_WIDTH_SML, CP_WIDTH_BIG );
        this.cpUp = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        this.cpDown = new Rectangle( 0, 0, CP_WIDTH_BIG, CP_WIDTH_SML );
        
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
        
        if ( e.isKeyDown( EngineFrame.KEY_CONTROL ) || e.isGamepadButtonDown( EngineFrame.GAMEPAD_1, EngineFrame.GAMEPAD_BUTTON_RIGHT_FACE_LEFT ) ) {
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
        
        double currentSpeed = WALK_SPEED + MAX_ACCELERATION * ( accelerationStep / accelerationMaxStep );
        
        if ( accelerationStep > 5 ) {
            runAnimationRight.setTimeToNextFrame( 0.03 );
            runAnimationLeft.setTimeToNextFrame( 0.03 );
        } else {
            runAnimationRight.setTimeToNextFrame( 0.06 );
            runAnimationLeft.setTimeToNextFrame( 0.06 );
        }
        
        pushing = false;
        
        if ( e.isKeyDown( EngineFrame.KEY_LEFT ) || e.isGamepadButtonDown( EngineFrame.GAMEPAD_1, EngineFrame.GAMEPAD_BUTTON_LEFT_FACE_LEFT ) ) {
            
            if ( lookingState == State.LOOKING_RIGHT ) {
                accelerationStep = 0;
            }
            
            vel.x = -currentSpeed;
            lookingState = State.LOOKING_LEFT;
            xState = State.MOVING;
            
        } else if ( e.isKeyDown( EngineFrame.KEY_RIGHT ) || e.isGamepadButtonDown( EngineFrame.GAMEPAD_1, EngineFrame.GAMEPAD_BUTTON_LEFT_FACE_RIGHT ) ) {
            
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
        
        if ( ( e.isKeyPressed( EngineFrame.KEY_SPACE ) || e.isGamepadButtonPressed( EngineFrame.GAMEPAD_1, EngineFrame.GAMEPAD_BUTTON_RIGHT_FACE_DOWN ) ) && remainingJumps > 0 ) {
            jump();
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
        
        vel.y += GameWorld.GRAVITY * delta;
        
        if ( vel.y > MAX_FALL_SPEED ) {
            vel.y = MAX_FALL_SPEED;
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
        
        if ( remainingJumps == 0 && doubleJumpDustAnimation.getState() != AnimationExecutionState.FINISHED ) {
            doubleJumpDustAnimation.getCurrentFrame().draw( e, doubleJumpPos.x, doubleJumpPos.y );
        }
        
        if ( lookingState == State.LOOKING_RIGHT ) {
            if ( yState == State.ON_GROUND ) {
                if ( xState == State.MOVING ) {
                    if ( pushing ) {
                        pushAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    } else if ( running ) {
                        if ( accelerationStep > 5 ) {
                            dustAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                        }
                        runAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    } else {
                        walkAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    }
                } else {
                    idleAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                }
            } else {
                jumpAnimationRight.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
            }
        } else {
            if ( yState == State.ON_GROUND ) {
                if ( xState == State.MOVING ) {
                    if ( pushing ) {
                        pushAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    } else if ( running ) {
                        if ( accelerationStep > 5 ) {
                            dustAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                        }
                        runAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    } else {
                        walkAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                    }
                } else {
                    idleAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
                }
            } else {
                jumpAnimationLeft.getCurrentFrame().draw( e, pos.x - posAdjust.x, pos.y - posAdjust.y );
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
    
    public CollisionType checkCollisionEnemy( BaseEnemy enemy ) {
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpDown, enemy.getAABB() ) ) {
            return CollisionType.DOWN;
        }
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpLeft, enemy.getAABB() ) ) {
            return CollisionType.LEFT;
        }
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpRight, enemy.getAABB() ) ) {
            return CollisionType.RIGHT;
        }
        
        if ( CollisionUtils.checkCollisionRectangleAABB( cpUp, enemy.getAABB() ) ) {
            return CollisionType.UP;
        }
        
        return CollisionType.NONE;
        
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
                        if ( a.referencedObject instanceof Hero ) {
                            if ( b.referencedObject instanceof Tile t ) {
                                resolveCollisionTile( t );
                            } else if ( b.referencedObject instanceof BaseEnemy e ) {
                                resolveCollisionEnemy( e );
                            } else if ( b.referencedObject instanceof Coin c ) {
                                resolveCollisionCoin( c );
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
    
    public void resolveCollisionCoin( Coin coin ) {
        
        if ( coin.isActive() ) {
            if ( CollisionUtils.checkCollisionAABBs( aabb, coin.getAABB() ) ) {
                coin.collect();
                coins++;
            }
        }
        
    }
    
    public void resolveCollisionEnemy( BaseEnemy enemy ) {
        
        if ( enemy.isAlive() ) {
            
            CollisionType c = checkCollisionEnemy( enemy );

            switch ( c ) {
                case DOWN:
                    pos.y = enemy.getPos().y - dim.y;
                    vel.y = 0;
                    remainingJumps = 2;
                    jump();
                    enemy.prepareToDie();
                    break;
                case LEFT:
                    pos.x = enemy.getPos().x + enemy.getDim().x;
                    pushing = true;
                    accelerationStep = 0;
                    hp--;
                    enemy.prepareToDie();
                    break;
                case RIGHT:
                    pos.x = enemy.getPos().x - dim.x;
                    pushing = true;
                    accelerationStep = 0;
                    hp--;
                    enemy.prepareToDie();
                    break;
                case UP:
                    vel.y = 0;
                    pos.y = enemy.getPos().y + enemy.getDim().y;
                    break;
            }

            updateCollisionProbes();
            
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
                pos.x = tile.getPos().x + tile.getDim().x;
                pushing = true;
                accelerationStep = 0;
                break;
            case RIGHT:
                pos.x = tile.getPos().x - dim.x;
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
    
    private void jump() {
        vel.y = JUMP_SPEED;
        remainingJumps--;
        jumpAnimationRight.reset();
        jumpAnimationLeft.reset();
        if ( remainingJumps == 0 ) {
            doubleJumpPos.x = pos.x;
            doubleJumpPos.y = pos.y;
            doubleJumpDustAnimation.reset();
        }
    }
    
    private void loadImagesAndCreateAnimations() {
        
        this.idleImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/idle.png" );
        walkImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/walk.png" );
        runImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/run.png" );
        dustImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/dust.png" );
        pushImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/push.png" );
        jumpImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/jump.png" );
        doubleJumpDustImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/doubleJumpDust.png" );
        
        Image[] images = {
            idleImageMap,
            walkImageMap,
            runImageMap,
            pushImageMap,
            jumpImageMap
        };
        
        for ( Image image : images ) {
            Utils.replaceHeroImageColors( image );
        }
        
        idleAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        idleAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            true
        );
        
        walkAnimationRight = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        walkAnimationLeft = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            true
        );
        
        runAnimationRight = new FrameByFrameAnimation<>( 
            0.06,
            AnimationUtils.getSpriteMapAnimationFrameList( runImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        
        runAnimationLeft = new FrameByFrameAnimation<>( 
            0.06,
            AnimationUtils.getSpriteMapAnimationFrameList( runImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            true
        );
        
        dustAnimationRight = new FrameByFrameAnimation<>( 
            0.03,
            AnimationUtils.getSpriteMapAnimationFrameList( dustImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        dustAnimationLeft = new FrameByFrameAnimation<>( 
            0.03,
            AnimationUtils.getSpriteMapAnimationFrameList( dustImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            true
        );
        
        pushAnimationRight = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( pushImageMap, sliceDim.x, sliceDim.y ),
            true
        );
        pushAnimationLeft = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( pushImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            true
        );
        
        jumpAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( jumpImageMap, sliceDim.x, sliceDim.y ),
            false
        );
        jumpAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( jumpImageMap.copyFlipHorizontal(), sliceDim.x, sliceDim.y, true ),
            false
        );
        
        doubleJumpDustAnimation = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( doubleJumpDustImageMap, sliceDim.x, sliceDim.y ),
            false
        );
        doubleJumpDustAnimation.setStopAtLastFrameWhenFinished( false );
        
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

    public int getHp() {
        return hp;
    }

    public void setHp( int hp ) {
        this.hp = hp;
    }

    public int getLives() {
        return lives;
    }

    public void setLives( int lives ) {
        this.lives = lives;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins( int coins ) {
        this.coins = coins;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp( int maxHp ) {
        this.maxHp = maxHp;
    }
    
}
