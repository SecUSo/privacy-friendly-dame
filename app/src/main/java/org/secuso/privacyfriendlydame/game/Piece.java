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

 // data for a single piece
public class Piece {
    private int color;
    private boolean isKing;

    Piece(int color) {
        this.color = color;
        isKing = false;
    }

    Piece(int color, boolean king) {
        this.color = color;
        isKing = king;
    }

    public int getColor() {
        return color;
    }

    public boolean isKing() {
        return isKing;
    }

    public void makeKing() {
        isKing = true;
    }
}
