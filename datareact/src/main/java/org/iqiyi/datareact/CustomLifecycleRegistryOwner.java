package org.iqiyi.datareact;

import android.arch.lifecycle.Lifecycle;

/**
 * Created by liangxu on 2018/2/9.
 * 此类为适配没有Activity 和 Fragment 的场景
 * 比如：在view中使用 或者其他任何对象 但需要注意的是 需要在相应的代码块手动调生命周期方法
 */

public class CustomLifecycleRegistryOwner implements LifecycleRegistryOwner {

    private LifecycleRegistry mRegistry = new LifecycleRegistry(this);



    public void onCreate(){
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    public void onStart(){
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    public void onResume(){
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    public void onPause(){
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    public void onStop(){
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }


    /**
     * 为了防止内存泄漏，此方法必须被调用
     */
    public void onDestroy(){
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }

}
