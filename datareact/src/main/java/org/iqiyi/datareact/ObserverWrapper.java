package org.iqiyi.datareact;

/**
 * observer wrapper类，为支持精准定位
 * Created by sunxuewei on 2017/8/31.
 */

class ObserverWrapper {
    private final Observer<Data> mObserver;
    private Object mId;

    ObserverWrapper(Observer<Data> observer, Object id) {
        mObserver = observer;
        mId = id;
    }

    void setId(Object id) {
        mId = id;
    }

    Object getId() {
        return mId;
    }

    Observer<Data> getObserver() {
        return mObserver;
    }
}
