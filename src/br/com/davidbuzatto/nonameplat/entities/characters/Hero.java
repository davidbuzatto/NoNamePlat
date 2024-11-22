package br.com.davidbuzatto.nonameplat.entities.characters;

import br.com.davidbuzatto.jsge.animation.AnimationExecutionState;
import br.com.davidbuzatto.jsge.animation.AnimationUtils;
import br.com.davidbuzatto.jsge.animation.frame.FrameByFrameAnimation;
import br.com.davidbuzatto.jsge.animation.frame.SpriteMapAnimationFrame;
import br.com.davidbuzatto.jsge.collision.CollisionUtils;
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
    private double runSpeed;
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
    
    public Hero( Vector2 pos, Color color ) {
        
        this.pos = pos;
        this.prevPos = new Vector2();
        this.dim = new Vector2( GameWorld.BASE_WIDTH, GameWorld.BASE_WIDTH );
        this.vel = new Vector2();
        this.color = color;
        this.walkSpeed = 300;
        this.runSpeed = 500;
        this.jumpSpeed = -450;
        this.maxFallSpeed = 600;
        
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
        
        loadImagesAndCreateAnimations();
        
    }
    
    public void update( EngineFrame e, double worldWidth, double worldHeight, List<Tile> tiles, double delta ) {
        
        pos.x += vel.x * delta;
        pos.y += vel.y * delta;
        
        double currentSpeed;
        if ( e.isKeyDown( EngineFrame.KEY_CONTROL ) ) {
            currentSpeed = runSpeed;
            running = true;
        } else {
            currentSpeed = walkSpeed;
            running = false;
        }
        
        pushing = false;
        
        if ( e.isKeyDown( EngineFrame.KEY_LEFT ) ) {
            vel.x = -currentSpeed;
            lookingState = State.LOOKING_LEFT;
            xState = State.MOVING;
        } else if ( e.isKeyDown( EngineFrame.KEY_RIGHT ) ) {
            vel.x = currentSpeed;
            lookingState = State.LOOKING_RIGHT;
            xState = State.MOVING;
        } else {
            vel.x = 0;
            xState = State.IDLE;
        }
        
        resolveCollisionTiles( tiles );
        
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
                        dustAnimationRight.getCurrentFrame().draw( e, pos.x, pos.y );
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
                        dustAnimationLeft.getCurrentFrame().draw( e, pos.x, pos.y );
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
                    vel.y = 0;
                    remainingJumps = 2;
                    break;
                case LEFT:
                    pos.x = tile.getPos().x + tile.getDim().x - cpLeftAdjust;
                    pushing = true;
                    break;
                case RIGHT:
                    pos.x = tile.getPos().x - dim.x - cpRightAdjust;
                    pushing = true;
                    break;
                case UP:
                    vel.y = 0;
                    pos.y = tile.getPos().y + tile.getDim().y;
                    break;
            }
            updateCollisionProbes();
        }
        
    }
    
    private void loadImagesAndCreateAnimations() {
        
        Color[] fromColor = new Color[]{
            new Color( 244, 137, 246 ),
            new Color( 216, 64, 251 ),
            new Color( 120, 11, 247 )
        };
        
        Color[] toColor = new Color[]{
            new Color( 106, 156, 246 ),
            new Color( 15, 94, 238 ),
            new Color( 9, 56, 147 )
        };
        
        this.idleImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/idle_4.png" );
        this.walkImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/walk_6.png" );
        this.runImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/run_6.png" );
        this.dustImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/dust_6.png" );
        this.pushImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/push_6.png" );
        this.jumpImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/jump_8.png" );
        this.doubleJumpDustImageMap = ImageUtils.loadImage( "resources/images/sprites/hero/doubleJumpDust_5.png" );
        
        Image[] images = {
            this.idleImageMap,
            this.walkImageMap,
            this.runImageMap,
            this.pushImageMap,
            this.jumpImageMap
        };
        
        for ( int i = 0; i < fromColor.length; i++ ) {
            for ( Image img : images ) {
                img.colorReplace( fromColor[i], toColor[i] );
            }
        }
        
        this.idleAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap, 4, dim.x, dim.y ),
            true
        );
        this.idleAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( idleImageMap.copyFlipHorizontal(), 4, dim.x, dim.y, true ),
            true
        );
        
        this.walkAnimationRight = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap, 6, dim.x, dim.y ),
            true
        );
        this.walkAnimationLeft = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( walkImageMap.copyFlipHorizontal(), 6, dim.x, dim.y, true ),
            true
        );
        
        this.runAnimationRight = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( runImageMap, 6, dim.x, dim.y ),
            true
        );
        this.runAnimationLeft = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( runImageMap.copyFlipHorizontal(), 6, dim.x, dim.y, true ),
            true
        );
        
        this.dustAnimationRight = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( dustImageMap, 6, dim.x, dim.y ),
            true
        );
        this.dustAnimationLeft = new FrameByFrameAnimation<>( 
            0.05,
            AnimationUtils.getSpriteMapAnimationFrameList( dustImageMap.copyFlipHorizontal(), 6, dim.x, dim.y, true ),
            true
        );
        
        this.pushAnimationRight = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( pushImageMap, 6, dim.x, dim.y ),
            true
        );
        this.pushAnimationLeft = new FrameByFrameAnimation<>( 
            0.07,
            AnimationUtils.getSpriteMapAnimationFrameList( pushImageMap.copyFlipHorizontal(), 6, dim.x, dim.y, true ),
            true
        );
        
        this.jumpAnimationRight = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( jumpImageMap, 8, dim.x, dim.y ),
            false
        );
        this.jumpAnimationLeft = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( jumpImageMap.copyFlipHorizontal(), 8, dim.x, dim.y, true ),
            false
        );
        
        this.doubleJumpDustAnimation = new FrameByFrameAnimation<>( 
            0.1,
            AnimationUtils.getSpriteMapAnimationFrameList( doubleJumpDustImageMap, 5, dim.x, dim.y ),
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
    
}
