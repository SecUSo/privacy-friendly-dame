package de.tu_darmstadt.informatik.secuso.privacyfriendlydame.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.LinkedList;

import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.controller.GameController;
import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.game.GameCell;

/**
 * Created by Marc on 18.11.2017.
 */

public class DameBoardLayout extends View {

    GameController gameController;
    private int boardWidth, boardHeight, cellWidth, cellHeight;
    Paint blackPaint, whitePaint;

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
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
    }

    public void setGameController(GameController gc) {
        gameController = gc;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boardHeight = View.MeasureSpec.getSize((heightMeasureSpec));
        boardWidth = View.MeasureSpec.getSize((widthMeasureSpec));
        cellWidth = boardWidth / 8;
        cellHeight = boardHeight / 8;

        setMeasuredDimension(boardWidth, boardHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBoard(canvas);
    }

    private void drawBoard(Canvas canvas) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                drawCell(canvas, gameController.getCellAt(i, j), i, j);
            }
        }
    }

    private void drawCell(Canvas canvas, GameCell cell, int i, int j) {
        float left = i * cellWidth;
        float top = j * cellHeight;
        if (cell.isBlack())
            canvas.drawRect(left, top, left + cellWidth, top + cellHeight, blackPaint);
        else
            canvas.drawRect(left, top, left + cellWidth, top + cellHeight, whitePaint);
    }
}

