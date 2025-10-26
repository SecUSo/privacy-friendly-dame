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
import org.secuso.privacyfriendlydame.R
import java.io.Serializable

/**
 * This class models a game of checkers. It contains information about the game board, the current
 * player, the game type and the captured pieces of both players.
 */
class CheckersGame : Parcelable, Serializable {
    var searchDepth: Int = 0

    /**
     * Returns the board of the current game
     * @return board of the current game
     */
    // checkers game state
    val board: Board?
    private var turn: Int
    private var capturedBlackPieces: ArrayList<Piece?>? = null
    private var capturedWhitePieces: ArrayList<Piece?>? = null
    var isGameFinished: Boolean = false
    var gameType: GameType? = null
        private set

    // rules
    private val rules: GameRules

    // Piece image resource IDs
    @JvmField
    val blackNormalIconId: Int = R.drawable.ic_piece_black
    @JvmField
    val blackKingIconId: Int = R.drawable.ic_piece_black_queen
    @JvmField
    val whiteNormalIconId: Int = R.drawable.ic_piece_white
    @JvmField
    val whiteKingIconId: Int = R.drawable.ic_piece_white_queen

    /**
     * Default constructor which creates a new game with a default board setup and black as the first player
     */
    constructor(gameType: GameType?, depth: Int, rules: GameRules) {
        this.rules = rules
        this.board = Board(rules)
        turn = if (rules.getWhiteBegins()) WHITE else BLACK
        capturedBlackPieces = ArrayList<Piece?>()
        capturedWhitePieces = ArrayList<Piece?>()
        this.isGameFinished = false
        this.gameType = gameType
        searchDepth = depth
    }

    /**
     * Changes the current player to black if white is active and vice versa
     */
    private fun advanceTurn() {
        if (turn == WHITE) {
            turn = BLACK
        } else {
            turn = WHITE
        }
    }

    fun getCapturedBlackPieces(): ArrayList<Piece?> {
        return capturedBlackPieces!!
    }

    fun getCapturedWhitePieces(): ArrayList<Piece?> {
        return capturedWhitePieces!!
    }

    private fun getCapturedPiecesForMove(move: Move): ArrayList<Piece?> {
        val pieces = ArrayList<Piece?>()

        for (p in move.capturePositions) pieces.add(this.board!!.getPiece(p))

        return pieces
    }

    /**
     * Returns the longest move which can be executed for a pair of start- and end-positions
     * @param start start-position of desired move
     * @param end end-position of desired move
     * @return move with most capturePositions for a pair of start- and end-positions
     */
    fun getLongestMove(start: Position, end: Position): Move? {
        var longest: Move? = null
        val moveset = this.moves
        for (move in moveset) {
            if (move.start().equals(start) && move.end().equals(end)) {
                if (longest == null ||
                    longest.capturePositions.size < move.capturePositions.size
                ) longest = move
            }
        }
        return longest
    }

    val moves: Array<Move>
        /**
         * Generates an array of allowed moves for the current player
         * @return array of allowed moves for the current player
         */
        get() = board!!.getMoves(turn)

    /**
     * returns the possible moves for a specified player
     */
    fun getMoves(turn: Int): Array<Move> {
        return board!!.getMoves(turn)
    }

    /**
     * Executes a move on the board and passes the turn to the next player
     * @param move move to execute
     */
    fun makeMove(move: Move) {
        if (whoseTurn() == BLACK) capturedWhitePieces!!.addAll(getCapturedPiecesForMove(move))
        else capturedBlackPieces!!.addAll(getCapturedPiecesForMove(move))
        board!!.makeMove(move)
        advanceTurn()
    }

    /**
     * Returns the ID of the current player
     * @return ID of the current player
     */
    fun whoseTurn(): Int {
        return turn
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(this.board, 0)
        dest.writeInt(whoseTurn())
        rules.writeToParcel(dest, flags)
    }

    /** recreate object from parcel  */
    private constructor(`in`: Parcel) {
        this.board = `in`.readParcelable<Board?>(Board::class.java.getClassLoader())
        turn = `in`.readInt()
        rules = GameRules.CREATOR.createFromParcel(`in`)
    }

    /**
     * Copy constructor to create new Game instances for looking into the future possibilities
     */
    constructor(checkersGame: CheckersGame) {
        this.rules = checkersGame.rules
        this.board = Board(checkersGame.board!!.saveBoard(), checkersGame.rules)
        this.turn = checkersGame.turn
        this.capturedBlackPieces = ArrayList<Piece?>(checkersGame.capturedBlackPieces)
        this.capturedWhitePieces = ArrayList<Piece?>(checkersGame.capturedWhitePieces)
        this.isGameFinished = checkersGame.isGameFinished
        this.gameType = checkersGame.gameType
        this.searchDepth = checkersGame.searchDepth
    }

    companion object {
        const val NONE: Int = 0
        const val BLACK: Int = 1
        const val WHITE: Int = 2
        const val KINGED: Int = 3

        val CREATOR
                : Parcelable.Creator<CheckersGame?> = object : Parcelable.Creator<CheckersGame?> {
            override fun createFromParcel(`in`: Parcel): CheckersGame {
                return CheckersGame(`in`)
            }

            override fun newArray(size: Int): Array<CheckersGame?> {
                return arrayOfNulls<CheckersGame>(size)
            }
        }
    }
}
