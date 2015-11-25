package com.valyakinaleksey.followplan.followplan2.followplan.help_classes;

import android.content.Context;
import com.valyakinaleksey.followplan.followplan2.followplan.R;

public class Colors {
    static int[] pickerColors;

    public static int[] getPickerColors(Context context) {
        if (pickerColors == null) {
            pickerColors = context.getResources().getIntArray(R.array.plan_colors);
        }
        return pickerColors;
    }
}
