package org.secuso.privacyfriendlydame.controller;

import org.secuso.privacyfriendlydame.game.GameBoard;
import org.secuso.privacyfriendlydame.game.GameCell;
import org.secuso.privacyfriendlydame.game.GamePiece;

/**
 * Created by Marc on 18.11.2017.
 */

public class GameController {

    private GameBoard gameBoard;
    private boolean currentPlayer;
    private GamePiece selectedPiece;

    public GameController() {
        this.gameBoard = new GameBoard(8);
        selectedPiece = null;
    }

    public int getSize() {
        return gameBoard.getDimension();
    }

    public GameCell getCellAt(int i, int j) {
        return gameBoard.getGameCellAt(i, j);
    }

    public void play(int x, int y) {
        if (getCellAt(x, y).hasPiece()) {
            if (selectedPiece == null) {
                selectedPiece = getCellAt(x, y).getPiece();
                selectedPiece.toggleSelected();
            }
            else if (getCellAt(x, y).getPiece() == selectedPiece) {
                selectedPiece.toggleSelected();
                selectedPiece = null;
            }
        }
        else if (selectedPiece != null){
            gameBoard.getGameCellAt(selectedPiece.getX(), selectedPiece.getY()).removePiece();
            gameBoard.getGameCellAt(x, y).placePiece(selectedPiece);
            selectedPiece.setX(x);
            selectedPiece.setY(y);
            selectedPiece.toggleSelected();
            selectedPiece = null;
        }
    }
}
