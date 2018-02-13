package org.secuso.privacyfriendlydame;

import org.junit.Before;
import org.junit.Test;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.GameType;
import org.secuso.privacyfriendlydame.game.Move;
import org.secuso.privacyfriendlydame.game.Piece;
import org.secuso.privacyfriendlydame.game.Position;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CheckersGameTest {

    CheckersGame game;

    @Before
    public void init() {
        this.game = new CheckersGame(GameType.Human);
    }

    @Test
    public void checkLegalMoves(){
        assertEquals(game.whoseTurn() , CheckersGame.WHITE);
        Move[] moves = {
                new Move(new Position(1,2)).add(new Position(0,3)),
                new Move(new Position(1,2)).add(new Position(2,3)),
                new Move(new Position(3,2)).add(new Position(2,3)),
                new Move(new Position(3,2)).add(new Position(4,3)),
                new Move(new Position(5,2)).add(new Position(4,3)),
                new Move(new Position(5,2)).add(new Position(6,3)),
                new Move(new Position(7,2)).add(new Position(6,3)),
        };
        Move[] ingame = game.getMoves();
        for(int i = 0; i < moves.length; i++){
            assertTrue(ingame[i].equals(moves[i]));
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