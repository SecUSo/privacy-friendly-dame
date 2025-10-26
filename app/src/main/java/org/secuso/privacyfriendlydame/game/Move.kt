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
import java.util.ArrayList;


/**
 * This class is used to model a move in a game of checkers. A move keeps track of each board
 * position which is traversed as well as each position where an opposing piece is located which is
 * captured when that move is executed.
 */
public class Move {
    public ArrayList<Position> positions;
    public ArrayList<Position> capturePositions;
    private boolean kinged;

    public Move(Position pos) {
        init(pos.x, pos.y);
    }

    // copy constructor
    public Move(Move clone) {
        kinged = clone.kinged;
        positions = new ArrayList<>();
        for (Position position : clone.positions) {
            positions.add(position);
        }
        capturePositions = new ArrayList<>();
        for (Position capture : clone.capturePositions) {
            capturePositions.add(capture);
        }
    }

    private void init(int x, int y) {
        Position first = new Position(x, y);
        positions = new ArrayList<>();
        positions.add(first);
        kinged = false;
        capturePositions = new ArrayList<>();
    }

    public Move add(Position pos) {
        return add(pos.x, pos.y);
    }

    public Move add(int x, int y) {
        Position next = new Position(x, y);
        positions.add(next);

        // check if move results in a new king
        if (y == 0 || y == 7) {
            kinged = true;
        }
        return this;
    }

    public boolean isKinged() {
        return kinged;
    }

    /**
     * add a position of a piece which is captured when executing the move
     * @param position position of piece which is captured
     */
    public void addCapture(Position position) {
        capturePositions.add(position);
    }

    public Position start() {
        return positions.get(0);
    }

    public Position end() {
        return positions.get(positions.size() - 1);
    }

    public boolean equals(Move other){
        if(positions.size()!=other.positions.size())return false;
        for(int i = 0; i<positions.size(); i++){
            if(!positions.get(i).equals(other.positions.get(i)))return false;
        }
        return true;
    }

}