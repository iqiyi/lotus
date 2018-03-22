package com.iqiyi.datareact.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.iqiyi.datareact.fragment.FeedDetailWithLifeCycleInterfaceFragment;

import datareact.iqiyi.com.lotus.R;

/**
 * Created by liangxu on 2018/3/15.
 */

public class CommonActivity extends FragmentActivity {

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        FeedDetailWithLifeCycleInterfaceFragment detailFragment = FeedDetailWithLifeCycleInterfaceFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content,
                detailFragment).commitAllowingStateLoss();
    }
}
