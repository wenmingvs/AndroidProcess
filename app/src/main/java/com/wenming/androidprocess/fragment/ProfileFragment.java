package com.wenming.androidprocess.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wenming.andriodprocess.R;
import com.wenming.androidprocess.Features;


/**
 * Created by wenmingvs on 2016/1/14.
 */
public class ProfileFragment extends Fragment {
    private Context mContext;
    private View mView;
    private WebView mContentWv;

    public ProfileFragment(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.profile_layout, container, false);
        mContentWv = (WebView) mView.findViewById(R.id.wv_webview_content);
        initWebView();

        return mView;
    }

    private void initWebView() {
        if (Features.showProfile) {
            mContentWv.getSettings().setJavaScriptEnabled(true);
            mContentWv.loadUrl("https://github.com/wenmingvs");
            mContentWv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            mContentWv.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && mContentWv.canGoBack()) {  //表示按返回键
                            mContentWv.goBack();   //后退
                            //webview.goForward();//前进
                            return true;    //已处理
                        }
                    }
                    return false;
                }
            });

        }
    }


}
