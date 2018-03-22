package com.iqiyi.datareact;

import android.support.annotation.Nullable;
import android.util.Log;

import org.iqiyi.datareact.Observer;

/**
 * Created by liangxu on 2018/03/15.
 */

public class FeedObserver<T> implements Observer<T> {
    public FeedObserver() {
        Log.e("DataReactI","FeedObserver");
    }


    @Override
    public void onChanged(@Nullable T t) {

    }
}
