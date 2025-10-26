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

/**
 * This class models a position of a square on the game board. The x and y coordinate values range
 * from 0 to 7.
 */
class Position
/**
 * Default constructor which assigns the x and y coordinates to this position
 * @param x x coordinate of position
 * @param y y coordinate of position
 */(// x and y coordinates of position
    @JvmField val x: Int, @JvmField val y: Int
) {
    /**
     * Compares if two positions represent the same square on the board
     * @param other other position this position is compared to
     * @return true if both x- and y-values are the same
     */
    override fun equals(other: Any?): Boolean {
        if (other != null && other is Position) {
            val otherPosition = other
            return (x == otherPosition.x && y == otherPosition.y)
        } else {
            return false
        }
    }

    /**
     * Adds the x- and y values of one position to another position
     * Primarily used to traverse the board by adding e.g. (1, 1) or (-1, 1).
     * @param to position whose x- and y values are added to this position
     * @return new position which resulted from adding the x- and y-coordinates respectively.
     */
    fun plus(to: Position): Position {
        return Position(x + to.x, y + to.y)
    }

    fun equals(other: Position): Boolean {
        return x == other.x && y == other.y
    }
}
