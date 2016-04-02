package com.wenming.androidprocess.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wenming.andriodprocess.R;
import com.wenming.androidprocess.Features;
import com.wenming.androidprocess.adapter.ViewPagerAdapter;
import com.wenming.androidprocess.fragment.OneFragment;
import com.wenming.androidprocess.fragment.ProfileFragment;
import com.wenming.androidprocess.service.MyService;
import com.wenming.library.BackgroundUtil;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Context mContext;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initToolBar();
        initTabViewPager();
        Features.showForeground = true;
        intent = new Intent(mContext, MyService.class);
        startService(intent);
    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setVisibility(View.GONE);
    }

    private void initTabViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OneFragment(mContext), getString(R.string.tab1));
        adapter.addFragment(new ProfileFragment(mContext), getString(R.string.tab3));
        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0, false);
    }

    @Override
    protected void onDestroy() {
        Features.showForeground = false;
        Features.BGK_METHOD = BackgroundUtil.BKGMETHOD_GETRUNNING_TASK;
        stopService(intent);
        super.onDestroy();
    }
}
