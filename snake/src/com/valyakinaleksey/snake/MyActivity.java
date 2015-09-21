package com.valyakinaleksey.snake;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.valyakinaleksey.snake.dialogs.EndGameDialog;
import com.valyakinaleksey.snake.game.Cell;
import com.valyakinaleksey.snake.game.Direction;
import com.valyakinaleksey.snake.game.Game;
import com.valyakinaleksey.snake.game.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.valyakinaleksey.snake.game.Direction.*;
import static com.valyakinaleksey.snake.game.State.*;

public class MyActivity extends Activity {

    public static final int COL_COUNT = 10;
    private static final String ROW_COUNT_STR = "ROW_COUNT";
    private static final String CELL_WIDTH = "CELL_WIDTH";
    private static final Map<State, Integer> colors = new HashMap<>();
    public static Game game;
    private static int ROW_COUNT;
    public Direction currentDirection = LEFT;
    public View btnPlay;
    private int cellWidth;
    private int currentResult;
    private SharedPreferences sPref;
    private TextView[][] tvCells;
    private TextView tv_Result;
    private EndGameDialog endGameDialog;
    private Handler handler;
    private Animation btn_animation;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initColors();
        initGame();
        initField();
        prepareField();
        initDialog();
    }

    private void initColors() {
        if (colors.isEmpty()) {
            colors.put(EMPTY, getResources().getColor(R.color.grey));
            colors.put(SNAKE, getResources().getColor(android.R.color.black));
            colors.put(FOOD, getResources().getColor(android.R.color.holo_red_light));
        }
    }

    private void initGame() {
        handler = new MyHandler(this);
        initParams();
        if (game == null) {
            game = new Game(ROW_COUNT);
        }
    }

    private void initParams() {
        sPref = getPreferences(MODE_PRIVATE);
        if (sPref.contains(CELL_WIDTH)) {
            ROW_COUNT = sPref.getInt(ROW_COUNT_STR, 0);
            cellWidth = sPref.getInt(CELL_WIDTH, 0);
        } else {
            int mActionBarSize = (int) this.getTheme().obtainStyledAttributes(
                    new int[]{android.R.attr.actionBarSize}).getDimension(0, 0);
            DisplayMetrics metricsB = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metricsB);
            cellWidth = metricsB.widthPixels / COL_COUNT;
            ROW_COUNT = (metricsB.heightPixels - mActionBarSize) / cellWidth - 3;
            saveData();
        }
    }

    private void initField() {
        TableLayout tlField = (TableLayout) findViewById(R.id.tl_field);
        tvCells = new TextView[ROW_COUNT][COL_COUNT];
        tv_Result = (TextView) findViewById(R.id.tv_result);
        btnPlay = findViewById(R.id.btn_pause_play);
        btn_animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        int wrapContent = TableRow.LayoutParams.WRAP_CONTENT;

        for (int i = 0; i < ROW_COUNT; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    wrapContent));
            for (int j = 0; j < COL_COUNT; j++) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new TableRow.LayoutParams(wrapContent, wrapContent));
                setParams(textView);
                textView.setBackgroundColor(colors.get(EMPTY));
                tableRow.addView(textView);
                tvCells[i][j] = textView;
            }
            tlField.addView(tableRow);
        }
        setParams((TextView) findViewById(R.id.btn_down));
        setParams((TextView) findViewById(R.id.btn_left));
        setParams((TextView) findViewById(R.id.btn_right));
        setParams((TextView) findViewById(R.id.btn_up));
        setParams((TextView) findViewById(R.id.tv_result));
        setParams((TextView) findViewById(R.id.tv_result_label));
        btnPlay.setOnClickListener(new View.OnClickListener() {
            boolean pause = false;

            @Override
            public void onClick(View v) {
                if (game.isGameLost()) {
                    pause = true;
                    restartGame();
                }
                if (!pause) {
                    v.setBackground(getResources().getDrawable(R.drawable.ic_action_playback_play));
                    pause = true;
                    game.stop();
                } else {
                    v.setBackground(getResources().getDrawable(R.drawable.ic_action_playback_pause));
                    pause = false;
                    game.start(handler);
                }
            }
        });
    }

    private void setParams(TextView textView) {
        textView.setWidth(cellWidth);
        textView.setHeight(cellWidth);
    }

    private void prepareField() {
        game.updateAll();
        List<Cell> cellsToUpdate = game.getCellsToUpdate();
        for (final Cell cell : cellsToUpdate) {
            final TextView textView = tvCells[cell.getRow()][cell.getCol()];
            textView.setBackgroundColor(colors.get(cell.getState()));
        }
    }

    private void initDialog() {
        endGameDialog = new EndGameDialog();
        endGameDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case Dialog.BUTTON_POSITIVE:
                        restartGame();
                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        btnPlay.setBackground(getResources().getDrawable(R.drawable.ic_action_playback_play));
                        game.setGameLost(true);
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        game.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.start(handler);
    }


    private void saveData() {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(ROW_COUNT_STR, ROW_COUNT);
        ed.putInt(CELL_WIDTH, cellWidth);
        ed.apply();
    }

    private void restartGame() {
        game.reset(handler);
        prepareField();
        currentResult = 0;
        tv_Result.setText(R.string.zero);
    }

    public void updateField() {
        List<Cell> cellsToUpdate = game.getCellsToUpdate();
        for (final Cell cell : cellsToUpdate) {
            final TextView textView = tvCells[cell.getRow()][cell.getCol()];
            State state = cell.getState();
            if (state != EMPTY) {
                textView.setBackgroundColor(colors.get(state));
            }
            startAnimation(cell, textView, state);
            if (currentResult < Game.getResult()) {
                currentResult = Game.getResult();
                tv_Result.setText("" + currentResult);
            }
        }
    }

    private void startAnimation(final Cell cell, final TextView textView, State state) {
        Animation animation = getAnimation(cell, state);
        if (state == EMPTY) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    textView.setBackgroundColor(colors.get(EMPTY));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        textView.startAnimation(animation);
    }

    private Animation getAnimation(Cell cell, State state) {
        float fromX = 1;
        float toX = 1;
        float pivotX = 1;
        float fromY = 1;
        float toY = 1;
        float pivotY = 1;
        switch (state) {
            case FOOD:
                pivotX = 0.5f;
                pivotY = 0.5f;
                fromX = 0;
                fromY = 0;
                break;
            case EMPTY:
                Cell nextCell = game.getSnake().getBody().peekLast();
                int row = cell.getRow();
                int col = cell.getCol();
                int nextCellRow = nextCell.getRow();
                int nextCellCol = nextCell.getCol();
                if (nextCellRow == row) {
                    toX = 0;
                    pivotX = getPivot(col, nextCellCol, COL_COUNT);
                } else {
                    toY = 0;
                    pivotY = getPivot(row, nextCellRow, ROW_COUNT);
                }
                break;
            case SNAKE:
                switch (currentDirection) {
                    case RIGHT:
                        fromX = 0;
                        pivotX = 0;
                        break;
                    case LEFT:
                        fromX = 0;
                        break;
                    case UP:
                        fromY = 0;
                        break;
                    case DOWN:
                        fromY = 0;
                        pivotY = 0;
                        break;
                }
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, toX, // Start and end values for the X axis scaling
                fromY, toY, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, pivotX, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, pivotY); // Pivot point of Y scaling
        scaleAnimation.setDuration(500);
        return scaleAnimation;
    }

    private float getPivot(int current, int next, int count) {
        if (next == 0 && current == count - 1) {
            next = count;
        } else if (current == 0 && next == count - 1) {
            current = count;
        }
        if (current > next) {
            return 0;
        }
        return 1;
    }


    public void setDirection(View view) {
        view.startAnimation(btn_animation);
        Direction newDirection = LEFT;
        switch (view.getId()) {
            case R.id.btn_left:
                newDirection = LEFT;
                break;
            case R.id.btn_right:
                newDirection = RIGHT;
                break;
            case R.id.btn_up:
                newDirection = UP;
                break;
            case R.id.btn_down:
                newDirection = DOWN;
                break;
        }
        Direction prevDirection = game.checkDirection();
        if (newDirection == prevDirection || newDirection == LEFT && prevDirection == RIGHT || prevDirection == LEFT && newDirection == RIGHT || newDirection == UP && prevDirection == DOWN || newDirection == DOWN && prevDirection == UP) {
            return;
        }
        game.setDirection(newDirection);
    }

    public void gameRestartDialogShow() {
        endGameDialog.show(getFragmentManager(), "endGameDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restart:
                gameRestartDialogShow();
                break;
            case R.id.about:
                Toast.makeText(this, "Потоки наше все:)", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
