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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlydame.R;
import org.secuso.privacyfriendlydame.game.Board;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.GameType;
import org.secuso.privacyfriendlydame.game.Move;
import org.secuso.privacyfriendlydame.game.Piece;
import org.secuso.privacyfriendlydame.game.Position;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private CheckersGame gamelogic;
    private CheckersLayout checkersView;
    private GameType gameType;
    private TextView currentPlayerText;
    private LinearLayout capturedBlackPieces;
    private LinearLayout capturedWhitePieces;


    private String prefDifficulty;
    private boolean prefAllowAnyMove;

    private static final String DIFFICULTY = "pref_difficulty";
    private static final String ANY_MOVE = "pref_any_move";

    @Override
    protected void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        setContentView(R.layout.activity_game);
        gamelogic = new CheckersGame();

        // generate new layout for the board
        checkersView = new CheckersLayout(gamelogic, this);
        checkersView.refresh();

        // layout which contains all items which are displayed ingame
        LinearLayout mainContentLayout = findViewById(R.id.main_content);
        mainContentLayout.addView(checkersView);

        // text which displays whose turn it is
        currentPlayerText = new TextView(this);
        currentPlayerText.setTextSize(24);
        currentPlayerText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mainContentLayout.addView(currentPlayerText);

        // layouts for captured pieces
        capturedBlackPieces = new LinearLayout(this);
        capturedBlackPieces.setOrientation(LinearLayout.HORIZONTAL);

        capturedWhitePieces = new LinearLayout(this);
        capturedWhitePieces.setOrientation(LinearLayout.HORIZONTAL);

        mainContentLayout.addView(capturedBlackPieces);
        mainContentLayout.addView(capturedWhitePieces);

        // portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Bundle extras = getIntent().getExtras();
        this.gameType = GameType.valueOf(extras.getString("gameType", GameType.Bot.name()));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener);
        prefDifficulty = sharedPreferences.getString(DIFFICULTY, null);
        prefAllowAnyMove = sharedPreferences.getBoolean(ANY_MOVE, false);

    }

    private ImageView generatePieceImage(int id) {
        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        switch(id) {
            case 1: image.setImageResource(R.drawable.ic_piece_black); break;
            case 2: image.setImageResource(R.drawable.ic_piece_white); break;
            case 3: image.setImageResource(R.drawable.ic_piece_black_king); break;
            default: image.setImageResource(R.drawable.ic_piece_white_king); break;
        }


        return image;
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepTurn();
    }

        // restart game
    private void restart() {
        gamelogic.restart();
        checkersView.refresh();
        prepTurn();
    }

    Piece selectedPiece;
    Position selectedPosition;
    Piece selectablePieces[];
    Position moveOptions[];


    // prepare a human or computer turn
    public void prepTurn() {
        Board board = gamelogic.getBoard();

        selectedPiece = null;
        selectedPosition = null;
        selectablePieces = null;
        moveOptions = null;

        int turn = gamelogic.whoseTurn();

        if (turn == CheckersGame.WHITE) {
            if(gameType == GameType.Bot) {
                currentPlayerText.setText(R.string.game_current_player_ai);

                makeComputerTurn();

            }else{
                currentPlayerText.setText(R.string.game_current_player_white);
                // prep for human player turn
                ArrayList<Piece> selectablePieces = new ArrayList<>();
                Move moves[] = gamelogic.getMoves();

                // find pieces which can be moved
                for (Move move : moves) {
                    Piece newPiece = board.getPiece(move.start());
                    if (!selectablePieces.contains(newPiece)) {
                        selectablePieces.add(newPiece);
                    }
                }

                // convert to array
                this.selectablePieces = selectablePieces.toArray(
                        new Piece[selectablePieces.size()]
                );

                if (selectablePieces.size() == 0) {
                    showWinDialog();
                }
            }

        } else if (turn == CheckersGame.BLACK) {
            currentPlayerText.setText(R.string.game_current_player_black);

            // prep for human player turn
            ArrayList<Piece> selectablePieces = new ArrayList<>();
            Move moves[] = gamelogic.getMoves();

            // find pieces which can be moved
            for (Move move : moves) {
                Piece newPiece = board.getPiece(move.start());
                if (!selectablePieces.contains(newPiece)) {
                    selectablePieces.add(newPiece);
                }
            }

            // convert to array
            this.selectablePieces = selectablePieces.toArray(
                    new Piece[selectablePieces.size()]
            );

            if (selectablePieces.size() == 0) {
                showWinDialog();
            }
        }

        checkersView.refresh();
    }

    // difficulty easy: randomly pick a move
    private void makeComputerTurn() {
        if (gamelogic.whoseTurn() == CheckersGame.WHITE) {
            Move moves[] = gamelogic.getMoves();
            if (moves.length > 0) {
                Move choice;
                int color;
                int num = (int)(moves.length * Math.random());
                choice = moves[num];
                if (choice.capturePositions.size() > 0) {
                    for (Position p: choice.capturePositions) {
                        color = gamelogic.getBoard().getPiece(p).getColor();
                        // captured piece is  black
                        if (color == 1) {
                            if (gamelogic.getBoard().getPiece(p).isKing()) {
                                // captured piece is black and king
                                capturedBlackPieces.addView(generatePieceImage(3));
                            }
                            else {
                                capturedBlackPieces.addView(generatePieceImage(1));
                            }
                        }
                        // captured piece is white
                        else {
                            if (gamelogic.getBoard().getPiece(p).isKing()) {
                                // captured piece is white and king
                                capturedWhitePieces.addView(generatePieceImage(4));
                            }
                            else {
                                capturedWhitePieces.addView(generatePieceImage(2));
                            }
                        }
                    }
                }

                gamelogic.makeMove(choice);
                prepTurn();
            } else {
                // player wins
                showWinDialog();
            }
        }
    }

    // check which piece is selected
    public boolean isSelected(Piece piece) {
        return (piece != null && piece == selectedPiece);
    }

    // check which squares are options
    public boolean isOption(Position checkPosition) {
        if (moveOptions == null) {
            return false;
        }
        for (Position position : moveOptions) {
            if (position.equals(checkPosition)) {
                return true;
            }
        }
        return false;
    }

    public void selectPiece(Piece piece, Position location)
    {
        selectedPiece = null;
        selectedPosition = null;
        moveOptions = null;

        if (piece != null && selectablePieces != null
                && piece.getColor() == gamelogic.whoseTurn())
        {
            boolean isSelectable = false;
            for (Piece selectablePiece : selectablePieces) {
                if (selectablePiece == piece) {
                    isSelectable = true;
                }
            }

            if (isSelectable) {
                selectedPiece = piece;
                selectedPosition = location;

                // fill move options

                ArrayList<Position> moveOptionsArr = new ArrayList<>();

                Move allMoves[] = gamelogic.getMoves();

                // iterate through moves
                for (Move checkMove : allMoves) {
                    Position start = checkMove.start();
                    Position end = checkMove.end();

                    if (start.equals(location)) {
                        if (!moveOptionsArr.contains(end)) {
                            moveOptionsArr.add(end);
                        }
                    }
                }

                // save list results
                moveOptions = moveOptionsArr.toArray(new Position[moveOptionsArr.size()]);
            }
        }

        checkersView.refresh();
    }

    // player made a move
    public void makeMove(Position destination)
    {
        // make longest move available
        Move move = gamelogic.getLongestMove(selectedPosition, destination);
        int color;
        if (move != null) {
            if (move.capturePositions.size() > 0) {
                for (Position p: move.capturePositions) {
                    color = gamelogic.getBoard().getPiece(p).getColor();
                    // captured piece is  black
                    if (color == 1) {
                        if (gamelogic.getBoard().getPiece(p).isKing()) {
                            // captured piece is black and king
                            capturedBlackPieces.addView(generatePieceImage(3));
                        }
                        else {
                            capturedBlackPieces.addView(generatePieceImage(1));
                        }
                    }
                    // captured piece is white
                    else {
                        if (gamelogic.getBoard().getPiece(p).isKing()) {
                            // captured piece is white and king
                            capturedWhitePieces.addView(generatePieceImage(4));
                        }
                        else {
                            capturedWhitePieces.addView(generatePieceImage(2));
                        }
                    }
                }
            }
            gamelogic.makeMove(move);
            prepTurn();
        }
    }

    // player makes a click
    public void onClick(int x, int y) {
        Position location = new Position(x, y);
        Piece targetPiece = gamelogic.getBoard().getPiece(x, y);

        // attempting to make a move
        if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
            makeMove(location);
        }
        else{
            selectPiece(targetPiece, location);
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    // update preferences
                    prefDifficulty = sharedPreferences.getString(DIFFICULTY, null);
                    prefAllowAnyMove = sharedPreferences.getBoolean(ANY_MOVE, false);



                    prepTurn();
                }
            };

    /**
     * Shows the dialog after a game was won with the options of going back to main as well as showing the final game pane
     */
    public void showWinDialog() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sWinDialogTitle);
        builder.setMessage(R.string.sWinDialogText);
        builder.setIcon(ResourcesCompat.getDrawable(this.getResources(), R.drawable.medal, null));
        builder.setPositiveButton(R.string.sWinDialogBack, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(GameActivity.this.getApplicationContext(), MainActivity.class);
                GameActivity.this.getApplicationContext().startActivity(intent);

            }
        });
        builder.setNegativeButton(R.string.sWinDialogShowBoard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog winDialog = builder.create();
        winDialog.show();
    }
}
