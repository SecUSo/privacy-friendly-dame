package de.tu_darmstadt.informatik.secuso.privacyfriendlydame.controller;

import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.game.GameBoard;
import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.game.GameCell;

/**
 * Created by Marc on 18.11.2017.
 */

public class GameController {

    private GameBoard gameBoard;
    private boolean currentPlayer;

    public GameController() {
        this.gameBoard = new GameBoard(8);
    }

    public int getSize() {
        return gameBoard.getDimension();
    }

    public GameCell getCellAt(int i, int j) {
        return gameBoard.getGameCellAt(i, j);
    }
}
