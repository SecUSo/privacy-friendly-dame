package org.secuso.privacyfriendlydame.game;

import android.os.AsyncTask;
import android.preference.PreferenceFragment;

import org.secuso.privacyfriendlydame.ui.GameActivity;

import java.util.ArrayList;

public class ComputerTurn extends AsyncTask<String, String, String>
{
    private GameActivity myActivity;
    private CheckersGame myGame;
    private String myDifficulty;
    private Move selectedMove;
    private boolean allowAnyMove;

    public ComputerTurn(GameActivity activity,
                        CheckersGame game,
                        String difficulty,
                        boolean allowAny)
    {
        super();

        myActivity = activity;
        myGame = game;
        myDifficulty = difficulty;
        allowAnyMove = allowAny;
        selectedMove = null;
    }

    protected int minimax(Board base, int turn, int depth)
    {
        int oppositeTurn = (turn == CheckersGame.RED ? CheckersGame.BLACK : CheckersGame.RED);
        Move[] baseMoves = base.getMoves(turn, allowAnyMove);

        int score = 999;

        int[][] data = base.saveBoard();
        for (Move move : baseMoves)
        {
            Board specificBoard = new Board(data);
            specificBoard.makeMove(move);

            int moveScore;
            if (depth > 0) {
                moveScore = minimax(specificBoard, oppositeTurn, depth - 1);
            } else {
                moveScore = specificBoard.pseudoScore();
            }

            if (score == 999) {
                score = moveScore;
            }

            if (turn == CheckersGame.RED) {
                // MIN
                score = (moveScore < score) ? moveScore : score;
            } else if (turn == CheckersGame.BLACK) {
                // MAX
                score = (moveScore > score) ? moveScore : score;
            }
        }
        return score;
    }

    protected Move Minimax(int depth)
    {
        Board realBoard = myGame.getBoard();
        Move moves[] = myGame.getMoves();

        int[][] data = realBoard.saveBoard();

        ArrayList<Move> bestMoves = new ArrayList<>();
        int bestScore = 1000;

        for (Move move : moves) {
            Board moveBoard = new Board(data);
            moveBoard.makeMove(move);
            int score = minimax(moveBoard, CheckersGame.BLACK, depth);
            if (score < bestScore) {
                bestMoves.clear();
                bestScore = score;
            }
            if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        int randomIndex = (int)(Math.random() * bestMoves.size());
        Move randomMove = bestMoves.get(randomIndex);
        return randomMove;
    }

    @Override
    protected String doInBackground(String... strings) {
        if (myGame.whoseTurn() != CheckersGame.RED) return null;
        Move moves[] = myGame.getMoves();

        if (moves.length == 0) return null;

        int difficulty = 0;

        if (difficulty == 0)
        {
            // easy CPU chooses move randomly
            int num = (int)(moves.length * Math.random());
            selectedMove = moves[num];
        }
        else if (difficulty == 1)
        {
            // medium CPU looks for most captures or kings
            selectedMove = moves[0];

            ArrayList<Move> selectedMoves = new ArrayList<>();

            int curScore = -1;
            for (Move option : moves) {
                int score = option.captures.size();
                Piece startPiece = myGame.getBoard().getPiece(option.start());
                if (option.kings && !startPiece.isKing())
                {
                    score += 2;
                }
                if (score > curScore) {
                    //selectedMove = option;
                    selectedMoves.clear();
                    selectedMoves.add(option);
                    curScore = score;
                } else if (score == curScore) {
                    selectedMoves.add(option);
                }
            }

            selectedMove = selectedMoves.get((int)(selectedMoves.size() * Math.random()));
        }

        if (difficulty > 1)
        {
            int depth = (difficulty == 2) ? 4 : 7;
            selectedMove = Minimax(depth);
        }
        else
        {
            // sleep on easy/medium
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (myGame.whoseTurn() == CheckersGame.RED) {
            if (selectedMove != null) {
                myGame.makeMove(selectedMove);
                myActivity.prepTurn();
            } else {
                // player wins
                myActivity.statusText.setText("You won!");
            }
        }
    }
}
