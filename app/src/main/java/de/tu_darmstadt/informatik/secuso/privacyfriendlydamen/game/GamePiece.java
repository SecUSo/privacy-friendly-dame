package de.tu_darmstadt.informatik.secuso.privacyfriendlydamen.game;

/**
 * Created by Marc on 16.11.2017.
 */

public class GamePiece {

    private boolean color;
    private boolean isKing;

    public GamePiece(boolean color, boolean isKing) {
        this.color = color;
        this.isKing = isKing;
    }
}
