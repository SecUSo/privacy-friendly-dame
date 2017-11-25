package org.secuso.privacyfriendlydame.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlydame.R;
import org.secuso.privacyfriendlydame.game.Board;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.ComputerTurn;
import org.secuso.privacyfriendlydame.game.Move;
import org.secuso.privacyfriendlydame.game.Piece;
import org.secuso.privacyfriendlydame.game.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class GameActivity extends AppCompatActivity {
    private CheckersGame gamelogic;
    private CheckersLayout checkersView;
    public TextView statusText;

    private String prefDifficulty;
    private boolean prefAllowAnyMove;

    private static final String DIFFICULTY = "pref_difficulty";
    private static final String ANY_MOVE = "pref_any_move";

    @Override
    protected void onCreate(Bundle saved)
    {
        super.onCreate(saved);
        createGameBoard();
        // portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //
        ////PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener);
        prefDifficulty = sharedPreferences.getString(DIFFICULTY, null);
        prefAllowAnyMove = sharedPreferences.getBoolean(ANY_MOVE, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepTurn();
    }


    private void createGameBoard()
    {
        gamelogic = new CheckersGame(prefAllowAnyMove);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        TextView topText = new TextView(this);
        topText.setText("Play Checkers");

        statusText = new TextView(this);
        statusText.setText("status");

        checkersView = new CheckersLayout(gamelogic, this);
        checkersView.refresh();

        rootLayout.addView(topText);
        rootLayout.addView(checkersView);
        rootLayout.addView(statusText);

        setContentView(rootLayout);
    }

    public void clearComputerTask() {
        if (computerTask != null) {
            if (computerTask.getStatus() != AsyncTask.Status.FINISHED)
            {
                computerTask.cancel(true);
            }
            computerTask = null;
        }
    }

    // restart game
    private void restart() {
        clearComputerTask();
        gamelogic.restart();
        checkersView.refresh();
        prepTurn();
    }

    Piece selectedPiece;
    Position selectedPosition;
    Piece selectablePieces[];
    Position moveOptions[];

    ComputerTurn computerTask = null;

    // prepare a human or computer turn
    public void prepTurn() {
        Board board = gamelogic.getBoard();

        selectedPiece = null;
        selectedPosition = null;
        selectablePieces = null;
        moveOptions = null;

        clearComputerTask();

        int turn = gamelogic.whoseTurn();

        if (turn == CheckersGame.RED) {
            statusText.setText("Red's (computer's) turn. Difficulty: "+prefDifficulty);

            // run the CPU AI on another thread
            computerTask = new ComputerTurn(this, gamelogic, prefDifficulty, prefAllowAnyMove);
            computerTask.execute();

        } else if (turn == CheckersGame.BLACK) {
            statusText.setText("Black's (player's) turn.");

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
                statusText.setText("You lost!");
            }
        }

        checkersView.refresh();
    }

    // difficulty easy: randomly pick a move
    private void makeComputerTurn() {
        if (gamelogic.whoseTurn() == CheckersGame.RED) {
            Move moves[] = gamelogic.getMoves();
            if (moves.length > 0) {
                Move choice;
                if (prefDifficulty.equals("Easy")) {
                    int num = (int)(moves.length * Math.random());
                    choice = moves[num];
                } else {
                    // find best move by capture amount vs king
                    choice = moves[0];
                    int curScore = -1;
                    for (Move option : moves) {
                        int score = option.captures.size();
                        Piece startPiece = gamelogic.getBoard().getPiece(option.start());
                        if (option.kings && !startPiece.isKing())
                        {
                            score += 2;
                        }
                        if (score > curScore) {
                            choice = option;
                            curScore = score;
                        }
                    }
                }
                gamelogic.makeMove(choice);
                prepTurn();
            } else {
                // player wins
                statusText.setText("You won!");
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
        if (move != null) {
            gamelogic.makeMove(move);
            prepTurn();
        }
    }

    // player makes a click
    public void onClick(int x, int y) {
        // check if its player's turn
        if (gamelogic.whoseTurn() != CheckersGame.BLACK) {
            return;
        }

        Position location = new Position(x, y);
        Piece targetPiece = gamelogic.getBoard().getPiece(x, y);

        // attempting to make a move
        if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
            makeMove(location);
        }
        else
        {
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

                    gamelogic.setAnyMove(prefAllowAnyMove);

                    prepTurn();
                }
            };
}
