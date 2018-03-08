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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is used to model a 8x8 board state for a game of checkers. Each square either contains
 * a piece or is null if empty. By executing a move the current board state can be modified.
 * Methods for loading and saving the current board state are available as well as methods for
 * generating lists of allowed moves for a player.
 */
public class Board implements Parcelable, Serializable{

    // each data field either contains a game piece or is null
    private Piece board[][];

    /**
     * Constructs a new default board
     */
    Board() {
        board = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int side = (y < 3) ? CheckersGame.BLACK : (y > 4) ? CheckersGame.WHITE : 0;
                boolean validSquare = this.isGameSquare(x, y);
                if (side != CheckersGame.NONE && validSquare) {
                    board[x][y] = new Piece(side, false);
                } else {
                    board[x][y] = null;
                }
            }
        }
    }

    /**
     * Reconstructs a board with a two dimensional array containing information for each position
     * @param positions each field represents a position on board
     */
    Board(int[][] positions) {
        //this.checkersGame = checkersGame;
        board = new Piece[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (positions[x][y] > CheckersGame.NONE) {
                    int side = positions[x][y] % CheckersGame.KINGED;
                    boolean kinged = positions[x][y] > CheckersGame.KINGED;
                    board[x][y] = new Piece(side, kinged);
                } else {
                    board[x][y] = null;
                }
            }
        }
    }

     /**
      * Generates a list of moves for a certain position which include at least one capture
      * @param start starting position of all moves which are generated
      * @return list of moves which include at least one capture
      */
    private ArrayList<Move> getCaptures(Position start)
    {
        ArrayList<Move> base = new ArrayList<>();
        Piece piece = getPiece(start);
        int color = piece.getColor();
        boolean isKing = piece.isKing();

        // add jumps in each direction
        Position[] directions = getDirections(color, isKing);
        for (Position dir : directions) {
            // if the current piece is a king the search range is increased by one in each iteration
            if (isKing) {
                for (int i = 0; i < 8; i++) {
                    Position target = start.plus(dir);
                    for (int j = 0; j < i; j++) {
                        target = target.plus(dir);
                    }
                    Position dest = target.plus(dir);
                    Piece targetPiece = getPiece(target);
                    Piece destPiece = getPiece(dest);

                    // look for a valid landing space with an opposing piece in-between
                    if (isGameSquare(dest) && destPiece == null &&
                            targetPiece != null &&
                            targetPiece.getColor() != color) {
                        Move newMove = new Move(start);
                        newMove.add(dest);
                        newMove.addCapture(target);
                        base.add(newMove);
                    }
                    // if 2 pieces are back-to-back or the position is off-board
                    // the search is terminated in the current direction
                    else if ((targetPiece != null && destPiece != null) || !isGameSquare(target)) {
                        break;
                    }
                }
            }
            else {
                Position target = start.plus(dir);
                Position dest = target.plus(dir);
                Piece targetPiece = getPiece(target);
                Piece destPiece = getPiece(dest);

                // look for a valid landing space with an opposing piece in-between
                if (isGameSquare(dest) && destPiece == null &&
                        targetPiece != null &&
                        targetPiece.getColor() != color) {
                    Move newMove = new Move(start);
                    newMove.add(dest);
                    newMove.addCapture(target);
                    base.add(newMove);
                }
            }
        }

        // find longest for each jump choice
        return getCaptures(start, base);
    }

     /**
      * Tries to extend an existing list of capturePositions(moves) by searching for ways to add more capturePositions
      * @param start starting position of all moves which are generated
      * @param expand list of moves which include exactly one capture
      * @return final list of all moves which have at least one capture
      */
    private ArrayList<Move> getCaptures(Position start, ArrayList<Move> expand)
    {
        ArrayList<Move> finalCaptures = new ArrayList<>();
        ArrayList<Move> furtherCaptures = new ArrayList<>();

        Piece piece = getPiece(start);
        int color = piece.getColor();
        boolean isKing = piece.isKing();

        // create longer moves from existing ones
        for (Move move : expand) {
            Position[] directions = getDirections(color, isKing || move.isKinged());
            Position current = move.end();
            boolean continues = false;
            for (Position dir : directions)
            {
                Position target = current.plus(dir);
                Position dest = target.plus(dir);
                Piece targetPiece = getPiece(target);
                Piece destPiece = getPiece(dest);

                // look for a valid landing space with an opposing piece in-between
                if (isGameSquare(dest) && destPiece == null &&
                        targetPiece != null &&
                        targetPiece.getColor() != color) {
                    // check that the 'opposing piece' hasn't been captured in this move sequence yet
                    boolean valid = true;
                    for (Position captured : move.capturePositions) {
                        if (captured.equals(target)) {
                            valid = false;
                            break;
                        }
                    }
                    // valid piece to capture
                    if (valid) {
                        Move newMove = new Move(move);
                        newMove.add(dest);
                        newMove.addCapture(target);
                        furtherCaptures.add(newMove);
                        continues = true;
                    }
                }
            }

            // only add this move if there are no longer alternatives
            if (!continues) {
                finalCaptures.add(move);
            }
        }

        if (furtherCaptures.size() > 0) {
            furtherCaptures = getCaptures(start, furtherCaptures);
        }
        finalCaptures.addAll(furtherCaptures);

        return finalCaptures;
    }

    /**
     * Returns the directions a piece with passed properties can move towards
     * @param color color of the piece
     * @param king is the piece a king
     * @return allowed directions a piece can move towards
     */
    private Position[] getDirections(int color, boolean king) {
        if (king) {
            return new Position[]{new Position(-1, 1), new Position(1, 1),
                    new Position(-1, -1), new Position(1, -1)};
        } else if (color == CheckersGame.BLACK) {
            return new Position[]{new Position(-1, 1), new Position(1, 1)};
        } else if (color == CheckersGame.WHITE) {
            return new Position[]{new Position(-1, -1), new Position(1, -1)};
        } else {
            return new Position[]{};
        }
    }

     /**
      * Generates a list of all possible moves for a certain position on the board
      * @param start starting position of all moves which are generated
      * @return ArrayList of executable moves
      */
    private ArrayList<Move> getMoves(Position start)
    {
        Piece piece = getPiece(start);

        ArrayList<Move> immediateMoves = new ArrayList<>();

        // check neighboring positions
        Position[] neighbors = getDirections(piece.getColor(), piece.isKing());
        for (Position pos : neighbors) {
            // check each square if it is free to move to
            if (piece.isKing()) {
                for (int i = 0; i < 8; i++) {
                    Position dest = start.plus(pos);
                    for (int j = 0; j < i; j++) {
                        dest = dest.plus(pos);
                    }
                    Piece destPiece = getPiece(dest);

                    // add current square if square is on board and no other piece is on that position
                    if (isGameSquare(dest) && destPiece == null) {
                        Move newMove = new Move(start);
                        newMove.add(dest);
                        immediateMoves.add(newMove);
                    }
                    // else position is off board or another piece occupies the position
                    // so no further checking is required in this direction
                    else {
                        break;
                    }
                }
            }
            else {
                Position dest = start.plus(pos);
                Piece destPiece = getPiece(dest);

                if (isGameSquare(dest) && destPiece == null) {
                    Move newMove = new Move(start);
                    newMove.add(dest);
                    immediateMoves.add(newMove);
                }
            }
        }

        ArrayList<Move> captures = getCaptures(start);
        immediateMoves.addAll(captures);
        return immediateMoves;
    }

     /**
      * Generates a list of possible moves for a player
      * @param currentPlayer current player
      * @return array of executable moves
      */
    Move[] getMoves(int currentPlayer) {
        ArrayList<Move> finalMoves;
        ArrayList<Move> potentialMoves = new ArrayList<>();

        // add moves for each matching piece
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = getPiece(x, y);
                if (piece != null && piece.getColor() == currentPlayer) {
                    Position start = new Position(x, y);
                    potentialMoves.addAll(
                            getMoves(start)
                    );
                }
            }
        }

        // check if non-jumping moves need to be removed
        finalMoves = potentialMoves;

        boolean areCaptures = false;
        for (Move sequence : potentialMoves) {
            if (sequence.capturePositions.size() > 0) {
                areCaptures = true;
                break;
            }
        }
        if (areCaptures) {
            finalMoves = new ArrayList<>();
            for (Move sequence : potentialMoves) {
                if (sequence.capturePositions.size() > 0) {
                    finalMoves.add(sequence);
                }
            }
        }


        // return choices as a sequence of positions
        return finalMoves.toArray(new Move[finalMoves.size()]);
    }

    /**
     * Returns the game piece at a certain position on the board
     * @param x x-coordinate of the position
     * @param y y-coordinate of the position
     * @return game piece at position or null if position is empty
     */
    public Piece getPiece(int x, int y) {
        return (isGameSquare(x, y) ? board[x][y] : null);
    }

    /**
     * Returns the game piece at a certain position on the board
     * @param pos position on board
     * @return game piece at position or null if position is empty
     */
    public Piece getPiece(Position pos) {
        return getPiece(pos.x, pos.y);
    }

    // find a piece on the board
    public Position getPosition(Piece piece) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (getPiece(x, y) == piece) {
                    return new Position(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a coordinate-pair is within the bounds of the board dimensions
     * @param x x-coordinate of position to check
     * @param y y-coordinate of position to check
     * @return true if coordinates represent a position on the board
     */
    public boolean isGameSquare(int x, int y) {
        // within 8x8 dimensions and is odd-square
        return (x >= 0 && y >= 0 && x < 8 && y < 8 && (x + y) % 2 > 0);
    }

    /**
     * Checks if a position, which consists of an x- and y-value, is within the bounds of the board dimensions
     * @param pos the position to check
     * @return true if the coordinates which represent the position are on the board
     */
    private boolean isGameSquare(Position pos) {
        return isGameSquare(pos.x, pos.y);
    }

     /**
      * Executes a move by placing a piece to an end position and removing all capturePositions in between
      * @param move move to execute
      */
    void makeMove(Move move) {
        Position start = move.start();
        Position end = move.end();
        Piece piece = getPiece(start);

        // clear visited positions
        for (Position pos : move.positions) {
            board[pos.x][pos.y] = null;
        }
        // clear captured positions and decrease piece count
        for (Position cap : move.capturePositions) {
            board[cap.x][cap.y] = null;
        }
        // place at end position
        board[end.x][end.y] = piece;
        // check if piece was kinged
        if (move.isKinged()) {
            piece.makeKing();
        }
    }

    /**
     * Saves the current board status as a 2-dimensional array with all necessary information
     * to be able to be reconstructed
     * @return the current board status as a 2-dimensional int array
     */
    int[][] saveBoard() {
        int result[][] = new int[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (board[x][y] != null) {
                    Piece piece = board[x][y];
                    result[x][y] = piece.getColor();
                    if (piece.isKing()) {
                        result[x][y] += CheckersGame.KINGED;
                    }
                } else {
                    result[x][y] = CheckersGame.NONE;
                }
            }
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == null)
                    dest.writeInt(0);
                else
                    dest.writeInt(board[i][j].getSummaryID());
            }
        }

    }
    public static final Parcelable.Creator<Board> CREATOR
            = new Parcelable.Creator<Board>() {
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    /** recreate object from parcel */
    private Board(Parcel in) {
        board = new Piece[8][8];
        int cellID = 0;

        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                cellID = in.readInt();
                switch (cellID) {
                    case 1: board[i][j] = new Piece(1, false); break;
                    case 2: board[i][j] = new Piece(2, false); break;
                    case 3: board[i][j] = new Piece(1, true); break;
                    case 4: board[i][j] = new Piece(2, true); break;
                    default:
                }
            }
        }
    }
}
