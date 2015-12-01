package com.valyakinaleksey.a2048updated;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.avast.android.dialogs.iface.ISimpleDialogListener;
import com.valyakinaleksey.a2048updated.custom_views.SquareTextView;
import com.valyakinaleksey.a2048updated.game.Cell;
import com.valyakinaleksey.a2048updated.game.Game;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.valyakinaleksey.a2048updated.Constants.GAME_RECORD;
import static com.valyakinaleksey.a2048updated.Constants.GAME_SCORE;
import static com.valyakinaleksey.a2048updated.Constants.LOG_TAG;
import static com.valyakinaleksey.a2048updated.Constants.MAX_COL_ROW;
import static com.valyakinaleksey.a2048updated.Constants.MIN_COL_ROW;
import static com.valyakinaleksey.a2048updated.Constants.SAVED_VALUES;

public class MainActivityFragment extends Fragment implements ISimpleDialogListener {
    private static final int REQUEST_CODE_RESTART = 1;

    private static final Map<CharSequence, Integer> COLORS = new HashMap<>();
    private static Game game;
    @Bind(R.id.field)
    TableLayout field;
    @Bind(R.id.tv_score)
    TextView tvScore;
    @Bind(R.id.tv_record)
    TextView tvRecord;
    @BindColor(R.color.colorText)
    int colorText;
    @BindString(R.string.record)
    String record;
    @BindString(R.string.score)
    String score;
    @BindString(R.string.max_count)
    String colRowCountKey;
    @BindString(R.string.default_max_count)
    String maxColRow;
    @BindString(R.string.orientation)
    String orientationKey;
    private TextView[][] textViews;
    private GestureDetector gestureDetector;
    private Updater updater;
    private SharedPreferences sp;
    private int currentColRowCount;
    private String[] orientations;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_main, null);
        ButterKnife.bind(this, viewGroup);
        initBaseValues();
        initGame();
        initField();
        fillField();
        initOnTouchListener(viewGroup);
        setHasOptionsMenu(true);
        return viewGroup;
    }

    private void initBaseValues() {
        initMaxColRow();
        initColors();
        orientations = getResources().getStringArray(R.array.entry_orientation);
        updater = new Updater(this);
    }

    private void initMaxColRow() {
        MAX_COL_ROW = Integer.valueOf(maxColRow);
        currentColRowCount = getMaxColRowCount();
        if (currentColRowCount != 0 && currentColRowCount != MAX_COL_ROW) {
            MAX_COL_ROW = currentColRowCount;
        }
    }

    private void fillField() {
        for (int i = MIN_COL_ROW; i <= MAX_COL_ROW; i++) {
            for (int j = MIN_COL_ROW; j <= MAX_COL_ROW; j++) {
                fillTextView(i - 1, j - 1);
            }
        }
        updateScoreAndRecord();
    }

    private void fillTextView(int i, int j) {
        Cell cell = game.getCells()[i][j];
        String stringValue = getStringValue(cell);
        textViews[i][j].setText(stringValue);
        setBgColor(textViews[i][j], stringValue);
        if (cell.isChanged()) {
            textViews[i][j].startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.new_value));
        }
    }

    @NonNull
    private String getStringValue(Cell cell) {
        int value = cell.getValue();
        String s;
        if (value == 0) {
            s = "";
        } else {
            s = String.valueOf(value);
        }
        return s;
    }

    private void initGame() {
        String[] values = sp.getString(SAVED_VALUES, "").split(" ");
        if (values.length > 1) {
            MAX_COL_ROW = (int) Math.sqrt(values.length);
        }
        if (game == null) {
            game = new Game();
            if (values.length == 1) {
                game.initFirstCells();
            } else {
                loadData(values);
            }
        }
    }

    private void loadData(String[] values) {
        game.loadData(values);
        Log.d(LOG_TAG, "Score " + sp.getInt(GAME_SCORE, 0));
        Log.d(LOG_TAG, "Record " + sp.getInt(GAME_RECORD, 0));
        game.setScore(sp.getInt(GAME_SCORE, 0));
        game.setRecord(sp.getInt(GAME_RECORD, 0));
        updateScoreAndRecord();
    }

    private void initColors() {
        if (COLORS.size() == 0) {
            String[] strings = getResources().getStringArray(R.array.values);
            int[] colors = getResources().getIntArray(R.array.colors);
            for (int i = 0; i < strings.length; i++) {
                COLORS.put(strings[i], colors[i]);
            }
        }
    }


    private void initField() {
        textViews = new TextView[MAX_COL_ROW][MAX_COL_ROW];
        field.removeAllViewsInLayout();
        field.setWeightSum(MAX_COL_ROW);
        for (int i = MIN_COL_ROW; i <= MAX_COL_ROW; i++) {
            TableRow row = new TableRow(getContext());
            TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1.0f);
            row.setWeightSum(MAX_COL_ROW);
            for (int j = MIN_COL_ROW; j <= MAX_COL_ROW; j++) {
                SquareTextView squareTextView = getSquareTextView(i, j);
                row.addView(squareTextView);
            }
            field.addView(row, rowLp);
        }
        //FixMe Ширина все равно при альбомной ориентации не идеальна(
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            final ViewTreeObserver observer = field.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    field.getLayoutParams().width = field.getHeight();
                    Log.d(LOG_TAG, "height" + field.getHeight());
                    Log.d(LOG_TAG, "width" + field.getLayoutParams().width);
                    field.requestLayout();
                    field.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    @NonNull
    private SquareTextView getSquareTextView(int i, int j) {
        SquareTextView squareTextView = new SquareTextView(getContext(), null);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f);
        lp.setMargins(5, 5, 5, 5);
        squareTextView.setLayoutParams(lp);
        squareTextView.setGravity(Gravity.CENTER);
        int styleId;
        if (MAX_COL_ROW < 5) {
            styleId = android.R.style.TextAppearance_DeviceDefault_Large;
        } else if (MAX_COL_ROW < 8) {
            styleId = android.R.style.TextAppearance_DeviceDefault_Medium;
        } else if (MAX_COL_ROW < 10) {
            styleId = android.R.style.TextAppearance_DeviceDefault_Small;
        } else {
            styleId = R.style.TextAppearanceSmall;
        }
        squareTextView.setTextAppearance(getContext(), styleId);
        squareTextView.setTypeface(null, Typeface.BOLD);
        squareTextView.setTextColor(colorText);
        textViews[i - 1][j - 1] = squareTextView;
        return squareTextView;
    }

    @Override
    public void onNegativeButtonClicked(int requestCode) {

    }

    @Override
    public void onNeutralButtonClicked(int requestCode) {

    }

    @Override
    public void onPositiveButtonClicked(int requestCode) {
        if (requestCode == REQUEST_CODE_RESTART) {
            restart();
        }
    }

    private void restart() {
        if (currentColRowCount != textViews[0].length) {
            MAX_COL_ROW = currentColRowCount;
            initField();
        }
        game.restartGame();
        fillField();
    }

    private void updateScoreAndRecord() {
        tvRecord.setText(String.format(record, game.getRecord()));
        tvScore.setText(String.format(score, game.getScore()));
    }

    private void createNewCell() {
        Cell cell = game.getCellInitialized();
        TextView textView = textViews[cell.getRowNumber() - 1][cell.getColumnNumber() - 1];
        fillTextView(cell.getRowNumber() - 1, cell.getColumnNumber() - 1);
        textView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.new_cell));
    }

    private void initOnTouchListener(View view) {
        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        view.setOnTouchListener(gestureListener);
    }

    private void setBgColor(TextView textView, CharSequence value) {
        textView.setBackgroundColor(COLORS.get(value));
//        Drawable background = textView.getBackground().getCurrent();
//        if (background instanceof ShapeDrawable) {
//            ((ShapeDrawable) background).getPaint().setColor(COLORS.get(value));
//        } else if (background instanceof GradientDrawable) {
//            ((GradientDrawable) background).setColor(COLORS.get(value));
//        }
//        background.invalidateSelf();

    }

    private void colRowCountDialogShow() {
        SimpleDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setTitle(R.string.restart)
                .setMessage(R.string.col_row_count_dialog_message)
                .setPositiveButtonText(R.string.restart)
                .setNegativeButtonText(R.string.cancel).show().setTargetFragment(this, REQUEST_CODE_RESTART);
    }

    private void gameLostDialogShow() {
        SimpleDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setTitle(R.string.game_over)
                .setMessage(tvScore.getText() + "\n" + tvRecord.getText())
                .setPositiveButtonText(R.string.restart)
                .setNegativeButtonText(R.string.cancel).show().setTargetFragment(this, REQUEST_CODE_RESTART);
    }

    private void restartDialogShow() {
        SimpleDialogFragment.createBuilder(getContext(), getFragmentManager())
                .setTitle(R.string.restart)
                .setPositiveButtonText(R.string.ok)
                .setNegativeButtonText(R.string.cancel).show().setTargetFragment(this, REQUEST_CODE_RESTART);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_refresh).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartDialogShow();
            }
        });
        menu.findItem(R.id.action_undo).getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.undo();
                fillField();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
        game.setUpdater(updater);
    }

    private void updatePreferences() {
        currentColRowCount = getMaxColRowCount();
        String orientation = sp.getString(orientationKey, orientations[0]);
        if (orientation.equals(orientations[0])) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (orientation.equals(orientations[1])) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        if (currentColRowCount != 0 && currentColRowCount != MAX_COL_ROW) {
            colRowCountDialogShow();
        }
    }

    private int getMaxColRowCount() {
        String currentColRowCount = sp.getString(colRowCountKey, "");
        if (currentColRowCount.equals("")) {
            return 0;
        }
        return Integer.valueOf(currentColRowCount);
    }

    @Override
    public void onPause() {
        super.onPause();
        game.setUpdater(null);
        saveData();
    }

    private void saveData() {
        Cell[][] cells = game.getCells();
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(GAME_SCORE, game.getScore());
        editor.putInt(GAME_RECORD, game.getRecord());
        Log.d(LOG_TAG, "score saved: " + game.getScore());
        Log.d(LOG_TAG, "record saved: " + game.getRecord());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < MAX_COL_ROW; i++) {
            for (int j = 0; j < MAX_COL_ROW; j++) {
                stringBuilder.append(cells[i][j].getValue()).append(" ");
            }
        }
        editor.putString(SAVED_VALUES, stringBuilder.toString().trim());
        editor.apply();
    }

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
                    if (movesMade) {
                        updateScoreAndRecord();
                        if (game.getFreePositionsCount() > 0) {
                            createNewCell();
                        }
                        if (game.checkEndGame()) {
                            Log.d(LOG_TAG, "Free cells: " + game.getFreePositionsCount());
                            Log.d(LOG_TAG, "lost");
                            gameLostDialogShow();
                        }
                    }
                }

                @Override
                protected Void doInBackground(Void... params) {
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

    public class Updater {
        private final WeakReference<MainActivityFragment> mFragment;

        public Updater(MainActivityFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        public void update(final Cell[] cells) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (final Cell cell : cells) {
                        mFragment.get().fillTextView(cell.getRowNumber() - 1, cell.getColumnNumber() - 1);
                    }
                }
            });
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

