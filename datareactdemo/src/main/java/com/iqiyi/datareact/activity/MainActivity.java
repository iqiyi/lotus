package com.iqiyi.datareact.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.iqiyi.datareact.cons.DataReactType;

import org.iqiyi.datareact.Data;
import org.iqiyi.datareact.DataReact;
import org.iqiyi.datareact.LifecycleRegistry;
import org.iqiyi.datareact.LifecycleRegistryOwner;
import org.iqiyi.datareact.Observer;

import datareact.iqiyi.com.lotus.R;

public class MainActivity extends FragmentActivity implements LifecycleRegistryOwner {
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);
    public static final String FEED_COMMENTS_COUNT = "pp_feed_comments_count";
    private Button mBtnFeedsTest;
    private Button mBtnCommonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        mBtnFeedsTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FirstActivity.class);
                startActivity(intent);
            }
        });
        mBtnCommonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CommonActivity.class);
                startActivity(intent);
            }
        });
        DataReact.observe(DataReactType.FEED_COMMENTS_COUNT, "200" , MainActivity.this, new Observer<Data>() {
            @Override
            public void onChanged(@Nullable Data s) {
                Log.e("DataReact integer", s.getData() + " thread:" + Thread.currentThread()
                        .getName());
            }
        }, false);
        DataReact.observe(DataReactType.FEED_COMMENTS_COUNT, "1011",
                MainActivity.this, new Observer<Data>() {
                    @Override
                    public void onChanged(@Nullable Data s) {
                        Log.e("DataReact string", s.getData() + " thread:" + Thread.currentThread()
                                .getName());
                    }
                });



    }

    private void initViews() {
        mBtnFeedsTest = findViewById(R.id.btn_test_feeds);
        mBtnCommonTest = findViewById(R.id.btn_test_common);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e("DataRect","onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataReact.set(new Data<Integer>(FEED_COMMENTS_COUNT, new Integer(200), new Integer(10)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataReact.set(new Data<Integer>(FEED_COMMENTS_COUNT, "1011", new Integer(1011)));
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }


}
