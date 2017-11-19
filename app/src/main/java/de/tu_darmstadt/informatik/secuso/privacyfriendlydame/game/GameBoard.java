package de.tu_darmstadt.informatik.secuso.privacyfriendlydame.game;

/**
 * Created by Marc on 16.11.2017.
 */

public class GameBoard {

    private int dimension;
    private GameCell[][] board;


    public GameBoard(int dimension) {
        this.dimension = dimension;
        this.board = new GameCell[this.dimension][this.dimension];

        initBoard();
        initPieces();
    }

    /**
     * board[0][0] == top left corner is white == false
     * board[0][7] == top right corner is black == true
     * board[7][0] == bottom left corner is black == true
     * board[7][7] == bottom right corner is white == false
     */
    private void initBoard() {
        for (int i = 0; i < dimension; i++)
            for (int j = 0; j < dimension; j++) {
                if ((i + j) % 2 == 0)
                    board[i][j] = new GameCell(false);
                else
                    board[i][j] = new GameCell(true);
            }
    }

    /**
     * board[0][0] == top left corner is empty
     * board[0][1] == placement of first red piece
     * board[5][1] == placement of first white piece
     * board[7][7] == bottom right corner is empty
     *
     * all black fields in rows 0,1,2 are filled with red pieces
     * all black fields in rows 5,6,7 are filled with white pieces
     */
    private void initPieces() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < dimension; j++)
                if (board[i][j].isBlack())
                    board[i][j].placePiece(new GamePiece(true, false));

        for (int i = 5; i < dimension; i++)
            for (int j = 0; j < dimension; j++)
                if (board[i][j].isBlack())
                    board[i][j].placePiece(new GamePiece(false, false));
    }

    public int getDimension() {
        return this.dimension;
    }

    public GameCell getGameCellAt(int i, int j) {
        return board[i][j];
    }
}
