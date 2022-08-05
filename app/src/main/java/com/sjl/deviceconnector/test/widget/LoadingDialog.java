package com.sjl.deviceconnector.test.widget;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjl.deviceconnector.test.R;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename LoadingDialog
 * @time 2022/6/13 15:55
 * @copyright(C) 2022 song
 */
public class LoadingDialog {
    AVLoadingIndicatorView mAvLoadingIndicatorView;
    Dialog mLoadingDialog;
    ImageView ivClose;
    TextView tvMsg;
    LoadingListener loadingListener;
    public LoadingDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.load_dialog, null);
        mAvLoadingIndicatorView = view.findViewById(R.id.indicator);
        tvMsg = view.findViewById(R.id.tvMsg);
        ivClose = view.findViewById(R.id.iv_close);

        mLoadingDialog = new Dialog(context,R.style.LoadingDialog);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setContentView(view);
        mLoadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (loadingListener != null){
                    loadingListener.onDismiss(LoadingDialog.this);
                }
            }
        });
        Window window = mLoadingDialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = context.getResources().getDimensionPixelSize(R.dimen.dp_160);
        params.height = context.getResources().getDimensionPixelSize(R.dimen.dp_100);
        window.setAttributes(params);

    }


    public void show() {
        mLoadingDialog.show();
        mAvLoadingIndicatorView.setVisibility(View.VISIBLE);//内部隐藏了，复用显示时，需要再次显示
        mAvLoadingIndicatorView.show();

    }


    public void hide() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            if (mAvLoadingIndicatorView != null) {
                mAvLoadingIndicatorView.hide();
            }
            removeLoadingListener();
            tvMsg.setText(mLoadingDialog.getContext().getString(R.string.loading_processing));
        }
    }

    public void removeLoadingListener() {
        this.loadingListener = null;
        ivClose.setVisibility(View.GONE);
    }

    public void setLoadingListener(LoadingListener loadingListener) {
        this.loadingListener = loadingListener;
        ivClose.setVisibility(View.VISIBLE);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivClose.setVisibility(View.GONE);
                if (loadingListener != null){
                    loadingListener.onClickClose(LoadingDialog.this,tvMsg);
                }
            }
        });
    }

    public interface LoadingListener{
        /**
         * 点击关闭按钮的时候触发
         * @param loadingDialog
         * @param tvMsg 提示文本控件
         */
        void onClickClose(LoadingDialog loadingDialog, TextView tvMsg);

        /**
         * 关闭加载框的时候触发
         * @param loadingDialog
         */
        void onDismiss(LoadingDialog loadingDialog);
    }

}
