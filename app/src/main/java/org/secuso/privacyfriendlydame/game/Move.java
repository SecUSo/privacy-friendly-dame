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


// move as a sequence of positions and list of capture positions
public class Move {
    public ArrayList<Position> positions;
    public ArrayList<Position> captures;
    public boolean kings;

    public Move(Position pos) {
        init(pos.x, pos.y);
    }

    public Move(int x, int y) {
        init(x, y);
    }

    private void init(int x, int y) {
        Position first = new Position(x, y);
        positions = new ArrayList();
        positions.add(first);
        kings = false;
        captures = new ArrayList();
    }

    // copy constructor
    public Move(Move clone) {
        kings = clone.kings;
        positions = new ArrayList<>();
        for (Position position : clone.positions) {
            positions.add(position);
        }
        captures = new ArrayList<>();
        for (Position capture : clone.captures) {
            captures.add(capture);
        }
    }

    public void add(Position pos) {
        add(pos.x, pos.y);
    }

    public void add(int x, int y) {
        Position prev = positions.get(positions.size() - 1);
        Position next = new Position(x, y);
        positions.add(next);
        // check if move is a capture
        int dist = Math.abs(prev.x - x) + Math.abs(prev.y - y);
        if (dist > 2) {
            int cx = (prev.x + x) / 2;
            int cy = (prev.y + y) / 2;
            captures.add(new Position(cx, cy));
        }
        // check if move is a king
        if (y == 0 || y == 7) {
            kings = true;
        }
    }

    public Position start() {
        return positions.get(0);
    }

    public Position end() {
        return positions.get(positions.size() - 1);
    }
}
