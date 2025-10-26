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

import java.io.Serializable

/**
 * This class models a single game piece for a game of checkers. It can be either black or white
 * and additionally, it can be a normal piece or a kinged piece, which can move in further directions.
 */
class Piece internal constructor(
    /**
     * Returns the color of this piece
     * @return color of this piece
     */
    val color: Int,
    /**
     * Returns whether this piece is a king
     * @return true if piece is a king
     */
    var isKing: Boolean
) : Serializable {
    var summaryID: Int
        private set

    // summary IDs
    val EMPTY: Int = 0
    val BLACK: Int = 1
    val WHITE: Int = 2
    val BLACK_KING: Int = 3
    val WHITE_KING: Int = 4

    /**
     * Constructs a piece with chosen color and king-status
     * @param color color of player whom this piece belongs to
     * @param isKing indicates if piece is a king
     */
    init {
        summaryID = color

        if (this.isKing) summaryID += 2
    }

    /**
     * Makes this piece a king
     */
    fun makeKing() {
        isKing = true
        summaryID = color + 2
    }
}
