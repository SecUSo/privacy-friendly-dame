/*
 This file is part of Privacy Friendly Dame.

 Privacy Friendly Dame is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Dame is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlydame.game;

public class CheckersGame {
    public static final int NONE = 0;
    public static final int BLACK = 1;
    public static final int RED = 2;
    public static final int KINGED = 3;

    // checkers game state
    private Board gameBoard;
    private int turn;
    private boolean over;
    private int winner;
    private boolean allowAnyMove;

    // checkers game holds board state and current turn
    public CheckersGame(boolean anyMove) {
        gameBoard = new Board(this);
        turn = CheckersGame.BLACK;
        over = false;
        winner = CheckersGame.NONE;
        allowAnyMove = anyMove;
    }

    public void restart() {
        gameBoard = new Board(this);
        turn = CheckersGame.BLACK;
        over = false;
        winner = CheckersGame.NONE;
    }

    public void setAnyMove(boolean anyMove) {
        allowAnyMove = anyMove;
    }

    // check whose turn it is
    public int whoseTurn() {
        return turn;
    }

    // get the board data
    public Board getBoard() {
        return this.gameBoard;
    }

    public Move getLongestMove(Position start, Position end) {
        Move longest = null;
        Move moveset[] = getMoves();
        for (Move move : moveset) {
            if (move.start().equals(start) && move.end().equals(end)) {
                if (longest == null ||
                        longest.captures.size() < move.captures.size())
                    longest = move;
            }
        }
        return longest;
    }

    public Move[] getMoves() {
        return gameBoard.getMoves(turn, allowAnyMove);
    }

    // make a move
    public void makeMove(Move choice) {
        gameBoard.makeMove(choice);
        advanceTurn();
    }

    // switch turns
    private void advanceTurn() {
        if (turn == CheckersGame.RED) {
            turn = CheckersGame.BLACK;
        } else {
            turn = CheckersGame.RED;
        }
    }
}
