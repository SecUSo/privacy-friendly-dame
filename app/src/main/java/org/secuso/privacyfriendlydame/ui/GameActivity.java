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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.secuso.privacyfriendlydame.R;
import org.secuso.privacyfriendlydame.game.Board;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.GameRules;
import org.secuso.privacyfriendlydame.game.GameType;
import org.secuso.privacyfriendlydame.game.Move;
import org.secuso.privacyfriendlydame.game.Piece;
import org.secuso.privacyfriendlydame.game.Position;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class GameActivity extends AppCompatActivity {
    private Handler mHandler;

    private CheckersGame game;
    private CheckersLayout checkersView;
    private TextView currentPlayerText;
    private LinearLayout capturedBlackPiecesUI;
    private LinearLayout capturedWhitePiecesUI;
    private ImageButton prevMoveButton;
    Dialog dialog;
    boolean actionInProgress;
    int maxDepth;
    int startOwnPieces, startOwnKings, startEnemyPieces, startEnemyKings;

    @Override
    protected void onCreate(Bundle saved)
    {
        super.onCreate(saved);

        mHandler = new Handler();

        game = loadFile();
        actionInProgress = false;
        setContentView(R.layout.activity_game);

        RelativeLayout content = findViewById((R.id.content_layout));

        if (saved == null) {
            if ((game==null || getIntent().getExtras()!=null)) {
                Bundle extras = getIntent().getExtras();
                GameType gameType = GameType.valueOf(extras.getString("gameType", GameType.Bot.name()));
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                boolean flying_dame = sp.getBoolean(PrefManager.PREF_RULE_FLYING_KING, false);
                boolean white_begins = sp.getBoolean(PrefManager.PREF_RULE_WHITE_BEGINS, false);
                GameRules rules = new GameRules(flying_dame, white_begins);
                game = new CheckersGame(gameType,extras.getInt("level"), rules);
            }
        }
        maxDepth=game.searchDepth;

        //  else game = saved.getParcelable("gameController");

        // generate new layout for the board
        checkersView = new CheckersLayout(game, this);
        checkersView.refresh();

        // layouts which contain all items displayed ingame
        LinearLayout mainContentLayout = findViewById(R.id.main_content);
        LinearLayout sideContentLayout = findViewById(R.id.side_content);

        mainContentLayout.addView(checkersView);

        // text which displays whose turn it is
        currentPlayerText = new TextView(this);
        currentPlayerText.setTextSize(24);
        currentPlayerText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        sideContentLayout.addView(currentPlayerText);

        // layouts for captured pieces
        capturedBlackPiecesUI = new LinearLayout(this);
        capturedBlackPiecesUI.setOrientation(LinearLayout.HORIZONTAL);

        capturedWhitePiecesUI = new LinearLayout(this);
        capturedWhitePiecesUI.setOrientation(LinearLayout.HORIZONTAL);

        sideContentLayout.addView(capturedBlackPiecesUI);
        sideContentLayout.addView(capturedWhitePiecesUI);



        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        prevMoveButton = findViewById(R.id.prevMoveButton);
        prevMoveButton.setClickable(false);
        prevMoveButton.setBackgroundResource(R.drawable.button_disabled);
        prevMoveButton.setOnClickListener(v -> {
            if (game.getMovesHistorySize() >= 1) {
                game.prevMove();
                mHandler.removeCallbacksAndMessages(null);
                restoreCapturedPiecesUI();
                checkersView.refresh();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        prepTurn();
                    }
                }, 1000);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        game = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener);
    }

    private ImageView generatePieceImage(int id) {
        ImageView image = new ImageView(this);


        int pixels = getResources().getConfiguration().orientation != ORIENTATION_LANDSCAPE ?
                Resources.getSystem().getDisplayMetrics().widthPixels / 12:
                Resources.getSystem().getDisplayMetrics().widthPixels / 12 / 2;

        image.setLayoutParams(new LinearLayout.LayoutParams(pixels, pixels));
        switch (id) {
            case 1: Glide.with(this).load(game.getBlackNormalIconId()).into(image); break;
            case 2: Glide.with(this).load(game.getWhiteNormalIconId()).into(image); break;
            case 3: Glide.with(this).load(game.getBlackKingIconId()).into(image); break;
            default: Glide.with(this).load(game.getWhiteKingIconId()).into(image); break;
        }

        return image;
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepTurn();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkersView.refresh();
    }

    Piece selectedPiece;
    Position selectedPosition;
    Piece[] selectablePieces;
    Position[] moveOptions;


    // prepare a human or computer turn
    public void prepTurn() {
        Board board = game.getBoard();

        selectedPiece = null;
        selectedPosition = null;
        selectablePieces = null;
        moveOptions = null;

        int turn = game.whoseTurn();

        if (game.getGameType() == GameType.Bot && turn == CheckersGame.BLACK) {
            currentPlayerText.setText(R.string.game_current_player_ai);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    makeComputerTurn();
                    actionInProgress = false;
                }
            }, 1000);

        }else{
            if (turn == CheckersGame.BLACK)
                currentPlayerText.setText(R.string.game_current_player_black);
            else
                currentPlayerText.setText(R.string.game_current_player_white);
            // prep for human player turn
            ArrayList<Piece> selectablePieces = new ArrayList<>();
            Move[] moves = game.getMoves();

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
                game.setGameFinished(true);
                // delete file
                deleteFile("savedata");
                showWinDialog();
            }
        }

        updateCapturedPiecesUI();
        checkersView.refresh();
        if (game.getMovesHistorySize() >= 1) {
            prevMoveButton.setClickable(true);
            prevMoveButton.setBackgroundResource(R.drawable.button_normal);
        } else {
            prevMoveButton.setClickable(false);
            prevMoveButton.setBackgroundResource(R.drawable.button_disabled);
        }
    }

    // difficulty easy: randomly pick a move
    private void makeComputerTurn() {
        if (game.whoseTurn() == CheckersGame.BLACK) {
            Move[] moves = game.getMoves();
            if (moves.length > 0) {
                //available: moves--> captures



                //int num = (int)(moves.length * Math.random());
                //final Move choice = moves[num];
                switch (game.searchDepth){
                    case 0:
                        //0-1
                        maxDepth = new Random().nextInt(2);
                        break;
                    case 4:
                        //3-5
                        maxDepth = new Random().nextInt(3)+3;
                        break;
                    case 8:
                        //7-9
                        maxDepth = new Random().nextInt(3)+7;
                        break;
                    case 12:
                        //10-14
                        maxDepth = new Random().nextInt(5)+10;
                        break;
                    case 16:
                        //15-20
                        maxDepth = new Random().nextInt(6)+15;
                        break;
                }
                final Move choice = alphabetaSearch(moves);



                checkersView.animateMove(choice);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(game != null) {
                            game.saveHistory(choice);
                            game.makeMove(choice);
                            prepTurn();
                        }
                    }
                }, 1500);


            } else {
                // player wins
                game.setGameFinished(true);
                showWinDialog();
            }
        }
    }

    /**
     * Move search algorithm that plays out the possible moves' future until maxdepth and returns the move with best value
     * @param legalMoves the moves to check
     * @return best move
     */
    Move alphabetaSearch(Move[] legalMoves){
        Move best=null;
        ArrayList<Move> currentBest;
        startOwnPieces=game.getBoard().getPieceCount(1);
        startOwnKings=game.getBoard().getPieceCount(3);
        startEnemyPieces=game.getBoard().getPieceCount(2);
        startEnemyKings=game.getBoard().getPieceCount(4);

        currentBest = new ArrayList<Move>();
        int maxValue=Integer.MIN_VALUE;

        for(Move move:legalMoves){
            //make move and evaluate resulting game
            CheckersGame next=new CheckersGame(game);
            next.makeMove(move);

            int value = min(next,Integer.MAX_VALUE,Integer.MIN_VALUE,0);

            //is the move valuable?
            if (value==maxValue){
                currentBest.add(move);
            }else if (value>maxValue){
                currentBest.clear();
                currentBest.add(move);
                maxValue=value;
            }

        }

        //choose random move from those with best value
        int ran = (int)(currentBest.size() * Math.random());
        best=currentBest.get(ran);

        return best;
    }

    /**
     * Minimizing algorithm (enemies perspective) which alternates with Maximizing (bot perspective).
     * @param game game which has changed one move from last depth
     * @param alpha maximum value from previous depths
     * @param beta minimum value from previous depths
     * @param depth current tree depth which is break condition
     * @return minimum possible value for bot in this game given the remaining depth
     */
    int min(CheckersGame game,int alpha,int beta,int depth){
        Move[] legalMoves=game.getMoves();
        if (legalMoves.length==0||depth==maxDepth){
            return evaluation(game);
        }
        int val=Integer.MAX_VALUE;
        for (Move move:legalMoves){
            CheckersGame next = new CheckersGame(game);
            next.makeMove(move);
            val=Math.min(val,max(next,alpha,beta,depth+1));
            if(val<=alpha)return val;
            beta=Math.min(beta,val);
        }

        return val;
    }

    /**
     * Maximizing (bot perspective) algorithm which alternates with Minimizing (enemies perspective).
     * @param game game which has changed one move from last depth
     * @param alpha maximum value from previous depths
     * @param beta minimum value from previous depths
     * @param depth current tree depth which is break condition
     * @return maximum possible value for bot in this game given the remaining depth
     */
    int max(CheckersGame game,int alpha,int beta,int depth){
        Move[] legalMoves=game.getMoves();
        if (legalMoves.length==0||depth==maxDepth){
            return evaluation(game);
        }
        int val=Integer.MIN_VALUE;
        for (Move move:legalMoves){
            CheckersGame next = new CheckersGame(game);
            next.makeMove(move);
            val=Math.max(val,min(next,alpha,beta,depth+1));
            if(val>=beta)return val;
            alpha=Math.max(alpha,val);
        }
        return val;
    }

    /**
     * called when maxdepth is reached, puts a value on the game state.
     * @param game game that has been min-maxed until maxdepth
     * @return value of that game state for bot
     */
    int evaluation(CheckersGame game){
        int gameValue=0;
        int ownPieces=0;
        int ownKings=0;
        int enemyPieces=0;
        int enemyKings=0;
        Board board = game.getBoard();


        for (int i=0; i<8;i++){
            for (int j=0; j<8;j++){
                switch (board.getPieceID(i,j)){
                    case 1:
                        ownPieces++;
                        gameValue+=defending(i,j,board)*50 + (i==0? 50:0)+ 15*i+ 100 -((Math.abs(4 - i) + Math.abs(4 - j)) * 10 );
                        break;
                    case 2:
                        enemyPieces++;
                        gameValue-=defending(i,j,board)*50 + (i==0? 50:0)+ 15*(7-i)+ 100 -((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                    case 3:
                        ownKings++;
                        gameValue+=100 -((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                    case 4:
                        enemyKings++;
                        gameValue-=100 -((Math.abs(4 - i) + Math.abs(4 - j)) * 10);
                        break;
                }
            }
        }

        //trading is encouraged when ahead
        if(startOwnPieces + startOwnKings > startEnemyPieces + startEnemyKings
                && enemyPieces + enemyKings != 0
                && startEnemyPieces + startEnemyKings != 0
                && startEnemyKings != 1){
            if((ownPieces+ownKings)/(enemyPieces+enemyKings)>(startOwnPieces+startOwnKings)/(startEnemyPieces+startEnemyKings)){
                gameValue+=150;
            }else{
                gameValue-=150;
            }
        }

        //addition of resulting pieces to score
        gameValue+=600*ownPieces+1000*ownKings-600*enemyPieces-1000*enemyKings;


        //check for number of moves (only when players have few pieces)
        if(startOwnKings+startOwnPieces<6||startEnemyPieces+startEnemyKings<6){
            Move[] blackMoves=game.getMoves(CheckersGame.BLACK);
            Move[] whiteMoves=game.getMoves(CheckersGame.WHITE);
            if(blackMoves.length<1){
                return Integer.MIN_VALUE;
            }
            if(whiteMoves.length<1){
                return Integer.MAX_VALUE;
            }
        }

        //no left pieces cases get considered
        if(enemyPieces+enemyKings==0 && ownPieces+ownKings>0){
            gameValue=Integer.MAX_VALUE;
        }
        if(ownPieces+ownKings==0 && enemyPieces+enemyKings>0){
            gameValue=Integer.MIN_VALUE;
        }

        return gameValue;
    }

    /**
     * assigns a value to the "safety" of a certain piece on the board, depending on the defending pieces
     * @param yrow row position
     * @param xcolumn column position
     * @param board board which sets the conditions
     * @return high value if that piece is well defended, low otherwise
     */
    int defending(int yrow, int xcolumn,Board board){
        int n=0;

        switch (board.getPieceID(xcolumn,yrow)){
            case 1:
                if(xcolumn+1 < 8 &&yrow+1<8){
                    if((board.getPieceID(xcolumn+1,yrow+1)&1)==1){
                        n++;
                    }
                }
                if(xcolumn+1 <8 &&yrow-1>=0){
                    if((board.getPieceID(xcolumn+1,yrow-1)&1)==1){
                        n++;
                    }
                }
                break;

            case 2:
                if(xcolumn-1 >=0 &&yrow+1<8){
                    if((board.getPieceID(xcolumn-1,yrow+1)&1)==0){
                        n++;
                    }
                }
                if(xcolumn-1>=0 &&yrow-1>=0){
                    if((board.getPieceID(xcolumn-1,yrow-1)&1)==0){
                        n++;
                    }
                }
                break;

            case 3:
                if(xcolumn+1 < 8 &&yrow+1<8){
                    if((board.getPieceID(xcolumn+1,yrow+1)&1)==1){
                        n++;
                    }
                }
                if(xcolumn+1 <8 &&yrow-1>=0){
                    if((board.getPieceID(xcolumn+1,yrow-1)&1)==1){
                        n++;
                    }
                }
                if(xcolumn-1 >=0 &&yrow+1<8){
                    if((board.getPieceID(xcolumn-1,yrow+1)&1)==1){
                        n++;
                    }
                }
                if(xcolumn-1>=0 &&yrow-1>=0){
                    if((board.getPieceID(xcolumn-1,yrow-1)&1)==1){
                        n++;
                    }
                }
                break;

            case 4:
                if(xcolumn+1 < 8 &&yrow+1<8){
                    if((board.getPieceID(xcolumn+1,yrow+1)&1)==0){
                        n++;
                    }
                }
                if(xcolumn+1 <8 &&yrow-1>=0){
                    if((board.getPieceID(xcolumn+1,yrow-1)&1)==0){
                        n++;
                    }
                }
                if(xcolumn-1 >=0 &&yrow+1<8){
                    if((board.getPieceID(xcolumn-1,yrow+1)&1)==0){
                        n++;
                    }
                }
                if(xcolumn-1>=0 &&yrow-1>=0){
                    if((board.getPieceID(xcolumn-1,yrow-1)&1)==0){
                        n++;
                    }
                }
                break;

        }

        return n;
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

    private void restoreCapturedPiecesUI() {
        capturedBlackPiecesUI.removeAllViews();
        capturedWhitePiecesUI.removeAllViews();
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

                Move[] allMoves = game.getMoves();

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
        final Move move = game.getLongestMove(selectedPosition, destination);
        if (move != null) {
            checkersView.animateMove(move);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(game != null) {
                        game.saveHistory(move);
                        game.makeMove(move);
                        prepTurn();
                        actionInProgress = false;
                    }
                }
            }, 1500);
        }
        else {
            actionInProgress = false;
        }
    }

    // player makes a click
    public void onClick(int x, int y) {
        if (!actionInProgress) {
            if (game.getGameType() == GameType.Bot && game.whoseTurn() == CheckersGame.BLACK)
                ;
            else {
                Position location = new Position(x, y);
                Piece targetPiece = game.getBoard().getPiece(x, y);

                // attempting to make a move
                if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
                    //game.advanceTurn();
                    actionInProgress = true;
                    makeMove(location);
                } else {
                    selectPiece(targetPiece, location);
                    if (selectedPiece == null)
                        checkersView.highlightSelectablePieces(selectablePieces);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkersView.refresh();
                            }
                    }, 500);
                }
            }
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
        // show alertDialog
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(GameActivity.this);

        // Setting Dialog Title and Message dependent on turn and GameMode
        if(game.getGameType() == GameType.Bot){
            if(game.whoseTurn()==CheckersGame.BLACK){
                builder.setTitle(R.string.playerWinDialogTitle);
                builder.setMessage(R.string.playerWinDialogText);
            } else {
                builder.setTitle(R.string.botWinDialogTitle);
                builder.setMessage(R.string.botWinDialogText);
            }
        } else{
            builder.setTitle(R.string.playerWinDialogTitle);
            if(game.whoseTurn()==CheckersGame.BLACK){
                builder.setMessage(R.string.whiteWinDialogText);
            } else {
                builder.setMessage(R.string.blackWinDialogText);
            }
        }

        builder.setPositiveButton(R.string.sWinDialogBack, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // open Settings
                Intent intent = new Intent(GameActivity.this, MainActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.sWinDialogShowBoard, new DialogInterface.OnClickListener()     {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
                dialog.dismiss();
            }
        });
        if (!this.isFinishing())
        {
            builder.show();
        }
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

    public void onPause()
    {
        if(dialog!=null && dialog.isShowing())
        {
            dialog.dismiss();
        }
        //state will be saved in a file
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = openFileOutput("savedata", Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(game);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) try { oos.close(); } catch (IOException ignored) {}
            if (fos != null) try { fos.close(); } catch (IOException ignored) {}
        }

        super.onPause();
    }

    private CheckersGame loadFile() {
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        try {
            fis = this.openFileInput("savedata");
            ois = new ObjectInputStream(fis);
            game = (CheckersGame) ois.readObject();
            return game;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) try { ois.close(); } catch (IOException e) { e.printStackTrace();}
            if (fis != null) try { fis.close(); } catch (IOException e) { e.printStackTrace();}
        }
        return null;
    }

    public void onBackPressed() {
        Intent mainActivity = new Intent(GameActivity.this, MainActivity.class);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
        finish();
    }
}


