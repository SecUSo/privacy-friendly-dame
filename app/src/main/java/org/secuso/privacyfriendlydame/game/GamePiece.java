package org.secuso.privacyfriendlydame.game;

/**
 * Created by Marc on 16.11.2017.
 */

public class GamePiece {

    private boolean color;
    private boolean isKing;
    private boolean isSelected;
    private int x, y;

    public GamePiece(boolean color, boolean isKing, int x, int y) {
        this.color = color;
        this.isKing = isKing;
        this.isSelected = false;
        this.x = x;
        this.y = y;
    }

    public boolean isKing() {
        return isKing;
    }

    public boolean isRed() {
        return color;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void toggleSelected() {
        isSelected = !isSelected;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
}
