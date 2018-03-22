package com.iqiyi.datareact.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iqiyi.datareact.entity.Feed;
import com.iqiyi.datareact.fragment.FeedDetailFragment;
import com.iqiyi.datareact.fragment.FeedListFragment;

import org.iqiyi.datareact.DataReact;
import org.iqiyi.datareact.LifecycleRegistry;
import org.iqiyi.datareact.LifecycleRegistryOwner;

import datareact.iqiyi.com.lotus.R;


/**
 * Created by liangxu on 2018/03/15.
 */

public class FirstActivity extends Activity implements LifecycleRegistryOwner {
    private Feed mFeed;
    private LifecycleRegistry mRegistry = new LifecycleRegistry(this);
//    private static TextView sTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_one);

        FeedListFragment feedListFragment = FeedListFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.fl_content,
                feedListFragment).commitAllowingStateLoss();
//        sTextView = new TextView(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState,
            @Nullable PersistableBundle persistentState) {

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
