package com.sjl.deviceconnector.test.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;


import com.sjl.deviceconnector.test.entity.MessageEvent;
import com.sjl.deviceconnector.test.util.TUtils;
import com.sjl.deviceconnector.test.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * 基类Activity
 *
 * @author Kelly
 * @version 1.0.0
 * @filename BaseActivity
 * @time 2021/9/11 14:40
 * @copyright(C) 2021 song
 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    protected VB viewBinding;
    protected Context mContext;
    protected LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Method inflate = TUtils.getClass(getClass()).getDeclaredMethod("inflate", LayoutInflater.class);
            viewBinding = (VB) inflate.invoke(null, getLayoutInflater());
            setContentView(viewBinding.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mContext = this;
        loadingDialog = new LoadingDialog(this);
        initView();
        initListener();
        initData();

    }


    protected abstract void initView();

    protected abstract void initListener();

    protected abstract void initData();

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        _onMessageEvent(event);
    }

    protected void _onMessageEvent(MessageEvent event) {
    }


    public void openActivity(Class clz) {
        openActivity(clz, null);
    }

    public void openActivity(Class clz, Bundle bundle) {
        Intent intent = new Intent(mContext, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        mContext.startActivity(intent);
    }



    /**
     * 判断Activity是否Destroy
     *
     * @param mActivity
     * @return
     */
    public static boolean isDestroy(Activity mActivity) {
        if (mActivity == null || mActivity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())) {
            return true;
        } else {
            return false;
        }
    }
    protected void showLoading() {
        if (loadingDialog != null) {
            loadingDialog.show();
        }
    }

    protected void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消监听
        hideLoading();
        loadingDialog = null;
        viewBinding = null;
    }

}

