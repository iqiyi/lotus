package com.iqiyi.datareact.fragment;


import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.iqiyi.datareact.activity.CommonTestActivity;
import com.iqiyi.datareact.cons.DataReactType;

import org.iqiyi.datareact.Data;
import org.iqiyi.datareact.DataReact;
import org.iqiyi.datareact.LifecycleRegistry;
import org.iqiyi.datareact.LifecycleRegistryOwner;
import org.iqiyi.datareact.Observer;
import org.iqiyi.datareact.annotation.DataSubscribe;

import datareact.iqiyi.com.lotus.R;


/**
 * Created by liangxu on 2018/03/15.
 * 如果不想继承LifeCycleFragment/LifeCycleActivity 可以选择继承LifecycleRegistryOwner
 */

public class FeedDetailWithLifeCycleInterfaceFragment extends Fragment implements
        LifecycleRegistryOwner {
    private LifecycleRegistry mRegistry = new LifecycleRegistry(this);
    private View rootView;
    private Button mBtnGo;

    public static FeedDetailWithLifeCycleInterfaceFragment newInstance() {
        Bundle args = new Bundle();

        FeedDetailWithLifeCycleInterfaceFragment
                fragment = new FeedDetailWithLifeCycleInterfaceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_feed_detail_with_interface, null);
        }
        initView(rootView);
        initListener();
        initData();
        return rootView;
    }


    private void initData() {

        //写法一
        DataReact.observe(DataReactType.DATA_TEST_COMMON, this, new Observer<Data>() {
            @Override
            public void onChanged(@Nullable Data data) {
                Toast.makeText(getActivity(), (String) data.getData(), Toast.LENGTH_SHORT).show();
                Log.e("this_life", "type 1");
            }
        });
    }

    //写法二
    @DataSubscribe(dataType = DataReactType.DATA_TEST_COMMON)
    public void dataReact(Data data) {
        Toast.makeText(getActivity(), (String) data.getData(), Toast.LENGTH_SHORT).show();
        Log.e("this_life", "type 2");
    }

    private void initListener() {
        mBtnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CommonTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView(View rootView) {
        mBtnGo = rootView.findViewById(R.id.btnGo);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }
}
