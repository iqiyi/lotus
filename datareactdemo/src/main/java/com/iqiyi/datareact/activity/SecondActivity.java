package com.iqiyi.datareact.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.iqiyi.datareact.entity.Feed;
import com.iqiyi.datareact.fragment.FeedDetailFragment;

import org.iqiyi.datareact.LifecycleRegistry;
import org.iqiyi.datareact.LifecycleRegistryOwner;

import datareact.iqiyi.com.lotus.R;


/**
 * Created by liangxu on 2018/03/15.
 */

public class SecondActivity extends Activity implements LifecycleRegistryOwner {
    private LifecycleRegistry mRegistry = new LifecycleRegistry(this);
    private Feed mFeed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);

        mFeed = getIntent().getParcelableExtra("feed");
        FeedDetailFragment detailFragment = FeedDetailFragment.newInstance(mFeed);
        getFragmentManager().beginTransaction().replace(R.id.fl_content,
                detailFragment).commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }
}
