package org.superasync;

interface BaseObserver<V> {
    void onResult(V result);

    void onError(Throwable e);

    boolean isObserving();
}
