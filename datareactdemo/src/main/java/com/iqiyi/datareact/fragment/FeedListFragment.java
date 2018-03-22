package com.iqiyi.datareact.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.iqiyi.datareact.adapter.FeedAdapter;
import com.iqiyi.datareact.entity.Feed;

import org.iqiyi.datareact.Data;
import org.iqiyi.datareact.DataReact;
import org.iqiyi.datareact.LifecycleRegistry;
import org.iqiyi.datareact.LifecycleRegistryOwner;
import org.iqiyi.datareact.Observer;

import java.util.ArrayList;

import datareact.iqiyi.com.lotus.R;

/**
 * Created by liangxu on 2018/03/15.
 */

public class FeedListFragment extends Fragment implements
        LifecycleRegistryOwner {
    private View rootView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    public static FeedListFragment newInstance() {
        FeedListFragment fragment = new FeedListFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_feed_list, null);
        }
        initView(rootView);
        initData();
        return rootView;
    }

    private void initData() {
        ArrayList<Feed> feeds = new ArrayList<>();
        Feed feed;
        for (int i = 0; i < 20; i++) {
            feed = new Feed();
            feed.content = "第" + i + "条Feed";
            feed.feedId = 10 + i;
            feeds.add(feed);
        }
        mAdapter = new FeedAdapter(getActivity(), feeds);
        mRecyclerView.setAdapter(mAdapter);

        DataReact.observe("aaa", this, new Observer<Data>() {
            @Override
            public void onChanged(@Nullable Data data) {
                Toast.makeText(getActivity(), "receive msg :" + data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_feeds);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }
}
