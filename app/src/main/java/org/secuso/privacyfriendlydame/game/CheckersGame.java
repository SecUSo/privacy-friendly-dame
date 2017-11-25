package org.secuso.privacyfriendlydame.game;

import org.secuso.privacyfriendlydame.game.Move;

import java.util.ArrayList;

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
