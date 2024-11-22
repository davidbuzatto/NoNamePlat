package br.com.davidbuzatto.nonameplat;

import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import br.com.davidbuzatto.nonameplat.entities.characters.Hero;
import br.com.davidbuzatto.nonameplat.entities.tiles.Tile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private double width;
    private double height;
    private double halfScreenWidth;
    private double halfScreenHeight;
    
    private Hero hero;
    private Camera2D camera;
    
    private Map<Character, Image> tileSkins;
    private List<Tile> tiles;
    
    public GameWorld() {
        // 896 = 14 columns
        // 512 = 8 lines
        super ( 896, 512, "No Name Platformer", 60, true );
    }
    
    @Override
    public void create() {
        
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
            G                       E
            G                       E
            G                       E
            G                       E
            G                       E
            G                       E
            G           IJJJJJK     E
            G          L            E
            G         L             E
            G        L              E
            G    p                  E
            MBBBBBBBBBBBBBBBBBBBBBBBN
            FFFFFFFFFFFFFFFFFFFFFFFFF
            """
        );
        
    }
    
    @Override
    public void update( double delta ) {
        
        hero.update( this, width, height, tiles, delta );
        
        updateCamera();
        
    }
    
    @Override
    public void draw() {
        
        clearBackground( SKYBLUE );
        beginMode2D( camera );
        
        //drawTestBackground();
        drawTiles();
        
        hero.draw( this );
        
        endMode2D();
        
        drawStatistics( 20, 20 );
    
    }
    
    private void drawTestBackground() {
        
        for ( int i = 0; i < lines; i++) {
            for ( int j = 0; j < columns; j++ ) {
                fillRectangle( 
                    j * BASE_WIDTH, 
                    i * BASE_WIDTH, 
                    BASE_WIDTH, 
                    BASE_WIDTH, 
                    i % 2 == 0 ?
                        j % 2 == 0 ?
                            GRAY : DARKGRAY
                        :
                        j % 2 == 0 ?
                            DARKGRAY : GRAY
                );
            }
        }
        
        for ( int i = 0; i <= lines; i++) {
            drawLine( 0, i * BASE_WIDTH, width, i * BASE_WIDTH, BLACK );
        }
        
        for ( int i = 0; i <= columns; i++) {
            drawLine( i * BASE_WIDTH, 0, i * BASE_WIDTH, height, BLACK );
        }
        
    }
    
    private void drawTiles() {
        for ( Tile t : tiles ) {
            t.draw( this );
        }
    }
    
    private void drawStatistics( int x, int y ) {
        
        fillRectangle( x - 10, y - 10, 440, 120, ColorUtils.fade( WHITE, 0.5 ) );
        drawFPS( x, y );
        drawText( "    pos: " + hero.getPos().toString(), x, y += 20, BLACK );
        drawText( "prevPos: " + hero.getPos().toString(), x, y += 20, BLACK );
        drawText( "    vel: " + hero.getVel().toString(), x, y += 20, BLACK );
        drawText( "r jumps: " + hero.getRemainingJumps(), x, y += 20, BLACK );
        
    }
    
    private void updateCamera() {
        
        if ( hero.getPos().x <= halfScreenWidth ) {
            camera.target.x = halfScreenWidth;
        } else if ( hero.getPos().x >= width - halfScreenWidth ) {
            camera.target.x = width - halfScreenWidth;
        } else {
            camera.target.x = hero.getPos().x;
        }
        
        if ( hero.getPos().y <= halfScreenHeight ) {
            camera.target.y = halfScreenHeight;
        } else if ( hero.getPos().y >= height - halfScreenHeight ) {
            camera.target.y = height - halfScreenHeight;
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
        width = columns * BASE_WIDTH;
        height = lines * BASE_WIDTH;
        
    }
    
    public static void main( String[] args ) {
        new GameWorld();
    }
    
}
