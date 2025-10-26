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
package org.secuso.privacyfriendlydame.game

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * This class is used to model a 8x8 board state for a game of checkers. Each square either contains
 * a piece or is null if empty. By executing a move the current board state can be modified.
 * Methods for loading and saving the current board state are available as well as methods for
 * generating lists of allowed moves for a player.
 */
class Board : Parcelable, Serializable {
    // each data field either contains a game piece or is null
    private var board: Array<Array<Piece>?>
    private val rules: GameRules

    /**
     * Constructs a new default board
     */
    internal constructor(rules: GameRules) {
        board = Array<Array<Piece>?>(8) { arrayOfNulls<Piece>(8) }
        this.rules = rules
        for (x in 0..7) {
            for (y in 0..7) {
                val side = if (y < 3) CheckersGame.BLACK else if (y > 4) CheckersGame.WHITE else 0
                val validSquare = this.isGameSquare(x, y)
                if (side != CheckersGame.NONE && validSquare) {
                    board[x]!![y] = Piece(side, false)
                } else {
                    board[x]!![y] = null
                }
            }
        }
    }

    /**
     * Reconstructs a board with a two dimensional array containing information for each position
     * @param positions each field represents a position on board
     */
    internal constructor(positions: Array<IntArray?>, rules: GameRules) {
        //this.checkersGame = checkersGame;
        board = Array<Array<Piece>?>(8) { arrayOfNulls<Piece>(8) }
        this.rules = rules
        for (x in 0..7) {
            for (y in 0..7) {
                if (positions[x]!![y] > CheckersGame.NONE) {
                    val side = positions[x]!![y] % CheckersGame.KINGED
                    val kinged = positions[x]!![y] > CheckersGame.KINGED
                    board[x]!![y] = Piece(side, kinged)
                } else {
                    board[x]!![y] = null
                }
            }
        }
    }

    /**
     * Generates a list of moves for a certain position which include at least one capture
     * @param start starting position of all moves which are generated
     * @return list of moves which include at least one capture
     */
    private fun getCaptures(start: Position): ArrayList<Move> {
        val base = ArrayList<Move>()
        val piece = getPiece(start)
        val color = piece.color
        val isKing = piece.isKing

        // add jumps in each direction
        val directions = getDirections(color, isKing)
        for (dir in directions) {
            // if the current piece is a king the search range is increased by one in each iteration
            if (isKing) {
                var lastCapture = -1
                val reach = if (rules.getFlyingDame()) 8 else 1
                for (i in 0..<reach) {
                    var target = start.plus(dir)
                    for (j in 0..<i) {
                        target = target.plus(dir)
                    }
                    val dest = target.plus(dir)
                    val targetPiece = getPiece(target)
                    val destPiece = getPiece(dest)

                    // Only allow single movements and back-to-back capture after the first capture
                    if (lastCapture > 0 && lastCapture < i - 1) {
                        break
                    }

                    // if 2 pieces are back-to-back or the position is off-board
                    // or a piece with same color is in the way
                    // the search is terminated in the current direction
                    if (!isGameSquare(target) || (targetPiece != null && (destPiece != null || targetPiece.color == color))) {
                        break
                    } else if (isGameSquare(dest) && destPiece == null && targetPiece != null && targetPiece.color != color) {
                        val newMove = Move(start)
                        newMove.add(dest)
                        newMove.addCapture(target)
                        base.add(newMove)
                        lastCapture = i
                    }
                }
            } else {
                val target = start.plus(dir)
                val dest = target.plus(dir)
                val targetPiece = getPiece(target)
                val destPiece = getPiece(dest)

                // look for a valid landing space with an opposing piece in-between
                if (isGameSquare(dest) && destPiece == null && targetPiece != null && targetPiece.color != color) {
                    val newMove = Move(start)
                    newMove.add(dest)
                    newMove.addCapture(target)
                    base.add(newMove)
                }
            }
        }

        // find longest for each jump choice
        return getCaptures(start, base)
    }

    /**
     * Tries to extend an existing list of capturePositions(moves) by searching for ways to add more capturePositions
     * @param start starting position of all moves which are generated
     * @param expand list of moves which include exactly one capture
     * @return final list of all moves which have at least one capture
     */
    private fun getCaptures(start: Position, expand: ArrayList<Move>): ArrayList<Move> {
        val finalCaptures = ArrayList<Move>()
        var furtherCaptures = ArrayList<Move>()

        val piece = getPiece(start)
        val color = piece.color
        val isKing = piece.isKing

        // create longer moves from existing ones
        for (move in expand) {
            val directions = getDirections(color, isKing || move.isKinged())
            val current = move.end()
            var continues = false
            for (dir in directions) {
                val target = current.plus(dir)
                val dest = target.plus(dir)
                val targetPiece = getPiece(target)
                val destPiece = getPiece(dest)

                // look for a valid landing space with an opposing piece in-between
                if (isGameSquare(dest) && destPiece == null && targetPiece != null && targetPiece.color != color) {
                    // check that the 'opposing piece' hasn't been captured in this move sequence yet
                    var valid = true
                    for (captured in move.capturePositions) {
                        if (captured.equals(target)) {
                            valid = false
                            break
                        }
                    }
                    // valid piece to capture
                    if (valid) {
                        val newMove = Move(move)
                        newMove.add(dest)
                        newMove.addCapture(target)
                        furtherCaptures.add(newMove)
                        continues = true
                    }
                }
            }

            // only add this move if there are no longer alternatives
            if (!continues) {
                finalCaptures.add(move)
            }
        }

        if (furtherCaptures.size > 0) {
            furtherCaptures = getCaptures(start, furtherCaptures)
        }
        finalCaptures.addAll(furtherCaptures)

        return finalCaptures
    }

    /**
     * Returns the directions a piece with passed properties can move towards
     * @param color color of the piece
     * @param king is the piece a king
     * @return allowed directions a piece can move towards
     */
    private fun getDirections(color: Int, king: Boolean): Array<Position> {
        if (king) {
            return arrayOf<Position>(
                Position(-1, 1), Position(1, 1),
                Position(-1, -1), Position(1, -1)
            )
        } else if (color == CheckersGame.BLACK) {
            return arrayOf<Position>(Position(-1, 1), Position(1, 1))
        } else if (color == CheckersGame.WHITE) {
            return arrayOf<Position>(Position(-1, -1), Position(1, -1))
        } else {
            return arrayOf<Position>()
        }
    }

    /**
     * Generates a list of all possible moves for a certain position on the board
     * @param start starting position of all moves which are generated
     * @return ArrayList of executable moves
     */
    private fun getMoves(start: Position): ArrayList<Move> {
        val piece = getPiece(start)

        val immediateMoves = ArrayList<Move>()

        // check neighboring positions
        val neighbors = getDirections(piece.color, piece.isKing)
        for (pos in neighbors) {
            // check each square if it is free to move to
            if (piece.isKing) {
                val reach = if (rules.getFlyingDame()) 8 else 1
                for (i in 0..<reach) {
                    var dest = start.plus(pos)
                    for (j in 0..<i) {
                        dest = dest.plus(pos)
                    }
                    val destPiece = getPiece(dest)

                    // add current square if square is on board and no other piece is on that position
                    if (isGameSquare(dest) && destPiece == null) {
                        val newMove = Move(start)
                        newMove.add(dest)
                        immediateMoves.add(newMove)
                    } else {
                        break
                    }
                }
            } else {
                val dest = start.plus(pos)
                val destPiece = getPiece(dest)

                if (isGameSquare(dest) && destPiece == null) {
                    val newMove = Move(start)
                    newMove.add(dest)
                    immediateMoves.add(newMove)
                }
            }
        }

        val captures = getCaptures(start)
        immediateMoves.addAll(captures)
        return immediateMoves
    }

    /**
     * Generates a list of possible moves for a player
     * @param currentPlayer current player
     * @return array of executable moves
     */
    fun getMoves(currentPlayer: Int): Array<Move?> {
        var finalMoves: ArrayList<Move>?
        val potentialMoves = ArrayList<Move>()

        // add moves for each matching piece
        for (x in 0..7) {
            for (y in 0..7) {
                val piece = getPiece(x, y)
                if (piece != null && piece.color == currentPlayer) {
                    val start = Position(x, y)
                    potentialMoves.addAll(
                        getMoves(start)
                    )
                }
            }
        }

        // check if non-jumping moves need to be removed
        finalMoves = potentialMoves

        var areCaptures = false
        for (sequence in potentialMoves) {
            if (sequence.capturePositions.size > 0) {
                areCaptures = true
                break
            }
        }
        if (areCaptures) {
            finalMoves = ArrayList<Move>()
            for (sequence in potentialMoves) {
                if (sequence.capturePositions.size > 0) {
                    finalMoves.add(sequence)
                }
            }
        }


        // return choices as a sequence of positions
        return finalMoves.toTypedArray<Move?>()
    }

    /**
     * Returns the game piece at a certain position on the board
     * @param x x-coordinate of the position
     * @param y y-coordinate of the position
     * @return game piece at position or null if position is empty
     */
    fun getPiece(x: Int, y: Int): Piece? {
        return (if (isGameSquare(x, y)) board[x]!![y] else null)
    }

    /**
     * Returns the game piece at a certain position on the board
     * @param pos position on board
     * @return game piece at position or null if position is empty
     */
    fun getPiece(pos: Position): Piece {
        return getPiece(pos.x, pos.y)!!
    }

    // find a piece on the board
    fun getPosition(piece: Piece?): Position? {
        for (x in 0..7) {
            for (y in 0..7) {
                if (getPiece(x, y) === piece) {
                    return Position(x, y)
                }
            }
        }
        return null
    }

    /**
     * Checks if a coordinate-pair is within the bounds of the board dimensions
     * @param x x-coordinate of position to check
     * @param y y-coordinate of position to check
     * @return true if coordinates represent a position on the board
     */
    fun isGameSquare(x: Int, y: Int): Boolean {
        // within 8x8 dimensions and is odd-square
        return (x >= 0 && y >= 0 && x < 8 && y < 8 && (x + y) % 2 > 0)
    }

    /**
     * Checks if a position, which consists of an x- and y-value, is within the bounds of the board dimensions
     * @param pos the position to check
     * @return true if the coordinates which represent the position are on the board
     */
    private fun isGameSquare(pos: Position): Boolean {
        return isGameSquare(pos.x, pos.y)
    }

    /**
     * Executes a move by placing a piece to an end position and removing all capturePositions in between
     * @param move move to execute
     */
    fun makeMove(move: Move) {
        val start = move.start()
        val end = move.end()
        val piece = getPiece(start)

        // clear visited positions
        for (pos in move.positions) {
            board[pos.x]!![pos.y] = null
        }
        // clear captured positions and decrease piece count
        for (cap in move.capturePositions) {
            board[cap.x]!![cap.y] = null
        }
        // place at end position
        board[end.x]!![end.y] = piece
        // check if piece was kinged
        if (move.isKinged()) {
            piece.makeKing()
        }
    }

    /**
     * Saves the current board status as a 2-dimensional array with all necessary information
     * to be able to be reconstructed
     * @return the current board status as a 2-dimensional int array
     */
    fun saveBoard(): Array<IntArray?> {
        val result = Array<IntArray?>(8) { IntArray(8) }
        for (x in 0..7) {
            for (y in 0..7) {
                if (board[x]!![y] != null) {
                    val piece = board[x]!![y]
                    result[x]!![y] = piece.color
                    if (piece.isKing) {
                        result[x]!![y] += CheckersGame.KINGED
                    }
                } else {
                    result[x]!![y] = CheckersGame.NONE
                }
            }
        }
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        rules.writeToParcel(dest, flags)
        for (i in board.indices) {
            for (j in board.indices) {
                if (board[i]!![j] == null) dest.writeInt(0)
                else dest.writeInt(board[i]!![j].getSummaryID())
            }
        }
    }

    /** recreate object from parcel  */
    private constructor(`in`: Parcel) {
        board = Array<Array<Piece>?>(8) { arrayOfNulls<Piece>(8) }
        rules = GameRules.CREATOR.createFromParcel(`in`)

        var cellID = 0
        for (i in board.indices) {
            for (j in board.indices) {
                cellID = `in`.readInt()
                when (cellID) {
                    1 -> board[i]!![j] = Piece(1, false)
                    2 -> board[i]!![j] = Piece(2, false)
                    3 -> board[i]!![j] = Piece(1, true)
                    4 -> board[i]!![j] = Piece(2, true)
                    else -> {}
                }
            }
        }
    }

    /*
     **counts occurrences of piece types
     * returns number of pieces on the board with specified ID
     */
    fun getPieceCount(ID: Int): Int {
        var counter = 0
        for (row in board) {
            for (p in row!!) {
                if (p != null && p.getSummaryID() == ID) counter++
            }
        }
        return counter
    }

    /*
     **similar to getPiece().getSummaryID() with null case and always-empty fields handling
     * returns 0,1,2,3,4 or -1 (if perma-empty field)
     */
    fun getPieceID(x: Int, y: Int): Int {
        if (isGameSquare(x, y)) {
            if (board[x]!![y] == null) return 0
            return board[x]!![y].getSummaryID()
        }
        return -1
    }

    companion object {
        val CREATOR
                : Parcelable.Creator<Board?> = object : Parcelable.Creator<Board?> {
            override fun createFromParcel(`in`: Parcel): Board {
                return Board(`in`)
            }

            override fun newArray(size: Int): Array<Board?> {
                return arrayOfNulls<Board>(size)
            }
        }
    }
}
