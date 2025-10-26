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
 * This class is used to model a move in a game of checkers. A move keeps track of each board
 * position which is traversed as well as each position where an opposing piece is located which is
 * captured when that move is executed.
 */
class Move {
    @JvmField
    var positions: ArrayList<Position?>? = null
    @JvmField
    var capturePositions: ArrayList<Position?>? = null
    var isKinged: Boolean = false
        private set

    constructor(pos: Position) {
        init(pos.x, pos.y)
    }

    // copy constructor
    constructor(clone: Move) {
        this.isKinged = clone.isKinged
        positions = ArrayList<Position?>()
        for (position in clone.positions!!) {
            positions!!.add(position)
        }
        capturePositions = ArrayList<Position?>()
        for (capture in clone.capturePositions!!) {
            capturePositions!!.add(capture)
        }
    }

    private fun init(x: Int, y: Int) {
        val first = Position(x, y)
        positions = ArrayList<Position?>()
        positions!!.add(first)
        this.isKinged = false
        capturePositions = ArrayList<Position?>()
    }

    fun add(pos: Position): Move {
        return add(pos.x, pos.y)
    }

    fun add(x: Int, y: Int): Move {
        val next = Position(x, y)
        positions!!.add(next)

        // check if move results in a new king
        if (y == 0 || y == 7) {
            this.isKinged = true
        }
        return this
    }

    /**
     * add a position of a piece which is captured when executing the move
     * @param position position of piece which is captured
     */
    fun addCapture(position: Position?) {
        capturePositions!!.add(position)
    }

    fun start(): Position? {
        return positions!!.get(0)
    }

    fun end(): Position? {
        return positions!!.get(positions!!.size - 1)
    }

    fun equals(other: Move): Boolean {
        if (positions!!.size != other.positions!!.size) return false
        for (i in positions!!.indices) {
            if (!positions!!.get(i)!!.equals(other.positions!!.get(i))) return false
        }
        return true
    }
}