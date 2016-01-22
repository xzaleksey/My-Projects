package com.valyakinaleksey.customviewexample;

import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class RoundButton extends View {
    private static final String TAG = "RoundButton";
    public static final int DEFAULT_RADIUS = 28;
    public static final int MINI_RADIUS = 14;
    private int defaultColor;
    private String title;
    private int valueColor;
    private Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int defaultSize;
    private int radius;
    private int parentWidth;
    private int parentHeight;


    public RoundButton(Context context) {
        super(context);
        init();
    }

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RoundButton, 0, 0);
        title = a.getString(R.styleable.RoundButton_titleText);
        valueColor = a.getColor(R.styleable.RoundButton_valueColor,
                context.getResources().getColor(R.color.colorAccent));
        radius = dpToPx(a.getInt(R.styleable.RoundButton_radius, DEFAULT_RADIUS));
        a.recycle();
        init();
    }

    private void init() {
        defaultColor = getResources().getColor(R.color.colorAccent);
        defaultSize = radius * 2;

        shapePaint.setStyle(Paint.Style.FILL);
        shapePaint.setColor(valueColor == 0 ? defaultColor : valueColor);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.default_text_size));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(defaultSize, widthSize);
        } else {
            //Be whatever you want
            width = defaultSize;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(defaultSize, heightSize);
        } else {
            //Be whatever you want
            height = defaultSize;
        }
        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((View) getParent()).setOnDragListener(new MyDragListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int x = getWidth();
        int y = getHeight();
        canvas.drawCircle(x / 2, y / 2, radius, shapePaint);
        if (title != null) {
            canvas.drawText(title, radius, radius + textPaint.getTextSize() / 2, textPaint);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    public int getValueColor() {
        return valueColor;
    }

    public void setValueColor(int valueColor) {
        this.valueColor = valueColor;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (Math.sqrt(Math.pow(x - radius, 2) + Math.pow(y - radius, 2)) <= radius) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(this);
                    this.startDrag(data, shadowBuilder, this, 0);
                    this.setVisibility(View.INVISIBLE);
                    break;
            }
        } else {
            return false;
        }
        return true;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    private class MyDragListener implements OnDragListener {

//        Drawable enterShape = getResources().getDrawable(android.R.drawable.star_on);
//        Drawable normalShape = getResources().getDrawable(android.R.drawable.star_off);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
//                    v.setBackground(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
//                    v.setBackground(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View view = (View) event.getLocalState();
                    view.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    float x = event.getX();
                    float y = event.getY();
                    layoutParams.leftMargin = (int) (x - radius);
                    layoutParams.topMargin = (int) (y - radius);
                    view.setLayoutParams(layoutParams);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
//                    v.setBackground(normalShape);
                default:
                    break;
            }
            return true;
        }
    }
}
