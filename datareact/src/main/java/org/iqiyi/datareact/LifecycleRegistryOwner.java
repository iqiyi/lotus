package org.iqiyi.datareact;


/**
 *
 * Created by sunxuewei on 2017/8/25.
 */

public interface LifecycleRegistryOwner extends android.arch.lifecycle.LifecycleRegistryOwner {
    @Override
    LifecycleRegistry getLifecycle();
}
