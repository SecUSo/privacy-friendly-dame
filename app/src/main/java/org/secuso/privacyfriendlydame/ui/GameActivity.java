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

// TODO: javadoc
public class GameActivity extends AppCompatActivity {
    private CheckersGame game;
    private CheckersLayout checkersView;
    private GameType gameType;
    private TextView currentPlayerText;
    private LinearLayout capturedBlackPiecesUI;
    private LinearLayout capturedWhitePiecesUI;

    @Override
    protected void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        setContentView(R.layout.activity_game);
        if (saved == null) {
            game = new CheckersGame();
            Bundle extras = getIntent().getExtras();
            this.gameType = GameType.valueOf(extras.getString("gameType", GameType.Bot.name()));
        }
        else
            game = saved.getParcelable("gameController");

        // generate new layout for the board
        checkersView = new CheckersLayout(game, this);
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
        capturedBlackPiecesUI = new LinearLayout(this);
        capturedBlackPiecesUI.setOrientation(LinearLayout.HORIZONTAL);

        capturedWhitePiecesUI = new LinearLayout(this);
        capturedWhitePiecesUI.setOrientation(LinearLayout.HORIZONTAL);

        mainContentLayout.addView(capturedBlackPiecesUI);
        mainContentLayout.addView(capturedWhitePiecesUI);

        // portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);




        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener);

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

    Piece selectedPiece;
    Position selectedPosition;
    Piece selectablePieces[];
    Position moveOptions[];


    // prepare a human or computer turn
    public void prepTurn() {
        Board board = game.getBoard();

        selectedPiece = null;
        selectedPosition = null;
        selectablePieces = null;
        moveOptions = null;

        int turn = game.whoseTurn();

        if (gameType == GameType.Bot && turn == CheckersGame.WHITE) {
            currentPlayerText.setText(R.string.game_current_player_ai);

            makeComputerTurn();

        }else{
            if (turn == CheckersGame.WHITE)
                currentPlayerText.setText(R.string.game_current_player_white);
            else
                currentPlayerText.setText(R.string.game_current_player_black);
            // prep for human player turn
            ArrayList<Piece> selectablePieces = new ArrayList<>();
            Move moves[] = game.getMoves();

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

        updateCapturedPiecesUI();
        checkersView.refresh();
    }

    // difficulty easy: randomly pick a move
    private void makeComputerTurn() {
        if (game.whoseTurn() == CheckersGame.WHITE) {
            Move moves[] = game.getMoves();
            if (moves.length > 0) {
                Move choice;
                int num = (int)(moves.length * Math.random());
                choice = moves[num];
                game.makeMove(choice);
                prepTurn();
            } else {
                // player wins
                showWinDialog();
            }
        }
    }

    private void updateCapturedPiecesUI() {
        int index;
        while (game.getCapturedBlackPieces().size() > capturedBlackPiecesUI.getChildCount()) {
            index = capturedBlackPiecesUI.getChildCount();
            capturedBlackPiecesUI.addView(generatePieceImage(game.getCapturedBlackPieces().get(index).getSummaryID()));
        }
        while (game.getCapturedWhitePieces().size() > capturedWhitePiecesUI.getChildCount()) {
            index = capturedWhitePiecesUI.getChildCount();
            capturedWhitePiecesUI.addView(generatePieceImage(game.getCapturedWhitePieces().get(index).getSummaryID()));
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
                && piece.getColor() == game.whoseTurn())
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

                Move allMoves[] = game.getMoves();

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
        Move move = game.getLongestMove(selectedPosition, destination);
        if (move != null) {
            game.makeMove(move);
            prepTurn();
        }
    }

    // player makes a click
    public void onClick(int x, int y) {
        Position location = new Position(x, y);
        Piece targetPiece = game.getBoard().getPiece(x, y);

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

        savedInstanceState.putParcelable("game", game);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }
}
