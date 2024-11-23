package br.com.davidbuzatto.nonameplat;

import br.com.davidbuzatto.jsge.collision.CollisionUtils;
import br.com.davidbuzatto.jsge.collision.aabb.AABB;
import br.com.davidbuzatto.jsge.collision.aabb.AABBQuadtree;
import br.com.davidbuzatto.jsge.collision.aabb.AABBQuadtreeNode;
import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.geom.Rectangle;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.entities.characters.Hero;
import br.com.davidbuzatto.nonameplat.entities.tiles.Tile;
import br.com.davidbuzatto.nonameplat.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Game World.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class GameWorld extends EngineFrame {
    
    public static final double GRAVITY = 20;
    public static final double BASE_WIDTH = 64;
    
    private int lines;
    private int columns;
    private double worldWidth;
    private double worldHeight;
    private double halfScreenWidth;
    private double halfScreenHeight;
    
    private Hero hero;
    private Camera2D camera;
    
    private Map<Character, Image> tileSkins;
    private List<Tile> tiles;
    
    private Color backgroundColor;
    private ParallaxEngine parallaxEngine;
    
    private Color aabbOverlapColor;
    
    private int qtWidth;
    private int qtHeight;
    private int maxTreeDepth;
    
    private List<AABB> aabbs;
    private AABBQuadtree quadtree;
    private List<Rectangle> overlaps;
    
    // statistics
    private boolean showStatistics;
    
    private Image heroIcon;
    
    public GameWorld() {
        // 896 = 14 columns
        // 512 = 8 lines
        super ( 896, 512, "No Name Platformer", 60, true );
    }
    
    @Override
    public void create() {
        
        heroIcon = loadImage( "resources/images/sprites/hero/hero.png" );
        Utils.replaceHeroImageColors( heroIcon );
        setWindowIcon( heroIcon );
        
        setDefaultFontSize( 20 );
        
        halfScreenWidth = getScreenWidth() / 2;
        halfScreenHeight = getScreenHeight() / 2;
        
        hero = new Hero( 
            new Vector2(),
            BLUE
        );
        
        camera = new Camera2D( 
            new Vector2(),
            new Vector2( halfScreenWidth, halfScreenHeight ),
            0,
            1.0
        );
        
        loadTileSkins();
        
        processMapData( 
            """
            G                                            E
            G                                            E
            G                                            E
            G                                            E
            G                                            E
            G                                            E
            G           IJJJJJK                          E
            G          L                                 E
            G         L                                  E
            G        L                                   E
            G    p                                       E
            MBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBN
            FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
            """
        );
        
        backgroundColor = new Color( 44, 154, 208 );
        parallaxEngine = new ParallaxEngine( worldWidth, getScreenWidth(), getScreenHeight(), 0.1 );
        
        qtWidth = (int) worldWidth;
        qtHeight = qtWidth;
        maxTreeDepth = 5;
        
        aabbOverlapColor = ColorUtils.fade( BLUE, 0.7 );
        
        initAABBs();
        quadtree = new AABBQuadtree( aabbs, qtWidth, qtHeight, maxTreeDepth );
        overlaps = new CopyOnWriteArrayList<>();
        
        showStatistics = true;
        
    }
    
    @Override
    public void update( double delta ) {
        
        if ( isKeyPressed( KEY_F1 ) ) {
            showStatistics = !showStatistics;
        }
        
        if ( isKeyPressed( KEY_R ) ) {
            hero.getAABB().active = !hero.getAABB().active;
        }
        
        hero.update( this, worldWidth, worldHeight, tiles, quadtree, delta );
        quadtree.update();
        updateCamera();
        
    }
    
    @Override
    public void draw() {
        
        clearBackground( backgroundColor );
        parallaxEngine.draw( this, hero );
        
        beginMode2D( camera );
        drawTiles();
        hero.draw( this );
        endMode2D();
        
        if ( showStatistics ) {
            drawStatistics( 20, 20 );
        }
    
    }
    
    private void drawTiles() {
        for ( Tile t : tiles ) {
            t.draw( this );
        }
    }
    
    private void drawStatistics( int x, int y ) {
        
        fillRectangle( x - 10, y - 10, 440, 290, ColorUtils.fade( WHITE, 0.5 ) );
        drawFPS( x, y );
        drawText( "    pos: " + hero.getPos().toString(), x, y += 20, BLACK );
        drawText( "prevPos: " + hero.getPos().toString(), x, y += 20, BLACK );
        drawText( "    vel: " + hero.getVel().toString(), x, y += 20, BLACK );
        drawText( "r jumps: " + hero.getRemainingJumps(), x, y += 20, BLACK );
        
        drawText( "Quadtree:", x, y += 20, BLACK );
        drawQuadTree( x, y += 20, 0.05 );
        
    }
    
    private void updateCamera() {
        
        if ( hero.getPos().x <= halfScreenWidth ) {
            camera.target.x = halfScreenWidth;
        } else if ( hero.getPos().x >= worldWidth - halfScreenWidth ) {
            camera.target.x = worldWidth - halfScreenWidth;
        } else {
            camera.target.x = hero.getPos().x;
        }
        
        if ( hero.getPos().y <= halfScreenHeight ) {
            camera.target.y = halfScreenHeight;
        } else if ( hero.getPos().y >= worldHeight - halfScreenHeight ) {
            camera.target.y = worldHeight - halfScreenHeight;
        } else {
            camera.target.y = hero.getPos().y;
        }
        
    }
    
    private void loadTileSkins() {
        
        String tilePath = "resources/images/tiles/tile%c.png";
        
        tileSkins = new HashMap<>();
        for ( char c = 'A'; c <= 'N'; c++ ) {
            tileSkins.put( c, ImageUtils.loadImage( String.format( tilePath, c ) ) );
        }
        
    }
    
    private void processMapData( String mapData ) {
        
        int currentLine = 0;
        int currentColumn = 0;
        int maxColumn = 0;
        
        tiles = new ArrayList<>();
        
        for ( char c : mapData.toCharArray() ) {
            if ( c == '\n' ) {
                currentLine++;
                currentColumn = 0;
            } else {
                
                if ( c >= 'A' && c <= 'N' ) {
                    tiles.add( 
                        new Tile( 
                            new Vector2( currentColumn * BASE_WIDTH, currentLine * BASE_WIDTH ), 
                            ORANGE,
                            tileSkins.get( c )
                        )
                    );
                } else {
                    
                    switch ( c ) {
                        case 'p':
                            hero.getPos().x = currentColumn * BASE_WIDTH;
                            hero.getPos().y = currentLine * BASE_WIDTH;
                            hero.updateCollisionProbes();
                            break;
                        default:
                            if ( c != ' ' ) {
                                tiles.add( new Tile( new Vector2( currentColumn * BASE_WIDTH, currentLine * BASE_WIDTH ) ) );
                            }
                            break;
                    }
                    
                }
                
                currentColumn++;
                
                if ( maxColumn < currentColumn ) {
                    maxColumn = currentColumn;
                }
                
            }
        }
        
        lines = currentLine;
        columns = maxColumn;
        worldWidth = columns * BASE_WIDTH;
        worldHeight = lines * BASE_WIDTH;
        
    }
    
    private void initAABBs() {
        
        aabbs = new ArrayList<>();
        aabbs.add( hero.getAABB() );
        
        for ( Tile t : tiles ) {
            aabbs.add( t.getAABB() );
        }
        
    }
    
    private void calculateOverlaps( AABBQuadtreeNode node, double x, double y, double scale ) {
        
        if ( node.depth < quadtree.getMaxDepth() ) {
            
            int size = node.aabbs.size();
            
            for ( int i = 0; i < size; i++ ) {
                for ( int j = i+1; j < size; j++ ) {
                    try {
                        AABB a = node.aabbs.get( i );
                        AABB b = node.aabbs.get( j );
                        if ( a.type != AABB.Type.STATIC || b.type != AABB.Type.STATIC ) {
                            Rectangle ra = new Rectangle( a.x1 * scale, a.y1 * scale, ( a.x2 - a.x1 ) * scale, ( a.y2 - a.y1 ) * scale );
                            Rectangle rb = new Rectangle( b.x1 * scale, b.y1 * scale, ( b.x2 - b.x1 ) * scale, ( b.y2 - b.y1 ) * scale );
                            if ( CollisionUtils.checkCollisionRectangles( ra, rb ) ) {
                                Rectangle ri = CollisionUtils.getCollisionRectangle( ra, rb );
                                ri.x += x;
                                ri.y += y;
                                overlaps.add( ri );
                            }
                        }
                    } catch ( IndexOutOfBoundsException | NullPointerException exc ) {
                    }
                }
            }
            
            calculateOverlaps( node.nw, x, y, scale );
            calculateOverlaps( node.ne, x, y, scale );
            calculateOverlaps( node.sw, x, y, scale );
            calculateOverlaps( node.se, x, y, scale );
            
        }
        
    }
    
    private void drawQuadTree( double x, double y, double scale ) {
        
        overlaps.clear();
        calculateOverlaps( quadtree.getRoot(), x, y, scale );
        
        quadtree.draw( this, x, y, scale );
        
        for ( Rectangle r : overlaps ) {
            r.fill( this, aabbOverlapColor );
        }
        
    }
    
    public static void main( String[] args ) {
        new GameWorld();
    }
    
}
