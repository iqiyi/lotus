package org.iqiyi.datareact;

import android.support.annotation.Nullable;

/**
 * 数据观察者
 * Created by sunxuewei on 2017/8/25.
 */

public interface Observer<T> extends android.arch.lifecycle.Observer<T>{
    /**
     * Called when the data is changed.
     * @param t  The new data
     */
    @Override
    void onChanged(@Nullable T t);
}
