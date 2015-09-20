package com.valyakinaleksey.simple2048;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyActivity extends AppCompatActivity {
    public static final String RESULT = "Result";
    public static final String RECORD = "Record";
    public static final String DATA = "Data";
    private static final Map<CharSequence, Integer> COLORS = new HashMap<>();
    private static final int[] IDS = new int[]{R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4, R.id.tv5, R.id.tv6, R.id.tv7, R.id.tv8, R.id.tv9, R.id.tv10, R.id.tv11, R.id.tv12, R.id.tv13, R.id.tv14, R.id.tv15, R.id.tv16};
    private static final int COUNT = 16;
    private static Game game;

    private AlertDialog alertDialog;
    private SharedPreferences sPref;
    private GestureDetector gestureDetector;

    private RelativeLayout relativeLayout;
    private TextView[] textViews = new TextView[COUNT];
    private TextView tvResult, tvRecord;

    SoundPool soundPool;
    int soundIdShot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initField(getParam());
        initGame();
        initOnTouchListener();
        initDialog();
        initSound();
    }

    private void initSound() {
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        soundIdShot = soundPool.load(this, R.raw.shot, 1);
    }

    //Сохранить результат при паузе
    @Override
    protected void onPause() {
        super.onPause();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < COUNT; i++) {
            CharSequence value = game.getCellValue(i);
            stringBuilder.append(value.equals("") ? 0 : value);
            stringBuilder.append(" ");
        }
        saveData(stringBuilder.toString());
    }

    //Сохранить состояние
    void saveData(String s) {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(RESULT, game.getResult());
        ed.putString(RECORD, game.getRecord());
        ed.putString(DATA, s);
        ed.apply();
    }

    //Загрузить состояние
    void loadData() {
        game.setResult(sPref.getString(RESULT, ""));
        tvResult.setText(game.getResult());
        game.setRecord(sPref.getString(RECORD, ""));
        tvRecord.setText(game.getRecord());
        String[] dataArr = sPref.getString(DATA, "").split(" ");
        game.setCellsValues(dataArr);
        restoreValues();
    }

    //Инициализация игры
    private void initGame() {
        sPref = getPreferences(MODE_PRIVATE);
        if (null == game) {
            game = new Game();
            if (COLORS.size() == 0) {
                String[] strings = getResources().getStringArray(R.array.values);
                int[] colors = getResources().getIntArray(R.array.colors);
                for (int i = 0; i < strings.length; i++) {
                    COLORS.put(strings[i], colors[i]);
                }
                if (sPref.getString(DATA, "").equals("")) {
                    createNewCell();
                    createNewCell();
                } else {
                    loadData();
                }
            }
        } else {
            restoreValues();
        }
        game.setUpdater(new Updater(textViews));
    }

    //Восстановить значения
    private void restoreValues() {
        for (int i = 0; i < COUNT; i++) {
            textViews[i].setText(game.getCellValue(i));
            setBgColor(textViews[i], textViews[i].getText());
        }
        tvResult.setText(game.getResult());
        tvRecord.setText(game.getRecord());
    }

    //Добавление клетки
    private void createNewCell() {
        int id = game.newCell();
        TextView textView = textViews[id];
        CharSequence value = game.getCellValue(id);
        textView.setText(value);
        setBgColor(textView, value);
        textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
    }

    //Устанавливает цвет для TextView
    private void setBgColor(TextView textView, CharSequence value) {
        //Drawable background = textView.getBackground();
        //background.setColorFilter(COLORS.get(value), PorterDuff.Mode.SRC_IN);
        ((GradientDrawable) textView.getBackground().getCurrent()).setColor(COLORS.get(value));
    }

    // Задает ширину и высоту TextView
    private void initField(int value) {
        relativeLayout = (RelativeLayout) findViewById(R.id.rlMain);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvRecord = (TextView) findViewById(R.id.tv_record);
        for (int i = 0; i < COUNT; i++) {
            textViews[i] = (TextView) findViewById(IDS[i]);
            textViews[i].setWidth(value);
            textViews[i].setHeight(value);
        }
        setLinearLayoutDimension(R.id.ll_result, value);
        setLinearLayoutDimension(R.id.ll_record, value);
    }

    //Задать параметры для блоков рекорда и результата
    private void setLinearLayoutDimension(int id, int param) {
        LinearLayout linearLayout;
        linearLayout = (LinearLayout) findViewById(id);
        linearLayout.getLayoutParams().width = param;
        linearLayout.getLayoutParams().height = param;
        linearLayout.requestLayout();
    }

    //Получает параметр Ширины и высоты TextView с учетом рахмера экрана
    private int getParam() {
        int param;
        int mActionBarSize = (int) this.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}).getDimension(0, 0);
        DisplayMetrics metricsB = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metricsB);
        int height = metricsB.heightPixels - mActionBarSize - 68;
        int width = metricsB.widthPixels - 72;
        param = height > width ? width : height;
        return (int) (param * 0.25);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Для рестарта
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restart:
                alertDialog.setTitle(getString(R.string.reset));
                alertDialog.setMessage(getDialogMessage());
                alertDialog.show();
                break;
            case R.id.about:
                new AlertDialog.Builder(this).setTitle(R.string.about_program)
                        .setMessage(getString(R.string.about_program_info))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showToast(getString(R.string.good_luck));
                            }
                        })
                        .create()
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Перезапуск игры
    private void restartGame() {
        game.resetGame();
        restoreValues();
        createNewCell();
        createNewCell();
    }

    //Инициализирует обработку жестов
    private void initOnTouchListener() {
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        relativeLayout.setOnTouchListener(gestureListener);
    }

    @NonNull
    private String getDialogMessage() {
        return "Счет " + game.getResult() + "\n" + "Рекорд " + game.getRecord();
    }

    //Инициализация диалога
    public void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher)
                .setCancelable(true).
                setPositiveButton(getString(R.string.restart), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                }).setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialog = builder.create();
    }

    private void showToast(CharSequence charSequence) {
        Toast.makeText(this, charSequence, Toast.LENGTH_SHORT).show();
    }

    public class Updater {
        TextView[] textViews;

        public Updater(TextView[] textViews) {
            this.textViews = textViews;
        }

        //Обновить значения TextView
        public void updateMoved(ArrayList<Integer> indexes) {
            final Integer[] arr = new Integer[indexes.size()];
            indexes.toArray(arr);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Integer id : arr) {
                        CharSequence value = game.getCellValue(id);
                        textViews[id].setText(value);

                        if (COLORS.containsKey(value)) {
                            setBgColor(textViews[id], value);
                        }
                    }
                }
            });
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Обработчик жестов
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            final float x1 = e1.getX(), x2 = e2.getX(), y1 = e1.getY(), y2 = e2.getY();
            final float difX = Math.abs(x2 - x1), difY = Math.abs(y2 - y1);
            new AsyncTask<Void, Void, Void>() {
                boolean movesMade;

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    //Если движения были сделаны, создать клетку и проверить на конец игры
                    if (movesMade) {
                        soundPool.play(soundIdShot, 1, 1, 0, 0, 1);
                        tvResult.setText(game.getResult());
                        if (game.checkRecord()) {
                            tvRecord.setText(game.getRecord());
                        }
                        if (game.getFreePositionsCount() > 0)
                            createNewCell();
                        if (game.checkEndGame()) {
                            tvRecord.setText(game.getRecord());
                            alertDialog.setTitle(getString(R.string.lost));
                            alertDialog.setMessage(getDialogMessage());
                            alertDialog.show();
                        }
                    }

                }
//fixme верстка портрет. padding bottom не работает у рамки

                @Override
                protected Void doInBackground(Void... params) {
                    //Проверка на жесты:вправо, вверх, вниз влево
                    if (difX > difY) {
                        if (x2 - x1 > 0) {
                            movesMade = game.moveRight();
                        } else
                            movesMade = game.moveLeft();
                    } else {
                        if (y2 - y1 > 0) {
                            movesMade = game.moveDown();

                        } else
                            movesMade = game.moveUp();
                    }
                    return null;
                }
            }.execute();
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
