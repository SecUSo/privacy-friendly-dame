package de.tu_darmstadt.informatik.secuso.privacyfriendlydame.game;

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

    public GamePiece removePiece() {
        GamePiece tempPiece = this.piece;
        this.piece = null;

        return tempPiece;
    }

    public boolean isBlack() {
        return this.color;
    }
}
