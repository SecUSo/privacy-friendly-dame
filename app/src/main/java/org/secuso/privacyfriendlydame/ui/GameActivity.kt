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

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlydame.PFApplicationData
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

class GameActivity : BaseActivity() {
    private val mHandler by lazy { Handler() }

    private lateinit var game: CheckersGame
    private lateinit var checkersView: CheckersLayout
    private lateinit var currentPlayerText: TextView
    private lateinit var capturedBlackPiecesUI: LinearLayout
    private lateinit var capturedWhitePiecesUI: LinearLayout
    var actionInProgress: Boolean = false
    var maxDepth: Int = 0
    var startOwnPieces: Int = 0
    var startOwnKings: Int = 0
    var startEnemyPieces: Int = 0
    var startEnemyKings: Int = 0

    private val dialog by lazy {
        AbortElseDialog.build(this) {
            if (game.gameType == GameType.Bot) {
                if (game.whoseTurn() == CheckersGame.BLACK) {
                    title = { ContextCompat.getString(context, R.string.playerWinDialogTitle) }
                    content = { ContextCompat.getString(context, R.string.playerWinDialogText) }
                } else {
                    title = { ContextCompat.getString(context, R.string.botWinDialogTitle) }
                    content = { ContextCompat.getString(context, R.string.botWinDialogText) }

                }
            } else {
                title = { ContextCompat.getString(context, R.string.playerWinDialogTitle) }
                content = { ContextCompat.getString(context,
                    if (game.whoseTurn() == CheckersGame.BLACK) R.string.whiteWinDialogText
                    else R.string.blackWinDialogText) }
            }
            acceptLabel = ContextCompat.getString(context, R.string.sWinDialogBack)
            abortLabel = ContextCompat.getString(context, R.string.sWinDialogShowBoard)
            onElse = {
                Intent(this@GameActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(this)
                    finish()
                }
            }
        }
    }

    override fun isActiveDrawerElement(element: DrawerElement) = false


    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        setContentView(R.layout.activity_game)

        if (saved == null) {
            loadFile().let {
                if (it == null || intent.extras != null) {
                    val gameType = GameType.valueOf(intent.extras!!.getString("gameType", GameType.Bot.name))
                    val ruleFlying = PFApplicationData.instance(this).ruleFlyingKing.value
                    val ruleWhiteBegins = PFApplicationData.instance(this).ruleWhiteBegins.value
                    val rules = GameRules(ruleFlying, ruleWhiteBegins)
                    game = CheckersGame(gameType, intent.extras!!.getInt("level"), rules)
                }
            }
        } else {
            game = saved.getParcelable("game", CheckersGame::class.java)!!
        }

        maxDepth = game.searchDepth

        // generate new layout for the board
        checkersView = CheckersLayout(game, this)
        checkersView.refresh()

        // layouts which contain all items displayed ingame
        val mainContentLayout = findViewById<LinearLayout>(R.id.main_content)
        val sideContentLayout = findViewById<LinearLayout>(R.id.side_content)

        mainContentLayout.addView(checkersView)

        // text which displays whose turn it is
        currentPlayerText = TextView(this).apply {
            textSize = 24f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            sideContentLayout.addView(this)
        }

        // layouts for captured pieces
        capturedBlackPiecesUI = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            sideContentLayout.addView(this)
        }

        capturedWhitePiecesUI = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            sideContentLayout.addView(this)
        }
    }


    private fun generatePieceImage(id: Int): ImageView {
        var pixels = Resources.getSystem().displayMetrics.widthPixels / 12
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            pixels /= 2
        }

        return ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(pixels, pixels)
            Glide.with(this@GameActivity).load(when (id) {
                1 -> game.blackNormalIconId
                2 -> game.whiteNormalIconId
                3 -> game.blackKingIconId
                else -> game.whiteKingIconId
            }).into(this)
        }
    }

    override fun onResume() {
        super.onResume()
        prepTurn()
    }

    var selectedPiece: Piece? = null
    var selectedPosition: Position? = null
    var selectablePieces: Array<Piece?>? = null
    var moveOptions: Array<Position>? = null


    // prepare a human or computer turn
    fun prepTurn() {
        val board = game.board

        selectedPiece = null
        selectedPosition = null
        selectablePieces = null
        moveOptions = null

        val turn = game.whoseTurn()

        if (game.gameType == GameType.Bot && turn == CheckersGame.BLACK) {
            currentPlayerText.setText(R.string.game_current_player_ai)

            mHandler.postDelayed({
                makeComputerTurn()
                actionInProgress = false
            }, 1000)
        } else {
            currentPlayerText.setText(if (turn == CheckersGame.BLACK) R.string.game_current_player_black else R.string.game_current_player_white)
            // prep for human player turn
            val selectablePieces = ArrayList<Piece?>()

            // find pieces which can be moved
            for (move in game.moves) {
                val newPiece = board.getPiece(move.start())
                if (!selectablePieces.contains(newPiece)) {
                    selectablePieces.add(newPiece)
                }
            }

            // convert to array
            this.selectablePieces = selectablePieces.toTypedArray<Piece?>()

            if (selectablePieces.isEmpty()) {
                game.isGameFinished = true
                // delete file
                deleteFile("savedata")
                dialog.show()
            }
        }

        updateCapturedPiecesUI()
        checkersView.refresh()
    }

    // difficulty easy: randomly pick a move
    private fun makeComputerTurn() {
        if (game.whoseTurn() == CheckersGame.BLACK) {
            if (game.moves.size > 0) {

                when (game.searchDepth) {
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
                alphabetaSearch(game.moves).let { choice ->
                    checkersView.animateMove(choice)

                    mHandler.postDelayed({
                        game.makeMove(choice)
                        if (!isFinishing) {
                            prepTurn()
                        }
                    }, 1500)
                }

            } else {
                // player wins
                game.isGameFinished = true
                dialog.show()
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
        startOwnPieces = game.board.getPieceCount(1)
        startOwnKings = game.board.getPieceCount(3)
        startEnemyPieces = game.board.getPieceCount(2)
        startEnemyKings = game.board.getPieceCount(4)

        val currentBest = ArrayList<Move>()
        var maxValue = Int.MIN_VALUE

        for (move in legalMoves) {
            //make move and evaluate resulting game
            val next = CheckersGame(game).apply {
                makeMove(move)
            }

            min(next, Int.MAX_VALUE, Int.MIN_VALUE, 0).let { value ->
                //is the move valuable?
                if (value == maxValue) {
                    currentBest.add(move)
                } else if (value > maxValue) {
                    currentBest.clear()
                    currentBest.add(move)
                    maxValue = value
                }
            }


        }

        //choose random move from those with best value
        return currentBest[(currentBest.size * Math.random()).toInt()]
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
        if (game.moves.size == 0 || depth == maxDepth) {
            return evaluation(game)
        }
        var value = Int.MAX_VALUE
        for (move in game.moves) {
            val next = CheckersGame(game).apply {
                makeMove(move)
            }
            value = kotlin.math.min(value, max(next, alpha, beta, depth + 1))
            if (value <= alpha) {
                return value
            }
            beta = kotlin.math.min(beta, value)
        }

        return value
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
        if (game.moves.size == 0 || depth == maxDepth) {
            return evaluation(game)
        }
        var value = Int.MIN_VALUE
        for (move in game.moves) {
            val next = CheckersGame(game).apply {
                makeMove(move)
            }
            value = kotlin.math.max(value, min(next, alpha, beta, depth + 1))
            if (value >= beta) {
                return value
            }
            alpha = kotlin.math.max(alpha, value)
        }
        return value
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
                return Int.MIN_VALUE
            }
            if (whiteMoves.size < 1) {
                return Int.MAX_VALUE
            }
        }

        //no left pieces cases get considered
        if (enemyPieces + enemyKings == 0 && ownPieces + ownKings > 0) {
            gameValue = Int.MAX_VALUE
        }
        if (ownPieces + ownKings == 0 && enemyPieces + enemyKings > 0) {
            gameValue = Int.MIN_VALUE
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
        while (game.capturedBlackPieces.size > capturedBlackPiecesUI.childCount) {
            val index = capturedBlackPiecesUI.childCount
            capturedBlackPiecesUI.addView(
                generatePieceImage(game.capturedBlackPieces[index].summaryID)
            )
        }
        while (game.capturedWhitePieces.size > capturedWhitePiecesUI.childCount) {
            val index = capturedWhitePiecesUI.childCount
            capturedWhitePiecesUI.addView(
                generatePieceImage(game.capturedWhitePieces[index].summaryID)
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

        if (piece != null &&
            selectablePieces != null &&
            piece.color == game.whoseTurn()
            && selectablePieces!!.find { it == piece } != null) {

            selectedPiece = piece
            selectedPosition = location

            // fill move options
            val moveOptionsArr = ArrayList<Position>()

            // iterate through moves
            for (checkMove in game.moves) {
                val start = checkMove.start()
                val end = checkMove.end()

                if (start == location) {
                    if (!moveOptionsArr.contains(end)) {
                        moveOptionsArr.add(end)
                    }
                }
            }

            // save list results
            moveOptions = moveOptionsArr.toTypedArray()
        }

        checkersView.refresh()
    }

    // player made a move
    fun makeMove(destination: Position?) {
        // make longest move available
        val move = game.getLongestMove(selectedPosition, destination)
        if (move != null) {
            checkersView.animateMove(move)

            mHandler.postDelayed({
                game.makeMove(move)
                prepTurn()
                actionInProgress = false
            }, 1500)
        } else {
            actionInProgress = false
        }
    }

    // player makes a click
    fun onClick(x: Int, y: Int) {
        if (actionInProgress || game.gameType == GameType.Bot && game.whoseTurn() == CheckersGame.BLACK) {
            return
        }

        val location = Position(x, y)
        val targetPiece = game.board.getPiece(x, y)

        // attempting to make a move
        if (selectedPiece != null && selectedPosition != null && targetPiece == null) {
            actionInProgress = true
            makeMove(location)
        } else {
            selectPiece(targetPiece, location)
            if (selectedPiece == null) {
                checkersView.highlightSelectablePieces(selectablePieces)
            }
            mHandler.postDelayed({ checkersView.refresh() }, 500)
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save the user's current game state

        savedInstanceState.putParcelable("game", game)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onPause() {
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
            game = ois.readObject() as CheckersGame
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
}
