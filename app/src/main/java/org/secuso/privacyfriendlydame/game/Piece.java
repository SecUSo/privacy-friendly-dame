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

import java.io.Serializable;

/**
 * This class models a single game piece for a game of checkers. It can be either black or white
 * and additionally, it can be a normal piece or a kinged piece, which can move in further directions.
 */
public class Piece implements Serializable {
    private int color;
    private boolean isKing;
    private int summaryID;

    // summary IDs
    public final int EMPTY = 0;
    public final int BLACK = 1;
    public final int WHITE = 2;
    public final int BLACK_KING = 3;
    public final int WHITE_KING = 4;

    /**
     * Constructs a piece with chosen color and king-status
     * @param color color of player whom this piece belongs to
     * @param king indicates if piece is a king
     */
    Piece(int color, boolean king) {
        this.color = color;
        this.isKing = king;

        summaryID = color;

        if (isKing())
            summaryID += 2;
    }

    /**
     * Returns the color of this piece
     * @return color of this piece
     */
    public int getColor() {
        return color;
    }

    public int getSummaryID() {
        return summaryID;
    }

    /**
     * Returns whether this piece is a king
     * @return true if piece is a king
     */
    public boolean isKing() {
        return isKing;
    }

    /**
     * Makes this piece a king
     */
    void makeKing() {
        isKing = true;
        summaryID += 2;
    }
}
