package org.iqiyi.datareact;

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;
import static android.arch.lifecycle.Lifecycle.State.STARTED;

import android.arch.core.executor.ArchTaskExecutor;
import android.arch.lifecycle.Lifecycle.State;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * LifecycleData is a data holder class that can be observed within a given lifecycle.
 * This means that an {@link Observer} can be added in a pair with a {@link LifecycleOwner}, and
 * this observer will be notified about modifications of the wrapped data only if the paired
 * LifecycleOwner is in active state. LifecycleOwner is considered as active, if its state is
 * {@link State#STARTED} or {@link State#RESUMED}.
 *
 * <p> An observer added with a Lifecycle will be automatically removed if the corresponding
 * Lifecycle moves to {@link State#DESTROYED} state. This is especially useful for
 * activities and fragments where they can safely observe LifecycleData and not worry about leaks:
 * they will be instantly unsubscribed when they are destroyed.
 *
 * This class can also be used for sharing data between different modules in your application
 * in a decoupled fashion.
 *
 * @param <T> The type of data hold by this instance
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class LifecycleData<T> {
    static final int START_VERSION = -1;

    private SafeIterableMap<Observer<T>, LifecycleBoundObserver> mObservers =
            new SafeIterableMap<>();

    private boolean mDispatchingValue;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean mDispatchInvalidated;
    private List mDataQueue = new LinkedList();
    private LifecycleBoundObserver mSingleObserver;

    private void considerNotify(LifecycleBoundObserver observer) {
        if (!observer.active || mDataQueue.size() == 0) {
            return;
        }
        // Check latest state b4 dispatch. Maybe it changed state but we didn't get the event yet.
        //
        // we still first check observer.active to keep it as the entrance for events. So even if
        // the observer moved to an active state, if we've not received that event, we better not
        // notify for a more predictable notification order.
        if (!isActiveState(observer.owner.getLifecycle().getCurrentState())) {
            return;
        }
//        Log.e("DataReact", "data queue size:" + mDataQueue.size());
        List dataCopy = new LinkedList();
        dataCopy.addAll(mDataQueue);
        for (int i = 0; i < dataCopy.size(); i++) {
            Object data = dataCopy.get(i);
            Data target = null;
            DataStatus ds = observer.mDataStatusMap.get(data.hashCode(), new DataStatus());
            if (!ds.include) {
                continue;
            }
            if (data instanceof Data) {
                target = (Data) data;
            }
            if ((observer.id == null) || ((target != null) && (target.getId() != null)
                    && (target.getId().toString().equals(observer.id.toString())))) {
                observer.observer.onChanged((T) data);
                if (target.isAbandoned()) {
                    mDataQueue.remove(data);
                }
            }
            ds.include = false;
            observer.mDataStatusMap.remove(data.hashCode());
            target.setObserverCount((target.getObserverCount() - 1));
            //remove data
            if (target.getObserverCount() == 0) {
                mDataQueue.remove(data);
            }
//            Log.e("DataReact", "data queue size:" + mDataQueue.size() + " target size:"
//                    + target.getObserverCount() + " ds size:" + observer.mDataStatuses.size());

        }
        if(observer == mSingleObserver) {
            removeObserver(mSingleObserver.observer);
        }

    }

    private void dispatchingValue(@Nullable LifecycleBoundObserver initiator, T value) {
        Data data = null;
        if (mDispatchingValue) {
            mDispatchInvalidated = true;
            return;
        }
        mDispatchingValue = true;
        do {
            mDispatchInvalidated = false;
            if (initiator != null) {
                considerNotify(initiator);
                initiator = null;
            } else {
                if(value instanceof Data){
                    data = (Data) value;
                }

                if (data != null) {
                    data.setObserverCount(data.getObserverCount() + mObservers.size());
                }

                for (Iterator<Map.Entry<Observer<T>, LifecycleBoundObserver>> iterator =
                        mObservers.iteratorWithAdditions(); iterator.hasNext(); ) {
                    LifecycleBoundObserver lbo = iterator.next().getValue();
                    prepareNotify(value, lbo);
                    if (mDispatchInvalidated) {
                        break;
                    }
                }
                if(mSingleObserver != null){
                    prepareNotify(value, mSingleObserver);
                }
            }
        } while (mDispatchInvalidated);
        mDispatchingValue = false;
    }

    private void prepareNotify(T data, LifecycleBoundObserver lbo){
        DataStatus ds = new DataStatus();
        ds.include = true;
        lbo.mDataStatusMap.put(data.hashCode(),ds);
        considerNotify(lbo);
    }
    /**
     * Adds the given observer to the observers list within the lifespan of the given
     * owner. The events are dispatched on the main thread. If LifecycleData already has data
     * set, it will be delivered to the observer.
     * <p>
     * The observer will only receive events if the owner is in {@link State#STARTED}
     * or {@link State#RESUMED} state (active).
     * <p>
     * If the owner moves to the {@link State#DESTROYED} state, the observer will
     * automatically be removed.
     * <p>
     * When data changes while the {@code owner} is not active, it will not receive any updates.
     * If it becomes active again, it will receive the last available data automatically.
     * <p>
     * LifecycleData keeps a strong reference to the observer and the owner as long as the
     * given LifecycleOwner is not destroyed. When it is destroyed, LifecycleData removes references to
     * the observer &amp; the owner.
     * <p>
     * If the given owner is already in {@link State#DESTROYED} state, LifecycleData
     * ignores the call.
     * <p>
     * If the given owner, observer tuple is already in the list, the call will change the id.
     * If the observer is already in the list with another owner, LifecycleData throws an
     * {@link IllegalArgumentException}.
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    @MainThread
    void observe(LifecycleOwner owner, Observer<T> observer, Object id, boolean isMutex) {
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        if (isMutex) {
            if (mSingleObserver != null) {
                removeObserver(mSingleObserver.observer);
            }
            mSingleObserver = new LifecycleBoundObserver(owner, observer, null);
            owner.getLifecycle().addObserver(mSingleObserver);
        } else {
            LifecycleBoundObserver wrapper = mObservers.getValue(observer);
            if (wrapper != null) {
                if (wrapper.owner != owner) {
                    throw new IllegalArgumentException("Cannot change observer");
                }
                wrapper.id = id;
                return;
            }
            wrapper = new LifecycleBoundObserver(owner, observer, id);
            mObservers.putIfAbsent(observer, wrapper);
            owner.getLifecycle().addObserver(wrapper);
        }
    }

    /**
     * Removes the given observer from the observers list.
     *
     * @param observer The Observer to receive events.
     */
    @MainThread
    void removeObserver(final Observer<T> observer) {
        LifecycleBoundObserver removed = mObservers.remove(observer);
        if(removed != null){
            removed.owner.getLifecycle().removeObserver(removed);
        }else if(mSingleObserver != null && mSingleObserver.observer == observer){
            mSingleObserver.owner.getLifecycle().removeObserver(mSingleObserver);
            mSingleObserver = null;
        }else {
            return;
        }
        if (!hasObservers() && mOnDestroyListener != null) {
            mOnDestroyListener.onDestroy(this);
        }
    }

    /**
     * Removes all observers that are tied to the given {@link LifecycleOwner}.
     *
     * @param owner The {@code LifecycleOwner} scope for the observers to be removed.
     */
    @MainThread
    void removeObservers(final LifecycleOwner owner) {
        for (Map.Entry<Observer<T>, LifecycleBoundObserver> entry : mObservers) {
            if (entry.getValue().owner == owner) {
                removeObserver(entry.getKey());
            }
        }
        if (mSingleObserver != null && mSingleObserver.owner == owner) {
            removeObserver(mSingleObserver.observer);
        }
    }

    /**
     *  Remove mutex observer
     */
    @MainThread
    void removeMutexObserver() {
        if (mSingleObserver != null) {
            removeObserver(mSingleObserver.observer);
        }
    }
    /**
     * Posts a task to a main thread to set the given value.
     *
     * @param value The new value
     */
    protected void postValue(final T value) {
        ArchTaskExecutor.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                setValue(value);
            }
        });
    }

    /**
     * Sets the value. If there are active observers, the value will be dispatched to them.
     * <p>
     * This method must be called from the main thread. If you need set a value from a background
     * thread, you can use {@link #postValue(Object)}
     *
     * @param value The new value
     */
    @MainThread
    protected void setValue(T value) {
        assertMainThread("setValue");
        mDataQueue.add(value);
        dispatchingValue(null, value);
    }

    /**
     * Returns true if this LifecycleData has observers.
     *
     * @return true if this LifecycleData has observers
     */
    boolean hasObservers() {
        return (mObservers.size() > 0) || (mSingleObserver != null);
    }

    class LifecycleBoundObserver implements LifecycleObserver {
        public final LifecycleOwner owner;
        public final Observer<T> observer;
        public boolean active;
        public int lastVersion = START_VERSION;
        public Object id;
        public SparseArray<DataStatus> mDataStatusMap = new SparseArray<>();

        LifecycleBoundObserver(LifecycleOwner owner, Observer<T> observer, Object id) {
            this.owner = owner;
            this.observer = observer;
            this.id = id;
        }

        @SuppressWarnings("unused")
        @Override
        public void onStateChange() {
            if (owner.getLifecycle().getCurrentState() == DESTROYED) {
                removeObserver(observer);
                return;
            }
            // immediately set active state, so we'd never dispatch anything to inactive
            // owner
            activeStateChanged(isActiveState(owner.getLifecycle().getCurrentState()));

        }

        void activeStateChanged(boolean newActive) {
            Log.e("lifecylexl","activeStateChanged="+newActive+ " life owner="+owner);
            if (newActive == active) {
                return;
            }
            active = newActive;
            if (active) {
                dispatchingValue(this, null);
            }
        }
    }

    static boolean isActiveState(State state) {
        return state.isAtLeast(STARTED);
    }

    private void assertMainThread(String methodName) {
        if (!ArchTaskExecutor.getInstance().isMainThread()) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on a background"
                    + " thread");
        }
    }

    private OnDestroyListener mOnDestroyListener;

    public void setOnDestroyListener(OnDestroyListener onDestroyListener) {
        mOnDestroyListener = onDestroyListener;
    }

    public interface OnDestroyListener {
        void onDestroy(LifecycleData data);
    }
}
