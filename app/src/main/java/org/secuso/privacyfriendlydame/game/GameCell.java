package org.secuso.privacyfriendlydame.game;

/**
 * Created by Marc on 16.11.2017.
 */

public class GameCell {

    private boolean color;
    private GamePiece piece;

    public GameCell(boolean color){
        this.color  = color;
        this.piece = null;
    }

    public void placePiece(GamePiece piece) {
        this.piece = piece;
    }

    public void removePiece() {
        this.piece = null;
    }

    public boolean isBlack() {
        return this.color;
    }

    public boolean hasPiece() {
        return (!(piece == null));
    }

    public GamePiece getPiece() {
        return piece;
    }
}
