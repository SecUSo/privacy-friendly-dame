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

import android.os.Parcel;
import android.os.Parcelable;

import org.secuso.privacyfriendlydame.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

/**
 * This class models a game of checkers. It contains information about the game board, the current
 * player, the game type and the captured pieces of both players.
 */
public class CheckersGame implements Parcelable, Serializable{
    public int searchDepth;

    static final int NONE = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    static final int KINGED = 3;

    // checkers game state
    private Board gameBoard;
    private int turn;
    private ArrayList<Piece> capturedBlackPieces;
    private ArrayList<Piece> capturedWhitePieces;
    private boolean isFinished;
    private GameType gameType;
    private Stack<Move> moveHistory;
    private Stack<Board> boardHistory;

    private Stack<Integer> turnHistory;

    private Stack<ArrayList<Piece>> capturedBlackPiecesHistory;
    private Stack<ArrayList<Piece>> capturedWhitePiecesHistory;

    // rules
    private GameRules rules;

    // Piece image resource IDs
    private int blackNormalIconId = R.drawable.ic_piece_black;
    private int blackKingIconId = R.drawable.ic_piece_black_queen;
    private int whiteNormalIconId = R.drawable.ic_piece_white;
    private int whiteKingIconId = R.drawable.ic_piece_white_queen;

    /**
     * Default constructor which creates a new game with a default board setup and black as the first player
     */
    public CheckersGame(GameType gameType,int depth, GameRules rules) {

        this.rules = rules;
        gameBoard = new Board(rules);
        turn = rules.getWhiteBegins() ? CheckersGame.WHITE : CheckersGame.BLACK;
        capturedBlackPieces = new ArrayList<>();
        capturedWhitePieces = new ArrayList<>();
        moveHistory = new Stack<Move>();
        boardHistory = new Stack<Board>();
        turnHistory  = new Stack<Integer>();
        capturedBlackPiecesHistory = new Stack<>();
        capturedWhitePiecesHistory = new Stack<>();
        isFinished = false;
        this.gameType = gameType;
        searchDepth=depth;
    }

    public GameType getGameType() {
        return gameType;
    }

    public boolean isGameFinished() {
        return isFinished;
    }

    public void setGameFinished(boolean gameFinished) {
        isFinished = gameFinished;
    }

    /**
     * Changes the current player to black if white is active and vice versa
     */
    private void advanceTurn() {
        if (turn == CheckersGame.WHITE) {
            turn = CheckersGame.BLACK;
        } else {
            turn = CheckersGame.WHITE;
        }
    }

    /**
     * Returns the board of the current game
     * @return board of the current game
     */
    public Board getBoard() {
        return this.gameBoard;
    }

    public ArrayList<Piece> getCapturedBlackPieces() {
        return capturedBlackPieces;
    }

    public ArrayList<Piece> getCapturedWhitePieces() {
        return capturedWhitePieces;
    }

    private ArrayList<Piece> getCapturedPiecesForMove(Move move) {
        ArrayList<Piece> pieces = new ArrayList<>();

        for (Position p: move.capturePositions)
            pieces.add(getBoard().getPiece(p));

        return pieces;
    }

    /**
     * Returns the longest move which can be executed for a pair of start- and end-positions
     * @param start start-position of desired move
     * @param end end-position of desired move
     * @return move with most capturePositions for a pair of start- and end-positions
     */
    public Move getLongestMove(Position start, Position end) {
        Move longest = null;
        Move[] moveset = getMoves();
        for (Move move : moveset) {
            if (move.start().equals(start) && move.end().equals(end)) {
                if (longest == null ||
                        longest.capturePositions.size() < move.capturePositions.size())
                    longest = move;
            }
        }
        return longest;
    }

    /**
     * Generates an array of allowed moves for the current player
     * @return array of allowed moves for the current player
     */
    public Move[] getMoves() {
        return gameBoard.getMoves(turn);
    }

    /**
     * returns the possible moves for a specified player
     */
    public Move[] getMoves(int turn) {
        return gameBoard.getMoves(turn);
    }

    /**
     * Executes a move on the board and passes the turn to the next player
     * @param move move to execute
     */
    public void makeMove(Move move) {
        if (whoseTurn() == BLACK)
            capturedWhitePieces.addAll(getCapturedPiecesForMove(move));
        else
            capturedBlackPieces.addAll(getCapturedPiecesForMove(move));
        gameBoard.makeMove(move);
        advanceTurn();
    }

    public void saveHistory(Move move) {
        moveHistory.add(move);
        boardHistory.add(new Board(gameBoard.saveBoard(), rules));
        turnHistory.add(whoseTurn());
        capturedWhitePiecesHistory.add(new ArrayList<>(capturedWhitePieces));
        capturedBlackPiecesHistory.add(new ArrayList<>(capturedBlackPieces));
    }

    public void prevMove() {
        if (getMovesHistorySize() < 1) {
            return;
        }
        // Pop and restores move
        Move move = moveHistory.pop();
        gameBoard = boardHistory.pop();
        turn = turnHistory.pop();
        capturedWhitePieces = capturedWhitePiecesHistory.pop();
        capturedBlackPieces = capturedBlackPiecesHistory.pop();
    }

    public int getMovesHistorySize() {
        return moveHistory.size();
    }

    /**
     * Returns the ID of the current player
     * @return ID of the current player
     */
    public int whoseTurn() {
        return turn;
    }

    public int getBlackNormalIconId() {
        return blackNormalIconId;
    }

    public int getBlackKingIconId() {
        return blackKingIconId;
    }

    public int getWhiteNormalIconId() {
        return whiteNormalIconId;
    }

    public int getWhiteKingIconId() {
        return whiteKingIconId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeParcelable(gameBoard, 0);
        dest.writeInt(whoseTurn());
        rules.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<CheckersGame> CREATOR
            = new Parcelable.Creator<CheckersGame>() {
        public CheckersGame createFromParcel(Parcel in) {
            return new CheckersGame(in);
        }

        public CheckersGame[] newArray(int size) {
            return new CheckersGame[size];
        }
    };

    /** recreate object from parcel */
    private CheckersGame(Parcel in) {

        gameBoard = in.readParcelable(Board.class.getClassLoader());
        turn = in.readInt();
        rules = GameRules.CREATOR.createFromParcel(in);
        moveHistory = new Stack<Move>();
        boardHistory = new Stack<Board>();
        turnHistory  = new Stack<Integer>();
        capturedBlackPiecesHistory = new Stack<>();
        capturedWhitePiecesHistory = new Stack<>();
    }

    /**
     * Copy constructor to create new Game instances for looking into the future possibilities
     */
    public CheckersGame(CheckersGame checkersGame){
        this.rules = checkersGame.rules;
        this.gameBoard=new Board(checkersGame.gameBoard.saveBoard(), checkersGame.rules);
        this.turn=checkersGame.turn;
        this.capturedBlackPieces=new ArrayList<Piece>(checkersGame.capturedBlackPieces);
        this.capturedWhitePieces=new ArrayList<Piece>(checkersGame.capturedWhitePieces);
        this.isFinished=checkersGame.isFinished;
        this.gameType=checkersGame.gameType;
        this.searchDepth=checkersGame.searchDepth;
        moveHistory = new Stack<Move>();
        boardHistory = new Stack<Board>();
        turnHistory  = new Stack<Integer>();
        capturedBlackPiecesHistory = new Stack<>();
        capturedWhitePiecesHistory = new Stack<>();
    }
}
