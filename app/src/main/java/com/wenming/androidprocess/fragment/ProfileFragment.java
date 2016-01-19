package com.wenming.androidprocess.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wenming.andriodprocess.R;


/**
 * Created by wenmingvs on 2016/1/14.
 */
public class ProfileFragment extends Fragment {
    private Context mContext;
    private View mView;

    public ProfileFragment(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_two, container, false);
        return mView;
    }
}
