package org.iqiyi.datareact;

import android.arch.core.executor.ArchTaskExecutor;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.iqiyi.datareact.meta.SubscriberInfo;
import org.iqiyi.datareact.meta.SubscriberInfoIndex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 包含生命周期的数据同步框架,支持对observer的精准定位
 *
 * Created by sunxuewei on 2017/8/24.
 */

public class DataReact {
    /** TAG */
    private static final String TAG = "DataReact";
    /** DEBUG开关 */
    private static final boolean DEBUG = false;
    /** 设置要debug的type */
    private static String sDebugType;
    /** 存储LifecycleData */
    private static final ConcurrentHashMap<String, LifecycleData<Data>>
            sLiveData = new ConcurrentHashMap<>();
    /** 存储没有生命周期，需要手动移除的观察者 */
    private static final ConcurrentHashMap<String, SafeIterableMap<Observer<Data>,
            ObserverWrapper>> sData = new ConcurrentHashMap<>();

    private static List<SubscriberInfoIndex> sSubscriberInfoIndex = new ArrayList();

    /** 移除FeatureLiveData避免memory leak */
    private static LifecycleData.OnDestroyListener
            sOnDestroyListener = new LifecycleData.OnDestroyListener() {
        @Override
        public void onDestroy(LifecycleData data) {
            for (Iterator<Map.Entry<String, LifecycleData<Data>>>
                    it = sLiveData.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, LifecycleData<Data>> item = it.next();
                if (item.getValue().equals(data)) {
                    it.remove();
                }
            }
        }
    };

    /**
     * 保存注解方法信息
     * 使用{@link #bind(Object, LifecycleRegistryOwner)}方法需要在该模块初始化时调用此方法
     * @param index index
     */
    public static void addIndex(SubscriberInfoIndex index) {
        sSubscriberInfoIndex.add(index);
    }

    /**
     * 将注解方法绑定到lifecycle
     * @param object 注解方法所属的对象,非空
     * @param lifecycle lifecycle,非空
     */
    public static void bind(final Object object, LifecycleRegistryOwner lifecycle) {
        if (sSubscriberInfoIndex.size() == 0) {
            if (DEBUG) {
                throw new RuntimeException("no subscriber info, please ensure invoke addIndex!!!");
            }
            return;
        }
        for (SubscriberInfoIndex info : sSubscriberInfoIndex) {
            SubscriberInfo subscriberInfo = info.getSubscriberInfo(object.getClass());
            if (subscriberInfo != null) {
                SubscriberMethod[] methods = subscriberInfo.getSubscriberMethods();
                if (methods == null || methods.length == 0) {
                    if (DEBUG) {
                        throw new RuntimeException("no DataReact method, please use DataSubscribe annotation!");
                    }
                    return;
                }
                for (final SubscriberMethod method : methods) {
                    observe(method.dataType, lifecycle, new Observer<Data>() {
                        @Override
                        public void onChanged(@Nullable Data data) {
                            try {
                                method.method.invoke(object, data);
                            } catch (Exception e) {
                                if (DEBUG) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                }
            }
            else {
                if (DEBUG) {
                    throw new RuntimeException("no subscriber info, please ensure invoke addIndex()!!!");
                }
            }
        }
    }

    /**
     * 设置观察某类数据，在主线程调用，观察者会随依附的lifecycle销毁自动销毁
     * @param dataType 必需，设置要观察的数据类型
     * @param lifecycle 必需，观察者依附的lifecycle
     * @param observer 必需，观察者
     */
    @MainThread
    public static void observe(String dataType, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        observe(dataType, null, lifecycle, observer, false);
    }

    /**
     * 设置观察某类数据，在主线程调用，观察者会随依附的lifecycle销毁自动销毁
     * @param dataType 必需，设置要观察的数据类型
     * @param dataId 非必需 需要精确观察数据id
     * @param lifecycle 必需，观察者依附的lifecycle
     * @param observer 必需，观察者
     */
    @MainThread
    public static void observe(String dataType, Object dataId, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        observe(dataType, dataId, lifecycle, observer, false);
    }

    /**
     * 设置观察某类数据，dataId不空可精准定位某个数据，在主线程调用
     * @param dataType 要观察的数据类型
     * @param dataId 具体数据的id，表示一类数据中精确的某个数据
     *               比如feed列表中某条数据的observer可设置dataId为position/feedId或card的block_id
     *               如果想修改dataId，可以再次调用该方法，lifecycle和observer跟上次调用保持一致即可
     * @param lifecycle 观察者依附的lifecycle
     * @param observer 观察者，随依附的lifecycle销毁自动销毁
     * @param  isMutex  是否定义该观察者事件为互斥事件，同一dataType最多有一个互斥事件观察者，后面注册的覆盖前面的
     */
    @MainThread
    public static void observe(String dataType, Object dataId, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer, boolean isMutex) {
        if (!(TextUtils.isEmpty(dataType)) && lifecycle != null && observer != null) {
            if (DEBUG) {
                Log.d(TAG, "observe dataType:" + dataType + " dataId:" + dataId
                        + " lifecycle:" + lifecycle.getClass().getSimpleName()
                        + " observer:" + observer);
            }
            LifecycleData<Data> liveData = sLiveData.get(dataType);
            if (liveData == null) {
                liveData = new LifecycleData<>();
                sLiveData.put(dataType, liveData);
                liveData.setOnDestroyListener(sOnDestroyListener);
            }
            liveData.observe(lifecycle, observer, dataId, isMutex);
        }
        else {
            throw new IllegalArgumentException("DataReact observe: invalid params!");
        }
    }

    /**
     *设置互斥事件的观察者(设置多个互斥观察者，只有一个观察者收到通知，默认为后注册覆盖前面的)
     * @param dataType 要观察的数据类型
     * @param lifecycle 观察者依附的lifecycle
     * @param observer 观察者，事件分发完成时即被销毁 否则随依附的lifecycle销毁自动销毁
     */
    public static void observeMutex(String dataType, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        observe(dataType, null, lifecycle, observer, true);
    }

    /**
     * 批量注册观察者
     */
    @MainThread
    public static void observe(List<String> typeList, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        if (typeList != null && typeList.size() > 0) {
            for (String dataType : typeList) {
                observe(dataType, lifecycle, observer);
            }
        } else {
            throw new IllegalArgumentException("DataReact observe: list is empty!");
        }
    }

    /**
     * 批量注册观察者
     */
    @MainThread
    public static void observe(List<String> typeList, Object dataId, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        if (typeList != null && typeList.size() > 0) {
            for (String dataType : typeList) {
                observe(dataType, dataId, lifecycle, observer, false);
            }
        } else {
            throw new IllegalArgumentException("DataReact observe: list is empty!");
        }
    }

    /**
     * 批量注册观察者
     */
    @MainThread
    public static void observe(String[] typeList, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        if (typeList != null && typeList.length > 0) {
            for (String dataType : typeList) {
                observe(dataType, lifecycle, observer);
            }
        } else {
            throw new IllegalArgumentException("DataReact observe: list is empty!");
        }
    }

    /**
     * 批量注册观察者
     */
    @MainThread
    public static void observe(String[] typeList, Object dataId, LifecycleRegistryOwner lifecycle,
            Observer<Data> observer) {
        if (typeList != null && typeList.length > 0) {
            for (String dataType : typeList) {
                observe(dataType, dataId, lifecycle, observer, false);
            }
        } else {
            throw new IllegalArgumentException("DataReact observe: list is empty!");
        }
    }

    /**
     * 注册没有生命周期的观察者，需要手动解注册{@link #unRegister(String, Observer)}
     * 否则会造成内存泄漏
     * @param dataType 需要观察的数据类型
     * @param observer 观察者，需要解注册，否则会造成内存泄漏
     */
    public static void register(String dataType, Observer<Data> observer) {
        register(dataType, null, observer);
    }

    /**
     * 注册没有生命周期的观察者，需要手动解注册{@link #unRegister(String, Observer)}
     * 否则会造成内存泄漏
     * @param dataType 需要观察的数据类型
     * @param dataId 需要观察的具体数据id
     * @param observer 观察者，需要解注册，否则会造成内存泄漏
     */
    public static void register(String dataType, Object dataId, Observer<Data> observer) {
        if (!(TextUtils.isEmpty(dataType)) && observer != null) {
            if (DEBUG) {
                Log.d(TAG, "register dataType:" + dataType + " dataId:" + dataId
                        + " observer:" + observer);
            }
            synchronized (sData) {
                SafeIterableMap<Observer<Data>, ObserverWrapper> observers = sData.get(dataType);
                if (observers == null) {
                    observers = new SafeIterableMap<>();
                    sData.put(dataType, observers);
                }
                ObserverWrapper observerWrapper = observers.getValue(observer);
                if (observerWrapper != null) {
                    observerWrapper.setId(dataId);
                }
                else {
                    observerWrapper = new ObserverWrapper(observer, dataId);
                    observers.putIfAbsent(observer, observerWrapper);
                }
            }
        }
        else {
            throw new IllegalArgumentException("DataReact register: invalid params!");
        }
    }

    /**
     * 批量注册没有生命周期的观察者，需要手动解注册{@link #unRegister(String, Observer)}
     * 否则会造成内存泄漏
     * @param typeList 需要观察的数据类型列表
     * @param observer 观察者，需要解注册，否则会造成内存泄漏
     */
    public static void register(List<String> typeList, Observer<Data> observer) {
        if (typeList != null && typeList.size() > 0) {
            for (String dataType : typeList) {
                register(dataType, null, observer);
            }
        } else {
            throw new IllegalArgumentException("DataReact register: list is empty!");
        }
    }

    /**
     * 批量注册没有生命周期的观察者，需要手动解注册{@link #unRegister(String, Observer)}
     * 否则会造成内存泄漏
     * @param typeList 需要观察的数据类型列表
     * @param observer 观察者，需要解注册，否则会造成内存泄漏
     */
    public static void register(String[] typeList, Observer<Data> observer) {
        if (typeList != null && typeList.length > 0) {
            for (String dataType : typeList) {
                register(dataType, null, observer);
            }
        } else {
            throw new IllegalArgumentException("DataReact register: list is empty!");
        }
    }

    /**
     * 解注册观察者，避免内存泄漏
     * @param dataType 观察者对应的数据类型
     * @param observer 观察者
     */
    public static void unRegister(String dataType, Observer<Data> observer) {
        if (!(TextUtils.isEmpty(dataType)) && observer != null) {
            if (DEBUG) {
                Log.d(TAG, "unRegister dataType:" + dataType + " observer:" + observer);
            }
            synchronized (sData) {
                SafeIterableMap<Observer<Data>, ObserverWrapper> observers = sData.get(dataType);
                if (observers != null) {
                    ObserverWrapper observerWrapper = observers.remove(observer);
                    if (DEBUG && observerWrapper == null) {
                        throw new RuntimeException("DataReact unRegister: wrong params!");
                    }
                    if (observers.size() == 0) {
                        sData.remove(dataType);
                    }
                }
                else {
                    if (DEBUG) {
                        throw new RuntimeException("DataReact unRegister: wrong params!");
                    }
                }
            }
        }
        else {
            throw new IllegalArgumentException("DataReact unRegister: invalid params!");
        }
    }

    /**
     * 批量解注册观察者，避免内存泄漏
     * @param typeList 观察者对应的数据类型类别
     * @param observer 观察者
     */
    public static void unRegister(List<String> typeList, Observer<Data> observer) {
        if (typeList != null && typeList.size() > 0) {
            for (String dataType : typeList) {
                unRegister(dataType, observer);
            }
        } else {
            throw new IllegalArgumentException("DataReact register: list is empty!");
        }
    }

    /**
     * 解注册互斥观察者
     *
     * @param dataType 观察者对应的数据类型
     */
    public static void unRegisterMutex(String dataType) {
        LifecycleData<Data> liveData = sLiveData.get(dataType);
        if (liveData != null) {
            liveData.removeMutexObserver();
        }
    }

    /**
     * 非主线程同步数据
     * @param data 要同步的数据
     */
    public static void post(final Data data) {
        if (data != null && !(TextUtils.isEmpty(data.getType()))) {
            if (DEBUG) {
                Log.d(TAG, "post data:" + data);
            }

            // 处理有生命周期的observer
            LifecycleData<Data> liveData = sLiveData.get(data.getType());
            if (liveData != null) {
                liveData.postValue(data);
            }

            // 处理没有生命周期的observer
            final SafeIterableMap<Observer<Data>, ObserverWrapper>
                    observers = sData.get(data.getType());
            if (observers != null) {
                ArchTaskExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyObserver(observers, data);
                    }
                });
            }


            if (DEBUG && data.getType().equals(sDebugType)) {
                Log.d(TAG, "post data:" + data + " stack: " +
                        Log.getStackTraceString(new RuntimeException(TAG)));
            }
        }
        else {
            throw new IllegalArgumentException("DataReact post data: invalid params!");
        }
    }

    /**
     * 主线程对外同步数据
     *
     * @param type 要同步的数据type
     */
    @MainThread
    public static void set(String type) {
        if (!TextUtils.isEmpty(type)) {
            set(new Data(type));
        }
    }

    /**
     * 主线程对外同步数据
     * @param data 要同步的数据
     */
    @MainThread
    public static void set(Data data) {
        if (data != null && !(TextUtils.isEmpty(data.getType()))) {
            if (DEBUG) {
                Log.d(TAG, "set data:" + data);
            }

            // 处理有生命周期的observer
            LifecycleData<Data> liveData = sLiveData.get(data.getType());
            if (liveData != null) {
                liveData.setValue(data);
            }

            // 处理没有生命周期的observer
            SafeIterableMap<Observer<Data>, ObserverWrapper> observers = sData.get(data.getType());
            if (observers != null) {
                notifyObserver(observers, data);
            }

            if (DEBUG && data.getType().equals(sDebugType)) {
                Log.d(TAG, "set data:" + data + " stack: " +
                        Log.getStackTraceString(new RuntimeException(TAG)));
            }
        }
        else {
            throw new IllegalArgumentException("DataReact set data: invalid params!");
        }

    }

    /**
     * 数据改变后通知没有生命周期的observer
     * @param observers observers
     * @param data data
     */
    private static void notifyObserver(SafeIterableMap<Observer<Data>, ObserverWrapper> observers,
            Data data) {
        for (Iterator<Map.Entry<Observer<Data>, ObserverWrapper>> iterator =
                observers.iteratorWithAdditions(); iterator.hasNext(); ) {
            ObserverWrapper observerWrapper = iterator.next().getValue();
            if (data.getId() == null || data.getId().equals(observerWrapper.getId())) {
                observerWrapper.getObserver().onChanged(data);
            }
        }
    }

    /**
     * 用于设置要debug的数据type,设置后该类数据更新时会打印出调用栈
     * @param type 要调试的数据type
     */
    public static void setDebugData(String type) {
        sDebugType = type;
    }
}
