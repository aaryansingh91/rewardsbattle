//For main loading dialog or pre-loader
package com.app.rewardsbattle.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.app.rewardsbattle.R;

public class LoadingDialog extends Dialog {
    public LoadingDialog( Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.progressdialog);
    }
}
