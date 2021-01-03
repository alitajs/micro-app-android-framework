package com.alitajs.micro.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.alitajs.micro.R;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class LoadingDialog extends Dialog {

    private Context mContext;

    MaterialProgressBar progressBar;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setDimAmount(0f);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCanceledOnTouchOutside(false);
        progressBar = findViewById(R.id.indeterminate_progress_library);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void initColorRes(int color){
        progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,color)));
    }

    public void initColor(int color){
        progressBar.setProgressTintList(ColorStateList.valueOf(color));
    }
}
