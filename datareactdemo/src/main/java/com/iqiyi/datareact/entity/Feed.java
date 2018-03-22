package com.iqiyi.datareact.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liangxu on 2018/03/15.
 */

public class Feed implements Parcelable{
    public long feedId;
    public long commentCount;
    public boolean isLike;
    public String content;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.feedId);
        dest.writeLong(this.commentCount);
        dest.writeByte(this.isLike ? (byte) 1 : (byte) 0);
        dest.writeString(this.content);
    }

    public Feed() {
    }

    protected Feed(Parcel in) {
        this.feedId = in.readLong();
        this.commentCount = in.readLong();
        this.isLike = in.readByte() != 0;
        this.content = in.readString();
    }

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    @Override
    public String toString() {
        return "feed: feedId="+feedId+" comment count="+commentCount+" islike="+isLike+" content="+content;
    }
}
