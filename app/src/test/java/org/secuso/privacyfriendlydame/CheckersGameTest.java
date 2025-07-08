package org.secuso.privacyfriendlydame;

import org.junit.Before;
import org.junit.Test;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.GameRules;
import org.secuso.privacyfriendlydame.game.GameType;
import org.secuso.privacyfriendlydame.game.Move;
import org.secuso.privacyfriendlydame.game.Piece;
import org.secuso.privacyfriendlydame.game.Position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CheckersGameTest {

    CheckersGame game;

    @Before
    public void init() {
        this.game = new CheckersGame(GameType.Human,0, new GameRules(false, true));
    }

    @Test
    public void checkLegalMoves(){
        assertEquals(game.whoseTurn() , CheckersGame.WHITE);
        Move[] moves = {
                new Move(new Position(0,5)).add(new Position(1,4)),
                new Move(new Position(2,5)).add(new Position(1,4)),
                new Move(new Position(2,5)).add(new Position(3,4)),
                new Move(new Position(4,5)).add(new Position(3,4)),
                new Move(new Position(4,5)).add(new Position(5,4)),
                new Move(new Position(6,5)).add(new Position(5,4)),
                new Move(new Position(6,5)).add(new Position(7,4)),
        };
        Move[] ingame = game.getMoves();
        for(int i = 0; i < moves.length; i++){
            final int _i = i;
            assertTrue(Arrays.stream(moves).filter((move) -> move.equals(ingame[_i])).findAny().isPresent());
        }
    }

    @Test
    public void checkMove(){
        Piece piece1=game.getBoard().getPiece(1, 2);
        game.makeMove(new Move(new Position(1,2)).add(new Position(2,3)));
        Piece piece2=game.getBoard().getPiece(2, 3);
        assertEquals(piece1, piece2);
    }

    @Test
    public void checkCapture(){
        assertEquals(game.whoseTurn() , CheckersGame.WHITE);
        game.makeMove(new Move(new Position(1,2)).add(new Position(2,3)));
        assertEquals(game.whoseTurn() , CheckersGame.BLACK);
        game.makeMove(new Move(new Position(4,5)).add(new Position(3,4)));
        assertEquals(game.whoseTurn() , CheckersGame.WHITE);
        assertEquals(1, game.getMoves().length);
        Move move = game.getMoves()[0];
        assertEquals(1, move.capturePositions.size());
        Piece willBeCaptured = game.getBoard().getPiece(3,4);
        assertNotNull(willBeCaptured);
        game.makeMove(move);
        assertNull(game.getBoard().getPiece(3,4));
    }
}