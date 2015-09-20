package com.example.FifteenPuzzleReworked;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class FifteenPuzzle extends Activity {

    public static final int COUNT = 4;
    private static Drawable[][] drawables;
    private static String MOVES_COUNT_TEXT;
    private static Game game;
    private static Point[][] points = new Point[4][4];
    public static final boolean MIX = true;
    public static final boolean NO_MIX = false;
    private AlertDialog restartDialog;
    private TextView tvMovesCount;
    private SquareButton[][] buttons = new SquareButton[4][4];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initDrawables();
        initGame();
        initField();
        initDialog();
    }

    //Инициализация игры
    private void initGame() {
        if (game == null) {
            game = new Game();
            MOVES_COUNT_TEXT = getString(R.string.moves_count);
            points = game.getPoints();
            // restartGame(); //Перемешать
        }

    }

    // Проверка на движение и движение
    public boolean move(int x, int y) {

        if (game.checkEmpty(points[x][y].x, points[x][y].y)) return false;
        int incX = 0, incY = 0;
        label:
        {
            //Проверка на свободную клетку
            if (x != 3) {
                if (game.checkEmpty(points[x + 1][y].x, points[x + 1][y].y)) {
                    incX = 1;
                    break label;
                }
            }
            if (x != 0) {
                if (game.checkEmpty(points[x - 1][y].x, points[x - 1][y].y)) {
                    incX = -1;
                    break label;
                }
            }
            if (y != 3) {
                if (game.checkEmpty(points[x][y + 1].x, points[x][y + 1].y)) {
                    incY = 1;
                    break label;
                }
            }
            if (y != 0) {
                if (game.checkEmpty(points[x][y - 1].x, points[x][y - 1].y)) {
                    incY = -1;
                }
            }
        }
        if (incX != 0 || incY != 0) {
            game.swapPoints(x, y, incX, incY);
            updateBg(x + incX, y + incY);
            setVisible(x + incX, y + incY);
            setInvisible(x, y);
            return true;
        }
        return false;
    }

    // обновить картинку у кнопки
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateBg(int x, int y) {
        Point point = points[x][y];
        buttons[x][y].setBackground(drawables[point.x][point.y]);
    }

    //Сделать кнопку невидимой
    private void setInvisible(int x, int y) {
        buttons[x][y].setVisibility(View.INVISIBLE);
    }

    //Сделать кнопку видимой
    private void setVisible(int x, int y) {
        buttons[x][y].setVisibility(View.VISIBLE);
    }

    //Инициализировать картинки
    private void initDrawables() {
        if (drawables == null) {
            drawables = new Drawable[COUNT][COUNT];
            drawables[0][0] = getResources().getDrawable(R.drawable.bg1);
            drawables[0][1] = getResources().getDrawable(R.drawable.bg2);
            drawables[0][2] = getResources().getDrawable(R.drawable.bg3);
            drawables[0][3] = getResources().getDrawable(R.drawable.bg4);
            drawables[1][0] = getResources().getDrawable(R.drawable.bg5);
            drawables[1][1] = getResources().getDrawable(R.drawable.bg6);
            drawables[1][2] = getResources().getDrawable(R.drawable.bg7);
            drawables[1][3] = getResources().getDrawable(R.drawable.bg8);
            drawables[2][0] = getResources().getDrawable(R.drawable.bg9);
            drawables[2][1] = getResources().getDrawable(R.drawable.bg10);
            drawables[2][2] = getResources().getDrawable(R.drawable.bg11);
            drawables[2][3] = getResources().getDrawable(R.drawable.bg12);
            drawables[3][0] = getResources().getDrawable(R.drawable.bg13);
            drawables[3][1] = getResources().getDrawable(R.drawable.bg14);
            drawables[3][2] = getResources().getDrawable(R.drawable.bg15);
            drawables[3][3] = getResources().getDrawable(R.drawable.bg16);

        }
    }

    //Инициализировать поле
    private void initField() {
        buttons[0][0] = (SquareButton) findViewById(R.id.b1);
        buttons[0][1] = (SquareButton) findViewById(R.id.b2);
        buttons[0][2] = (SquareButton) findViewById(R.id.b3);
        buttons[0][3] = (SquareButton) findViewById(R.id.b4);

        buttons[1][0] = (SquareButton) findViewById(R.id.b5);
        buttons[1][1] = (SquareButton) findViewById(R.id.b6);
        buttons[1][2] = (SquareButton) findViewById(R.id.b7);
        buttons[1][3] = (SquareButton) findViewById(R.id.b8);

        buttons[2][0] = (SquareButton) findViewById(R.id.b9);
        buttons[2][1] = (SquareButton) findViewById(R.id.b10);
        buttons[2][2] = (SquareButton) findViewById(R.id.b11);
        buttons[2][3] = (SquareButton) findViewById(R.id.b12);

        buttons[3][0] = (SquareButton) findViewById(R.id.b13);
        buttons[3][1] = (SquareButton) findViewById(R.id.b14);
        buttons[3][2] = (SquareButton) findViewById(R.id.b15);
        buttons[3][3] = (SquareButton) findViewById(R.id.b16);

        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                SquareButton squareButton = buttons[i][j];
                squareButton.setOnTouchListener(getOnTouchListener(i, j));
            }
        }

        tvMovesCount = (TextView) findViewById(R.id.tv_count);
        updateField();

        //Кнопка рестарт
        findViewById(R.id.b_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame(MIX);
            }
        });
        findViewById(R.id.b_no_mix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame(NO_MIX);
            }
        });
    }

    //Запустить игру и перемешать
    private void restartGame(boolean mix) {
        setVisible(game.getEmptyPoint().correctX, game.getEmptyPoint().correctY);
        game.restart(mix);
        updateField();
    }

    //Обновить картинки у всех кнопок
    private void updateField() {
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < COUNT; j++) {
                updateBg(i, j);
            }
        }
        setInvisible(game.getEmptyPoint().correctX, game.getEmptyPoint().correctY);
        updateMovesCount();
    }

    // Обновить на экране количество ходов
    private void updateMovesCount() {
        tvMovesCount.setText(MOVES_COUNT_TEXT + game.getMovesCount());
    }


    //Инициализация диалога
    public void initDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher)
                .setCancelable(true).
                setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame(MIX);
                    }
                }).setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateMovesCount();
                        dialog.cancel();
                    }
                });
        restartDialog = builder.create();
    }

    // обработчик нажатий на кнопки
    private View.OnTouchListener getOnTouchListener(int x, int y) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                SquareButton squareButton1 = buttons[x][y];
                if (event.getAction() == MotionEvent.ACTION_UP) {// При отпускании
                    squareButton1.setAlpha(1);
                    if (move(x, y)) {
                        game.incMovesCount();
                        updateMovesCount();
                        if (game.checkEndGame()) {
                            restartDialog.setTitle(getString(R.string.result) + " " + game.getMovesCount() + "\n" + getString(R.string.restart) + "?");
                            restartDialog.show();
                        }

                    }

                } else if (event.getAction() == MotionEvent.ACTION_DOWN) { //При нажатии
                    squareButton1.setAlpha(0.7f);

                }
                return false;
            }
        };
    }
}
