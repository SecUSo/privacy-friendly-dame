package org.secuso.privacyfriendlydame.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.secuso.privacyfriendlydame.controller.GameController;
import org.secuso.privacyfriendlydame.game.GameCell;

/**
 * Created by Marc on 18.11.2017.
 */

public class DameBoardLayout extends View {

    GameController gameController;
    private int boardWidth, cellWidth;
    Paint boardBlackPaint, boardWhitePaint, pieceRedPaint, pieceWhitePaint, pieceBorderPaint, pieceBlueSelectedPaint;

    public DameBoardLayout(Context context) {
        super(context);

        init(null);
    }

    public DameBoardLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public DameBoardLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs);
    }

    public DameBoardLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(attrs);
    }

    private void init(@Nullable AttributeSet set) {
        boardBlackPaint = new Paint();
        boardBlackPaint.setColor(Color.BLACK);
        boardWhitePaint = new Paint();
        boardWhitePaint.setColor(Color.WHITE);

        pieceWhitePaint = new Paint();
        pieceWhitePaint.setColor(Color.WHITE);

        pieceRedPaint = new Paint();
        pieceRedPaint.setColor(Color.RED);

        pieceBorderPaint = new Paint();
        pieceBorderPaint.setColor(Color.BLACK);
        pieceBorderPaint.setStyle(Paint.Style.STROKE);
        pieceBorderPaint.setStrokeWidth(2.0f);

        pieceBlueSelectedPaint = new Paint();
        pieceBlueSelectedPaint.setColor(Color.BLUE);
        pieceBlueSelectedPaint.setStyle(Paint.Style.FILL);

    }

    public void setGameController(GameController gc) {
        gameController = gc;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (View.MeasureSpec.getSize((heightMeasureSpec)) < View.MeasureSpec.getSize((widthMeasureSpec)))
            boardWidth = View.MeasureSpec.getSize((heightMeasureSpec));
        else
            boardWidth = View.MeasureSpec.getSize((widthMeasureSpec));

        cellWidth = boardWidth / 8;


        setMeasuredDimension(boardWidth, boardWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBoard(canvas);
    }

    private void drawBoard(Canvas canvas) {
        GameCell currentCell = null;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                currentCell = gameController.getCellAt(i, j);
                drawCell(canvas, currentCell, i, j);
                if (currentCell.hasPiece())
                    drawPiece(canvas, currentCell, i, j);
            }
        }
    }

    private void drawCell(Canvas canvas, GameCell cell, int i, int j) {
        float left = i * cellWidth;
        float top = j * cellWidth;
        if (cell.isBlack())
            canvas.drawRect(left, top, left + cellWidth, top + cellWidth, boardBlackPaint);
        else
            canvas.drawRect(left, top, left + cellWidth, top + cellWidth, boardWhitePaint);
    }

    private void drawPiece(Canvas canvas, GameCell cell, int i, int j) {
        float cx = i * cellWidth + cellWidth / 2;
        float cy = j * cellWidth + cellWidth / 2;
        float radius = cellWidth * 0.45f;

        if (cell.getPiece().isSelected()) {
            canvas.drawCircle(cx, cy, radius, pieceBlueSelectedPaint);
        }
        else {
            if (cell.getPiece().isRed())
                canvas.drawCircle(cx, cy, radius, pieceRedPaint);
            else
                canvas.drawCircle(cx, cy, radius, pieceWhitePaint);
        }
        canvas.drawCircle(cx, cy, radius, pieceBorderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) (event.getX() / cellWidth);
            int y = (int) (event.getY() / cellWidth);

            gameController.play(x, y);
        }
        invalidate();
        return true;
    }


}

