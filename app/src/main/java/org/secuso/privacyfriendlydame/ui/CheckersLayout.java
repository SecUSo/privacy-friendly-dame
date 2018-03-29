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

package org.secuso.privacyfriendlydame.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;

import org.secuso.privacyfriendlydame.R;
import org.secuso.privacyfriendlydame.game.Board;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.Move;
import org.secuso.privacyfriendlydame.game.Piece;
import org.secuso.privacyfriendlydame.game.Position;

/**
 * This class is used to generate a custom view of the checkers board. The size of each cell is
 * adjusted to the size of the screen and is updated whenever the layout changes, i.e. when the
 * device is rotated. The refresh method is called whenever the board state has changed and the view
 * has to be updated. Additionally, moves are animated by using a fade-out and fade-in animation
 * to improve the visibility of moves.
 */
public class CheckersLayout extends TableLayout {

    public class CheckerImageView extends AppCompatImageView {
        public int x;
        public int y;
        public CheckerImageView(Activity activity) {
            super(activity);
        }
    }

    protected GameActivity myActivity;
    protected CheckersGame myGame;
    protected CheckerImageView cells[][];

    private final OnClickListener CellClick = new OnClickListener() {
        @Override
        public void onClick(View _view) {
            CheckerImageView view = (CheckerImageView)_view;
            myActivity.onClick(view.x, view.y);
        }
    };

    public void highlightSelectablePieces(Piece[] selectablePieces) {
        for (Piece p: selectablePieces) {
            Position pos = myGame.getBoard().getPosition(p);
            CheckerImageView posCell = cells[pos.getX()][pos.getY()];

            posCell.setBackgroundColor(getResources().getColor(R.color.yellow));
        }

    }

    public void animateMove(Move move) {
        CheckerImageView cellFrom = cells[move.start().getX()][move.start().getY()];
        CheckerImageView cellTo = cells[move.end().getX()][move.end().getY()];
        CheckerImageView cellCapturedPiece;

        int imgID = myGame.getBoard().getPiece(move.start()).getSummaryID();

        switch (imgID) {
            case 1: Glide.with(this).load(myGame.getBlackNormalIconId()).into(cellTo); break;
            case 2: Glide.with(this).load(myGame.getWhiteNormalIconId()).into(cellTo); break;
            case 3: Glide.with(this).load(myGame.getBlackKingIconId()).into(cellTo); break;
            default: Glide.with(this).load(myGame.getWhiteKingIconId()).into(cellTo); break;
        }

        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1500);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);

        cellFrom.setBackgroundColor(getResources().getColor(R.color.yellow));
        cellTo.setBackgroundColor(getResources().getColor(R.color.yellow));
        cellFrom.startAnimation(fadeOut);



        for (Position p: move.capturePositions) {
            cellCapturedPiece = cells[p.getX()][p.getY()];

            cellCapturedPiece.setBackgroundColor(getResources().getColor(R.color.yellow));
            cellCapturedPiece.startAnimation(fadeOut);
        }

        for (Position p: move.positions) {
            cellCapturedPiece = cells[p.getX()][p.getY()];

            cellCapturedPiece.setBackgroundColor(getResources().getColor(R.color.yellow));
            cellCapturedPiece.startAnimation(fadeOut);
        }


        cellTo.startAnimation(fadeIn);
    }

    public void refresh() {
        Board myBoard = myGame.getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++)
                if (myBoard.isGameSquare(x, y)) {
                    CheckerImageView cell = cells[x][y];
                    Piece piece = myBoard.getPiece(x, y);
                    if (piece != null) {
                        int id = piece.getSummaryID();
                        // set the correct image
                        switch (id) {
                            case 1: Glide.with(this).load(myGame.getBlackNormalIconId()).into(cell); break;
                            case 2: Glide.with(this).load(myGame.getWhiteNormalIconId()).into(cell); break;
                            case 3: Glide.with(this).load(myGame.getBlackKingIconId()).into(cell); break;
                            default: Glide.with(this).load(myGame.getWhiteKingIconId()).into(cell); break;
                        }
                        // set the background color
                        if (myActivity.isSelected(piece)) {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellSelect));
                        } else {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellBlack));
                        }
                    } else {
                        // clear the image
                        cell.setImageDrawable(null);
                        Position curPos = new Position(x, y);
                        if (myActivity.isOption(curPos)) {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellOption));
                        } else {
                            cell.setBackgroundColor(getResources().getColor(R.color.cellBlack));
                        }
                    }
                }
        }
    }

    public CheckersLayout(CheckersGame game, GameActivity activity) {
        super(activity);
        myActivity = activity;

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dimension = displayMetrics.widthPixels;
        if (displayMetrics.heightPixels < dimension) {
            dimension = displayMetrics.heightPixels;
        }
        int cellDimension = dimension / 8 ;

        LayoutParams params;

        myGame = game;
        Board myBoard = myGame.getBoard();

        params = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);
        setLayoutParams(params);
        setBackgroundColor(getResources().getColor(R.color.cellBlack));

        // add table of image views
        cells = new CheckerImageView[8][8];
        for (int y = 0; y < 8; y++) {
            TableRow row = new TableRow(activity);
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tableParams.setMargins(0, 0, 0, 0);
            row.setLayoutParams(tableParams);

            for (int x = 0; x < 8; x++) {
                CheckerImageView cell;
                cells[x][y] = cell = new CheckerImageView(activity);
                cell.x = x;
                cell.y = y;

                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                rowParams.setMargins(0, 0, 0, 0);
                rowParams.width = cellDimension;
                rowParams.height = cellDimension;
                cell.setLayoutParams(rowParams);

                int bgColor;
                if (myBoard.isGameSquare(x,y)) {
                    // add click handler
                    cell.setOnClickListener(CellClick);
                    bgColor = getResources().getColor(R.color.cellBlack);
                }
                else {
                    bgColor = getResources().getColor(R.color.cellWhite);
                }

                cell.setBackgroundColor(bgColor);
                row.addView(cell);
            }
            addView(row);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ViewGroup.LayoutParams params = cells[i][j].getLayoutParams();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    params.width = (right - left) / 8;
                    params.height = (right - left) / 8;

                } else {
                    params.width = (bottom - top) / 8;
                    params.height = (bottom - top) / 8;
                }
                cells[i][j].setLayoutParams(params);
            }
        }
    }
}
