package com.iqiyi.datareact.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iqiyi.datareact.activity.FirstActivity;
import com.iqiyi.datareact.cons.DataReactType;
import com.iqiyi.datareact.entity.Feed;

import org.iqiyi.datareact.Data;
import org.iqiyi.datareact.DataReact;

import datareact.iqiyi.com.lotus.R;


/**
 * Created by liangxu on 2018/03/15.
 */

public class FeedDetailFragment extends Fragment {
    private View rootView;
    private TextView mTvComment;
    private TextView mTvLike;
    private TextView mTvContent;
    private TextView mTvDetail;
    private TextView mTvOnStart;
    private TextView mTvOnStop;
    private TextView mTvOnResume;
    private TextView mTvOnDestroy;
    private Feed feed;

    public static FeedDetailFragment newInstance(Feed feed) {

        Bundle args = new Bundle();
        args.putParcelable("feed", feed);
        FeedDetailFragment fragment = new FeedDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_detail, null);
        }
        initView(rootView);
        initData();
        return rootView;
    }

    private void initData() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            feed = (Feed) arguments.get("feed");
        }
//        DataReact.observe("test_sticky", (LifecycleRegistryOwner) getActivity(), new Observer() {
//            @Override
//            public void onChanged(@Nullable Object o) {
//                Toast.makeText(getActivity(),"test_sticky",Toast.LENGTH_SHORT).show();
//            }
//        });
        mTvComment.setText(getString(R.string.comments_count, feed.commentCount + ""));
        mTvLike.setText(getString(R.string.is_like, feed.isLike + ""));
        mTvContent.setText(getString(R.string.feed_content, feed.content));

        mTvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.commentCount++;
                mTvComment.setText(getString(R.string.comments_count, feed.commentCount + ""));
                Feed f= new Feed();
                f.commentCount = feed.commentCount;
                DataReact.set(new Data(DataReactType.FEED_LIST_CHANGE).setId(feed.feedId).setData(f));
                DataReact.set(new Data("aaa").setData("nice!"));
            }
        });
        mTvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.isLike = !feed.isLike;
                mTvLike.setText(getString(R.string.is_like, feed.isLike + ""));
                DataReact.set(new Data(DataReactType.FEED_LIST_CHANGE).setId(feed.feedId).setData(feed));
            }
        });
        mTvDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FirstActivity.class);
                intent.putExtra("feed", feed);
                getActivity().startActivity(intent);
            }
        });
    }

    private void initView(View rootView) {
        mTvComment = (TextView) rootView.findViewById(R.id.tv_comment);
        mTvLike = (TextView) rootView.findViewById(R.id.tv_like);
        mTvContent = (TextView) rootView.findViewById(R.id.tv_content);
        mTvDetail = (TextView) rootView.findViewById(R.id.tv_detail);
        mTvOnStart = (TextView) rootView.findViewById(R.id.tv_start);
        mTvOnResume = (TextView) rootView.findViewById(R.id.tv_on_resume);
        mTvOnStop = (TextView) rootView.findViewById(R.id.tv_on_stop);
        mTvOnDestroy = (TextView) rootView.findViewById(R.id.tv_on_destroy);
    }
}
