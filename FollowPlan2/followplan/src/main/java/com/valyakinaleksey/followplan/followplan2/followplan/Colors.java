package com.valyakinaleksey.followplan.followplan2.followplan;

import android.content.Context;

public class Colors {
    static int[] pickerColors;

    public static int[] getPickerColors(Context context) {
        if (pickerColors == null) {
            pickerColors = context.getResources().getIntArray(R.array.plan_colors);
        }
        return pickerColors;
    }
}
