package com.alitajs.micro.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 基础Fragment 基本操作.
 *
 */
public abstract class BaseFragment extends Fragment {
    /** 日志. */
    public static final String TAG = BaseFragment.class.getSimpleName();



    /** ButterKnife绑定视图 */
    protected View mRootView;

    protected Activity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = View.inflate(getActivity(),provideViewLayoutId(),null);
        getExtraDatas();
        mActivity = getActivity();
        setViews();
        setListeners();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * 获取 Extra 数据.
     */
    protected void getExtraDatas() {
    }
    /**
     * 布局文件layout的id.
     *
     * @return layout 对应的id.
     */
    protected abstract int provideViewLayoutId();

    /**
     * 视图的设置操作.
     */
    protected abstract void setViews();

    /**
     * 涉及到监听器的设置操作.
     */
    protected abstract void setListeners();

    /**
     * 释放相关操作.
     */
    protected void release(){};

}
