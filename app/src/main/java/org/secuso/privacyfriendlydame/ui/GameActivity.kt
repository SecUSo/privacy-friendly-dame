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
package org.secuso.privacyfriendlydame.ui

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.secuso.privacyfriendlydame.R
import org.secuso.privacyfriendlydame.game.Board
import org.secuso.privacyfriendlydame.game.CheckersGame
import org.secuso.privacyfriendlydame.game.GameRules
import org.secuso.privacyfriendlydame.game.GameType
import org.secuso.privacyfriendlydame.game.Move
import org.secuso.privacyfriendlydame.game.Piece
import org.secuso.privacyfriendlydame.game.Position
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Random
import kotlin.math.abs

class GameActivity : AppCompatActivity() {
    private var mHandler: Handler? = null

    private var game: CheckersGame? = null
    private var checkersView: CheckersLayout? = null
    private var currentPlayerText: TextView? = null
    private var capturedBlackPiecesUI: LinearLayout? = null
    private var capturedWhitePiecesUI: LinearLayout? = null
    var dialog: Dialog? = null
    var actionInProgress: Boolean = false
    var maxDepth: Int = 0
    var startOwnPieces: Int = 0
    var startOwnKings: Int = 0
    var startEnemyPieces: Int = 0
    var startEnemyKings: Int = 0

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)

        mHandler = Handler()

        game = loadFile()
        actionInProgress = false
        setContentView(R.layout.activity_game)

        val content = findViewById<RelativeLayout?>((R.id.content_layout))

        if (saved == null) {
            if ((game == null || getIntent().getExtras() != null)) {
                val extras = getIntent().getExtras()
                val gameType = GameType.valueOf(extras!!.getString("gameType", GameType.Bot.name))
                val sp = PreferenceManager.getDefaultSharedPreferences(this)
                val flying_dame = sp.getBoolean(PrefManager.PREF_RULE_FLYING_KING, false)
                val white_begins = sp.getBoolean(PrefManager.PREF_RULE_WHITE_BEGINS, false)
                val rules = GameRules(flying_dame, white_begins)
                game = CheckersGame(gameType, extras.getInt("level"), rules)
            }
        }
        maxDepth = game!!.searchDepth

        //  else game = saved.getParcelable("gameController");

        // generate new layout for the board
        checkersView = CheckersLayout(game, this)
        checkersView!!.refresh()

        // layouts which contain all items displayed ingame
        val mainContentLayout = findViewById<LinearLayout>(R.id.main_content)
        val sideContentLayout = findViewById<LinearLayout>(R.id.side_content)

        mainContentLayout.addView(checkersView)

        // text which displays whose turn it is
        currentPlayerText = TextView(this)
        currentPlayerText!!.setTextSize(24f)
        currentPlayerText!!.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
        sideContentLayout.addView(currentPlayerText)

        // layouts for captured pieces
        capturedBlackPiecesUI = LinearLayout(this)
        capturedBlackPiecesUI!!.setOrientation(LinearLayout.HORIZONTAL)

        capturedWhitePiecesUI = LinearLayout(this)
        capturedWhitePiecesUI!!.setOrientation(LinearLayout.HORIZONTAL)

        sideContentLayout.addView(capturedBlackPiecesUI)
        sideContentLayout.addView(capturedWhitePiecesUI)


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        mHandler!!.removeCallbacksAndMessages(null)
        game = null

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    private fun generatePieceImage(id: Int): ImageView {
        val image = ImageView(this)


        val pixels =
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) Resources.getSystem()
                .getDisplayMetrics().widthPixels / 12 else Resources.getSystem()
                .getDisplayMetrics().widthPixels / 12 / 2

        image.setLayoutParams(LinearLayout.LayoutParams(pixels, pixels))
        when (id) {
            1 -> Glide.with(this).load(game!!.getBlackNormalIconId()).into(image)
            2 -> Glide.with(this).load(game!!.getWhiteNormalIconId()).into(image)
            3 -> Glide.with(this).load(game!!.getBlackKingIconId()).into(image)
            else -> Glide.with(this).load(game!!.getWhiteKingIconId()).into(image)
        }

        return image
    }

    override fun onResume() {
        super.onResume()
        prepTurn()
    }

    var selectedPiece: Piece? = null
    var selectedPosition: Position? = null
    var selectablePieces: Array<Piece?>?
    var moveOptions: Array<Position>?


    // prepare a human or computer turn
    fun prepTurn() {
        val board = game!!.getBoard()

        selectedPiece = null
        selectedPosition = null
        selectablePieces = null
        moveOptions = null

        val turn = game!!.whoseTurn()

        if (game!!.getGameType() == GameType.Bot && turn == CheckersGame.BLACK) {
            currentPlayerText!!.setText(R.string.game_current_player_ai)

            mHandler!!.postDelayed(object : Runnable {
                override fun run() {
                    makeComputerTurn()
                    actionInProgress = false
                }
            }, 1000)
        } else {
            if (turn == CheckersGame.BLACK) currentPlayerText!!.setText(R.string.game_current_player_black)
            else currentPlayerText!!.setText(R.string.game_current_player_white)
            // prep for human player turn
            val selectablePieces = ArrayList<Piece?>()
            val moves = game!!.getMoves()

            // find pieces which can be moved
            for (move in moves) {
                val newPiece = board.getPiece(move.start())
                if (!selectablePieces.contains(newPiece)) {
                    selectablePieces.add(newPiece)
                }
            }

            // convert to array
            this.selectablePieces = selectablePieces.toTypedArray<Piece?>()

            if (selectablePieces.size == 0) {
                game!!.setGameFinished(true)
                // delete file
                deleteFile("savedata")
                showWinDialog()
            }
        }

        updateCapturedPiecesUI()
        checkersView!!.refresh()
    }

    // difficulty easy: randomly pick a move
    private fun makeComputerTurn() {
        if (game!!.whoseTurn() == CheckersGame.BLACK) {
            val moves = game!!.getMoves()
            if (moves.size > 0) {
                //available: moves--> captures


                //int num = (int)(moves.length * Math.random());
                //final Move choice = moves[num];


                when (game!!.searchDepth) {
                    0 ->                         //0-1
                        maxDepth = Random().nextInt(2)

                    4 ->                         //3-5
                        maxDepth = Random().nextInt(3) + 3

                    8 ->                         //7-9
                        maxDepth = Random().nextInt(3) + 7

                    12 ->                         //10-14
                        maxDepth = Random().nextInt(5) + 10

                    16 ->                         //15-20
                        maxDepth = Random().nextInt(6) + 15
                }
                val choice = alphabetaSearch(moves)


                checkersView!!.animateMove(choice)

                mHandler!!.postDelayed(object : Runnable {
                    override fun run() {
                        if (game != null) {
                            game!!.makeMove(choice)
                            prepTurn()
                        }
                    }
                }, 1500)
            } else {
                // player wins
                game!!.setGameFinished(true)
                showWinDialog()
            }
        }
    }

    /**
     * Move search algorithm that plays out the possible moves' future until maxdepth and returns the move with best value
     *
     * @param legalMoves the moves to check
     * @return best move
     */
    fun alphabetaSearch(legalMoves: Array<Move>): Move {
        var best: Move? = null
        val currentBest: ArrayList<Move>?
        startOwnPieces = game!!.getBoard().getPieceCount(1)
        startOwnKings = game!!.getBoard().getPieceCount(3)
        startEnemyPieces = game!!.getBoard().getPieceCount(2)
        startEnemyKings = game!!.getBoard().getPieceCount(4)

        currentBest = ArrayList<Move>()
        var maxValue = Int.Companion.MIN_VALUE

        for (move in legalMoves) {
            //make move and evaluate resulting game
            val next = CheckersGame(game)
            next.makeMove(move)

            val value = min(next, Int.Companion.MAX_VALUE, Int.Companion.MIN_VALUE, 0)

            //is the move valuable?
            if (value == maxValue) {
                currentBest.add(move)
            } else if (value > maxValue) {
                currentBest.clear()
                currentBest.add(move)
                maxValue = value
            }
        }

        //choose random move from those with best value
        val ran = (currentBest.size * Math.random()).toInt()
        best = currentBest.get(ran)

        return best
    }

    /**
     * Minimizing algorithm (enemies perspective) which alternates with Maximizing (bot perspective).
     *
     * @param game  game which has changed one move from last depth
     * @param alpha maximum value from previous depths
     * @param beta  minimum value from previous depths
     * @param depth current tree depth which is break condition
     * @return minimum possible value for bot in this game given the remaining depth
     */
    fun min(game: CheckersGame, alpha: Int, beta: Int, depth: Int): Int {
        var beta = beta
        val legalMoves = game.getMoves()
        if (legalMoves.size == 0 || depth == maxDepth) {
            return evaluation(game)
        }
        var `val` = Int.Companion.MAX_VALUE
        for (move in legalMoves) {
            val next = CheckersGame(game)
            next.makeMove(move)
            `val` = kotlin.math.min(`val`, max(next, alpha, beta, depth + 1))
            if (`val` <= alpha) return `val`
            beta = kotlin.math.min(beta, `val`)
        }

        return `val`
    }

    /**
     * Maximizing (bot perspective) algorithm which alternates with Minimizing (enemies perspective).
     *
     * @param game  game which has changed one move from last depth
     * @param alpha maximum value from previous depths
     * @param beta  minimum value from previous depths
     * @param depth current tree depth which is break condition
     * @return maximum possible value for bot in this game given the remaining depth
     */
    fun max(game: CheckersGame, alpha: Int, beta: Int, depth: Int): Int {
        var alpha = alpha
        val legalMoves = game.getMoves()
        if (legalMoves.size == 0 || depth == maxDepth) {
            return evaluation(game)
        }
        var `val` = Int.Companion.MIN_VALUE
        for (move in legalMoves) {
            val next = CheckersGame(game)
            next.makeMove(move)
            `val` = kotlin.math.max(`val`, min(next, alpha, beta, depth + 1))
            if (`val` >= beta) return `val`
            alpha = kotlin.math.max(alpha, `val`)
        }
        return `val`
    }

    /**
     * called when maxdepth is reached, puts a value on the game state.
     *
     * @param game game that has been min-maxed until maxdepth
     * @return value of that game state for bot
     */
    fun evaluation(game: CheckersGame): Int {
        var gameValue = 0
        var ownPieces = 0
        var ownKings = 0
        var enemyPieces = 0
        var enemyKings = 0
        val board = game.getBoard()


        for (i in 0..7) {
            for (j in 0..7) {
                when (board.getPieceID(i, j)) {
                    1 -> {
                        ownPieces++
                        gameValue += defending(
                            i,
                            j,
                            board
                        ) * 50 + (if (i == 0) 50 else 0) + 15 * i + 100 - ((abs(4 - i) + abs(4 - j)) * 10)
                    }

                    2 -> {
                        enemyPieces++
                        gameValue -= defending(
                            i,
                            j,
                            board
                        ) * 50 + (if (i == 0) 50 else 0) + 15 * (7 - i) + 100 - ((abs(4 - i) + abs(4 - j)) * 10)
                    }

                    3 -> {
                        ownKings++
                        gameValue += 100 - ((abs(4 - i) + abs(4 - j)) * 10)
                    }

                    4 -> {
                        enemyKings++
                        gameValue -= 100 - ((abs(4 - i) + abs(4 - j)) * 10)
                    }
                }
            }
        }

        //trading is encouraged when ahead
        if (startOwnPieces + startOwnKings > startEnemyPieces + startEnemyKings && enemyPieces + enemyKings != 0 && startEnemyPieces + startEnemyKings != 0 && startEnemyKings != 1) {
            if ((ownPieces + ownKings) / (enemyPieces + enemyKings) > (startOwnPieces + startOwnKings) / (startEnemyPieces + startEnemyKings)) {
                gameValue += 150
            } else {
                gameValue -= 150
            }
        }

        //addition of resulting pieces to score
        gameValue += 600 * ownPieces + 1000 * ownKings - 600 * enemyPieces - 1000 * enemyKings


        //check for number of moves (only when players have few pieces)
        if (startOwnKings + startOwnPieces < 6 || startEnemyPieces + startEnemyKings < 6) {
            val blackMoves = game.getMoves(CheckersGame.BLACK)
            val whiteMoves = game.getMoves(CheckersGame.WHITE)
            if (blackMoves.size < 1) {
                return Int.Companion.MIN_VALUE
            }
            if (whiteMoves.size < 1) {
                return Int.Companion.MAX_VALUE
            }
        }

        //no left pieces cases get considered
        if (enemyPieces + enemyKings == 0 && ownPieces + ownKings > 0) {
            gameValue = Int.Companion.MAX_VALUE
        }
        if (ownPieces + ownKings == 0 && enemyPieces + enemyKings > 0) {
            gameValue = Int.Companion.MIN_VALUE
        }

        return gameValue
    }

    /**
     * assigns a value to the "safety" of a certain piece on the board, depending on the defending pieces
     *
     * @param yrow    row position
     * @param xcolumn column position
     * @param board   board which sets the conditions
     * @return high value if that piece is well defended, low otherwise
     */
    fun defending(yrow: Int, xcolumn: Int, board: Board): Int {
        var n = 0

        when (board.getPieceID(xcolumn, yrow)) {
            1 -> {
                if (xcolumn + 1 < 8 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn + 1, yrow + 1) and 1) == 1) {
                        n++
                    }
                }
                if (xcolumn + 1 < 8 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn + 1, yrow - 1) and 1) == 1) {
                        n++
                    }
                }
            }

            2 -> {
                if (xcolumn - 1 >= 0 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn - 1, yrow + 1) and 1) == 0) {
                        n++
                    }
                }
                if (xcolumn - 1 >= 0 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn - 1, yrow - 1) and 1) == 0) {
                        n++
                    }
                }
            }

            3 -> {
                if (xcolumn + 1 < 8 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn + 1, yrow + 1) and 1) == 1) {
                        n++
                    }
                }
                if (xcolumn + 1 < 8 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn + 1, yrow - 1) and 1) == 1) {
                        n++
                    }
                }
                if (xcolumn - 1 >= 0 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn - 1, yrow + 1) and 1) == 1) {
                        n++
                    }
                }
                if (xcolumn - 1 >= 0 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn - 1, yrow - 1) and 1) == 1) {
                        n++
                    }
                }
            }

            4 -> {
                if (xcolumn + 1 < 8 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn + 1, yrow + 1) and 1) == 0) {
                        n++
                    }
                }
                if (xcolumn + 1 < 8 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn + 1, yrow - 1) and 1) == 0) {
                        n++
                    }
                }
                if (xcolumn - 1 >= 0 && yrow + 1 < 8) {
                    if ((board.getPieceID(xcolumn - 1, yrow + 1) and 1) == 0) {
                        n++
                    }
                }
                if (xcolumn - 1 >= 0 && yrow - 1 >= 0) {
                    if ((board.getPieceID(xcolumn - 1, yrow - 1) and 1) == 0) {
                        n++
                    }
                }
            }
        }

        return n
    }

    private fun updateCapturedPiecesUI() {
        var index: Int
        while (game!!.getCapturedBlackPieces().size > capturedBlackPiecesUI!!.getChildCount()) {
            index = capturedBlackPiecesUI!!.getChildCount()
            capturedBlackPiecesUI!!.addView(
                generatePieceImage(
                    game!!.getCapturedBlackPieces().get(index).getSummaryID()
                )
            )
        }
        while (game!!.getCapturedWhitePieces().size > capturedWhitePiecesUI!!.getChildCount()) {
            index = capturedWhitePiecesUI!!.getChildCount()
            capturedWhitePiecesUI!!.addView(
                generatePieceImage(
                    game!!.getCapturedWhitePieces().get(index).getSummaryID()
                )
            )
        }
    }

    // check which piece is selected
    fun isSelected(piece: Piece?): Boolean {
        return (piece != null && piece === selectedPiece)
    }

    // check which squares are options
    fun isOption(checkPosition: Position): Boolean {
        if (moveOptions == null) {
            return false
        }
        for (position in moveOptions) {
            if (position.equals(checkPosition)) {
                return true
            }
        }
        return false
    }

    fun selectPiece(piece: Piece?, location: Position) {
        selectedPiece = null
        selectedPosition = null
        moveOptions = null

        if (piece != null && selectablePieces != null && piece.getColor() == game!!.whoseTurn()) {
            var isSelectable = false
            for (selectablePiece in selectablePieces) {
                if (selectablePiece === piece) {
                    isSelectable = true
                }
            }

            if (isSelectable) {
                selectedPiece = piece
                selectedPosition = location

                // fill move options
                val moveOptionsArr = ArrayList<Position?>()

                val allMoves = game!!.getMoves()

                // iterate through moves
                for (checkMove in allMoves) {
                    val start = checkMove.start()
                    val end = checkMove.end()

                    if (start.equals(location)) {
                        if (!moveOptionsArr.contains(end)) {
                            moveOptionsArr.add(end)
                        }
                    }
                }

                // save list results
                moveOptions = moveOptionsArr.toTypedArray<Position?>()
            }
        }

        checkersView!!.refresh()
    }

    // player made a move
    fun makeMove(destination: Position?) {
        // make longest move available
        val move = game!!.getLongestMove(selectedPosition, destination)
        if (move != null) {
            checkersView!!.animateMove(move)

            mHandler!!.postDelayed(object : Runnable {
                override fun run() {
                    if (game != null) {
                        game!!.makeMove(move)
                        prepTurn()
                        actionInProgress = false
                    }
                }
            }, 1500)
        } else {
            actionInProgress = false
        }
    }

    // player makes a click
    fun onClick(x: Int, y: Int) {
        if (!actionInProgress) {
            if (game!!.getGameType() == GameType.Bot && game!!.whoseTurn() == CheckersGame.BLACK) ; else {
                val location = Position(x, y)
                val targetPiece = game!!.getBoard().getPiece(x, y)

                // attempting to make a move
                if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
                    //game.advanceTurn();
                    actionInProgress = true
                    makeMove(location)
                } else {
                    selectPiece(targetPiece, location)
                    if (selectedPiece == null) {
                        checkersView!!.highlightSelectablePieces(selectablePieces)
                    }
                    mHandler!!.postDelayed(object : Runnable {
                        override fun run() {
                            checkersView!!.refresh()
                        }
                    }, 500)
                }
            }
        }
    }


    private val preferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener =
        object : SharedPreferences.OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences?,
                s: String?
            ) {
                // update preferences

                prepTurn()
            }
        }

    /**
     * Shows the dialog after a game was won with the options of going back to main as well as showing the final game pane
     */
    fun showWinDialog() {
        // show alertDialog
        val builder = AlertDialog.Builder(this@GameActivity)

        // Setting Dialog Title and Message dependent on turn and GameMode
        if (game!!.getGameType() == GameType.Bot) {
            if (game!!.whoseTurn() == CheckersGame.BLACK) {
                builder.setTitle(R.string.playerWinDialogTitle)
                builder.setMessage(R.string.playerWinDialogText)
            } else {
                builder.setTitle(R.string.botWinDialogTitle)
                builder.setMessage(R.string.botWinDialogText)
            }
        } else {
            builder.setTitle(R.string.playerWinDialogTitle)
            if (game!!.whoseTurn() == CheckersGame.BLACK) {
                builder.setMessage(R.string.whiteWinDialogText)
            } else {
                builder.setMessage(R.string.blackWinDialogText)
            }
        }

        builder.setPositiveButton(
            R.string.sWinDialogBack,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, id: Int) {
                    // open Settings
                    val intent = Intent(this@GameActivity, MainActivity::class.java)

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    dialog.dismiss()
                }
            })

        builder.setNegativeButton(
            R.string.sWinDialogShowBoard,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, id: Int) {
                    //do nothing
                    dialog.dismiss()
                }
            })
        if (!this.isFinishing()) {
            builder.show()
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save the user's current game state

        savedInstanceState.putParcelable("game", game)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        //super.onRestoreInstanceState(savedInstanceState);
    }

    public override fun onPause() {
        if (dialog != null && dialog!!.isShowing()) {
            dialog!!.dismiss()
        }
        //state will be saved in a file
        var fos: FileOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            fos = openFileOutput("savedata", MODE_PRIVATE)
            oos = ObjectOutputStream(fos)
            oos.writeObject(game)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (oos != null) try {
                oos.close()
            } catch (ignored: IOException) {
            }
            if (fos != null) try {
                fos.close()
            } catch (ignored: IOException) {
            }
        }

        super.onPause()
    }

    private fun loadFile(): CheckersGame? {
        var ois: ObjectInputStream? = null
        var fis: FileInputStream? = null
        try {
            fis = this.openFileInput("savedata")
            ois = ObjectInputStream(fis)
            game = ois.readObject() as CheckersGame?
            return game
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } finally {
            if (ois != null) try {
                ois.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (fis != null) try {
                fis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun onBackPressed() {
        val mainActivity = Intent(this@GameActivity, MainActivity::class.java)
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(mainActivity)
        finish()
    }
}


