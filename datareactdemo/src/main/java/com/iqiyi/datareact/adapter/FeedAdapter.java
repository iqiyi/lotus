package com.iqiyi.datareact.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.iqiyi.datareact.FeedObserver;
import com.iqiyi.datareact.activity.SecondActivity;
import com.iqiyi.datareact.cons.DataReactType;
import com.iqiyi.datareact.entity.Feed;

import org.iqiyi.datareact.Data;
import org.iqiyi.datareact.DataReact;
import org.iqiyi.datareact.LifecycleRegistryOwner;

import java.util.ArrayList;

import datareact.iqiyi.com.lotus.R;

/**
 * Created by liangxu on 2018/03/15.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private ArrayList<Feed> mData = new ArrayList<>();
    private Context mContext;

    public FeedAdapter(Context context, ArrayList<Feed> data) {
        mContext = context;
        mData = data;
    }

    public void setData(ArrayList<Feed> data) {
        mData = data;
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("DataReactI", "onCreateViewHolder");
        return new FeedViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.pp_qz_feed_sum_up, parent, false));
    }

    @Override
    public void onBindViewHolder(final FeedViewHolder holder, final int position) {
        final Feed feed = mData.get(position);
        holder.feed = feed;
        holder.tvContent.setText(holder.feed.content);
        if (feed.commentCount > 0) {
            holder.tvComment.setText(holder.feed.commentCount + "");
        } else {
            holder.tvComment.setText("评论");
        }
        if (holder.feed.isLike) {
            holder.ivPraise.setImageResource(R.drawable.pp_qz_feed_like);
        } else {
            holder.ivPraise.setImageResource(R.drawable.pp_qz_feed_unlike);
        }
        holder.ivPraise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.feed.isLike = !holder.feed.isLike;
                if (holder.feed.isLike) {
                    holder.ivPraise.setImageResource(R.drawable.pp_qz_feed_like);
                } else {
                    holder.ivPraise.setImageResource(R.drawable.pp_qz_feed_unlike);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SecondActivity.class);
                intent.putExtra("feed", feed);
                mContext.startActivity(intent);
            }
        });
        if (position == 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DataReact.set(new Data("test_sticky"));
                }
            }, 5000);
        }

        DataReact.observe(DataReactType.FEED_LIST_CHANGE, feed.feedId,  (LifecycleRegistryOwner) mContext, holder.observer);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        private TextView tvComment;
        private ImageView ivPraise;
        private TextView tvContent;
        private Feed feed;
        FeedObserver observer;

        public FeedViewHolder(View itemView) {
            super(itemView);

            tvContent = itemView.findViewById(R.id.tv_content);
            tvComment = itemView.findViewById(R.id.gc_feed_comment_size);
            ivPraise =  itemView.findViewById(R.id.gc_feed_praise_iv);
            observer = new FeedObserver<Data>() {
                @Override
                public void onChanged(@Nullable Data data) {
                    final Feed changeFeed = (Feed) data.getData();
                    Log.e("DataReactI", "position:" + "  " + changeFeed.toString());
                    feed.isLike = changeFeed.isLike;
                    feed.commentCount = changeFeed.commentCount;
                    if (changeFeed.commentCount > 0) {
                        tvComment.setText(changeFeed.commentCount + "");
                    } else {
                        tvComment.setText("评论");
                    }
                    if (changeFeed.isLike) {
                        ivPraise.setImageResource(R.drawable.pp_qz_feed_like);
                    } else {
                        ivPraise.setImageResource(R.drawable.pp_qz_feed_unlike);
                    }
                }
            };
        }
    }
}
