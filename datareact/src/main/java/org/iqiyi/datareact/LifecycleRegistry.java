package org.iqiyi.datareact;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;

/**
 *
 * Created by sunxuewei on 2017/8/25.
 */

public class LifecycleRegistry extends android.arch.lifecycle.LifecycleRegistry {
    /**
     * Creates a new LifecycleRegistry for the given provider.
     * <p>
     * You should usually create this inside your LifecycleOwner class's constructor and hold
     * onto the same instance.
     *
     * @param provider The owner LifecycleOwner
     */
    public LifecycleRegistry(@NonNull LifecycleOwner provider) {
        super(provider);
    }
}
